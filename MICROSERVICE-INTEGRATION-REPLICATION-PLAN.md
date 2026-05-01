# Plan générique : répliquer l’intégration (Config Server, Eureka, Gateway, Feign, RabbitMQ)

Ce document décrit les **mêmes étapes** que pour **MSPatientMedcin** et **MSUser**, de façon **réutilisable** pour tout couple de microservices — avec des repères concrets pour **MSOrdonnance** et **MSLaboratoir**.

---

## 0. Principes (à respecter pour chaque service)

| Élément | Règle |
|--------|--------|
| **Nom Eureka / Feign / Gateway** | Doit être **identique** à `spring.application.name` du service cible. |
| **Config Server (profil native)** | Un fichier `config/<spring.application.name>.properties` (ou `.yml`) par service. |
| **Bootstrap local** | Dans le JAR : uniquement le **nom** de l’app + **import** Config Server (+ éventuellement actuator `refresh`). |
| **RabbitMQ** | Même **nom d’exchange** et de **routing key** côté producteur et consommateur ; la **queue** et le **binding** sont typiquement déclarés côté **consommateur** (ou partout si idempotent). |
| **Ordre de démarrage** | RabbitMQ (Docker) → Eureka → Config Server → services métier → Gateway. |

---

## 1. Config Server

### 1.1 Créer les fichiers centralisés

Emplacement : `demoConfigServer/src/main/resources/config/`.

Pour chaque service `S` avec `spring.application.name=S` :

- Créer **`S.properties`** (ou `S.yml`).
- Y déplacer tout ce qui était dans le `application.properties` / `application.yaml` du service : **port**, **Eureka**, **datasource**, **JPA**, **H2**, **RabbitMQ**, messages métier, etc.

**Exemples cibles dans ce repo :**

| Service | Fichier à ajouter / compléter | Remarque |
|---------|------------------------------|----------|
| MSOrdonnance | `MSOrdonnance.properties` | Aujourd’hui une partie est encore locale dans `MSOrdonnance` ; à **externaliser** entièrement pour être aligné avec MSUser. |
| MSLaboratoir | Fichier nommé comme **`spring.application.name`** | Si l’app s’enregistre encore sous un nom hérité (ex. `MSCandidat`), soit renommer l’app en `MSLaboratoir` partout, soit garder le nom actuel et nommer le fichier **`MSCandidat.properties`** — l’important est la **cohérence** nom fichier = `spring.application.name`. |

### 1.2 Alléger le `application` local du microservice

Dans chaque microservice, ne garder que l’équivalent de :

```properties
spring.application.name=<NOM_EUREKA>
spring.cloud.config.enabled=true
spring.config.import=optional:configserver:http://localhost:8888
management.endpoints.web.exposure.include=refresh
```

*(Pour YAML : mêmes clés sous `spring:` / `management:`.)*

### 1.3 Redémarrer le Config Server

Après ajout ou modification des fichiers sous `config/`, **recompiler / redémarrer** `demoConfigServer` pour que le profil `native` recharge le classpath.

---

## 2. Eureka

- Chaque microservice : `eureka.client.register-with-eureka=true` et `eureka.client.service-url.defaultZone=...` (souvent dans le fichier Config Server du service).
- Aucun enregistrement séparé « sur la gateway » : la gateway est **client Eureka** et résout `lb://<nom>` via le registre.

---

## 3. API Gateway

Dans `demoApiGateway` (`RouteLocator` ou `application.yml`) :

- Ajouter une route par **préfixe HTTP** exposé au client, avec `uri("lb://<spring.application.name>")`.

**Exemple pour Ordonnance et Laboratoire (à adapter aux chemins réels) :**

| Préfixe client | Cible Eureka |
|----------------|--------------|
| `/ordonnances/**` | `lb://MSOrdonnance` |
| `/laboratoires/**` | `lb://<nom_eureka_du_lab>` |

Les chemins des contrôleurs dans ce repo (indicatif) :

- MSOrdonnance : base `/ordonnances` (voir `OrdonnanceRestApi`).
- MSLaboratoir : base `/laboratoires` (voir `LaboratoireRestApi`).

Vérifier que **`spring.application.name`** du laboratoire correspond bien au nom utilisé dans `lb://...` (sinon 503 / pas d’instance).

---

## 4. Communication synchrone (OpenFeign)

### 4.1 Dépendances Maven

Dans le **client** (celui qui appelle) :

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

`spring-cloud-dependencies` (BOM) déjà géré en `dependencyManagement` comme pour MSUser / MSPatientMedcin.

### 4.2 Activer Feign

Sur la classe `@SpringBootApplication` du client :

```java
@EnableFeignClients
```

### 4.3 Interface client

Même pattern que `UserClient` dans MSPatientMedcin :

- `@FeignClient(name = "<spring.application.name_du_service_cible>")`
- Méthodes avec `@GetMapping` / `@PostMapping` et chemins **identiques** à l’API REST du service cible.

**Exemples conceptuels Ordonnance ↔ Laboratoire :**

- Depuis **MSOrdonnance** vers **MSLaboratoir** : client Feign pointant vers `GET /laboratoires/...` (nom Eureka = celui du lab).
- Depuis **MSLaboratoir** vers **MSOrdonnance** : client Feign pointant vers `GET /ordonnances/...` (nom Eureka = `MSOrdonnance`).

*(Le laboratoire a déjà des clients `PatientClient` / `MedecinClient` — même idée, vérifier les noms Eureka et les chemins.)*

### 4.4 DTO / JSON

- Créer des **DTO** côté client (ex. `UserDto`) avec les champs JSON attendus ; `@JsonIgnoreProperties(ignoreUnknown = true)` si besoin.
- Gérer les erreurs : `FeignException.NotFound`, `FeignException` pour indisponibilité (ex. 503 côté API).

---

## 5. Communication asynchrone (RabbitMQ)

### 5.1 Infrastructure

- `docker-compose.yml` (déjà présent dans `Clinic-Flow`) : service **RabbitMQ** sur **5672**, management **15672**.
- Commande typique : `docker compose up -d` depuis le dossier contenant le compose.

### 5.2 Configuration Spring

Dans le fichier Config Server de **chaque** service concerné :

```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

### 5.3 Dépendance Maven

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

### 5.4 Modèle de code (générique)

**Producteur (ex. MSOrdonnance ou MSLaboratoir)**

1. Classe **message** (POJO sérialisable JSON) : champs métier stables (`analysisId`, `patientId`, etc.).
2. **`@Configuration`** : bean `DirectExchange` (ou `TopicExchange`) + constantes `EXCHANGE`, `ROUTING_KEY`.
3. **`@Component`** avec `RabbitTemplate` : `convertAndSend(EXCHANGE, ROUTING_KEY, message)`.
4. Appeler le publisher depuis un **service** ou un **contrôleur** après l’action métier (comme `PatientUserLinkedPublisher` après sauvegarde patient).

**Consommateur (l’autre service)**

1. Même **structure JSON** du message (classe miroir ou package `messaging`).
2. **`@Configuration`** : `Queue`, `Exchange` (même nom que côté producteur), `Binding`, `Jackson2JsonMessageConverter` en `@Bean`.
3. **`@Component`** avec `@RabbitListener(queues = "...")` : traitement idempotent si possible (log, mise à jour lecture, envoi d’événement suivant).

**Référence existante dans le projet**

- Patient → User : `PatientUserLinkedPublisher`, `RabbitMqClinicPatientConfig` (MSPatientMedcin) ; `PatientUserLinkedConsumer`, `RabbitMqClinicPatientConfig` (MSUser).
- Ordonnance ↔ Lab : déjà un flux **analysis** (`AnalysisMessageProducer`, `RabbitMqConfig`, etc.) — le plan ci-dessus sert à **uniformiser** avec le même schéma (constants partagées par nom, Config Server, pas de secrets en dur dans le code).

### 5.5 Cohérence des noms

Documenter dans un petit tableau (README ou commentaire) pour chaque flux :

| Flux | Exchange | Routing key | Queue |
|------|----------|-------------|-------|
| … | … | … | … |

---

## 6. Checklist de réplication (copier-coller par service)

Pour **chaque** microservice à aligner sur le modèle MSUser / MSPatientMedcin :

- [ ] `spring.application.name` cohérent avec Eureka, Feign, Gateway et fichier Config.
- [ ] Fichier `demoConfigServer/.../config/<nom>.properties` créé ou complété (port, DB, Eureka, RabbitMQ, etc.).
- [ ] `application.properties` / `application.yaml` local réduit au bootstrap Config.
- [ ] Dépendances : `spring-cloud-starter-config`, `eureka-client`, `amqp` si Rabbit, `openfeign` si appels REST sync.
- [ ] Gateway : route `path` → `lb://<nom>`.
- [ ] Rabbit : producteur et/ou consommateur + topologie documentée.
- [ ] Feign : `@EnableFeignClients` + interfaces alignées sur les APIs réelles.
- [ ] Tests : Postman via **gateway** et en **direct** sur le port du service.

---

## 7. Ordre d’exécution pour les tests

1. `docker compose up -d` (RabbitMQ)  
2. Eureka  
3. Config Server  
4. Services métier (MSOrdonnance, MSLaboratoir, …)  
5. API Gateway  

---

## 8. Pièges fréquents

- **Port en double** : deux `server.port` identiques dans des services différents (ex. conflit si deux apps restent sur 8081) — à corriger dans Config Server.
- **Nom Eureka ≠ nom fichier Config** : le client Config ne récupère pas les bonnes propriétés.
- **`lb://` sans instance** : service non démarré ou mauvais `spring.application.name`.
- **Queue inexistante** : consommateur démarré avant la déclaration des beans ; en général les `@Bean` Queue/Binding corrigent au démarrage si RabbitMQ est up.
- **JSON incompatible** : champs ou types différents entre producteur et consommateur — utiliser les mêmes noms de propriétés Java (getters/setters).

---

*Document généré pour le dépôt Clinic-Flow : plan générique + application à MSOrdonnance / MSLaboratoir.*
