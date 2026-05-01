<<<<<<< HEAD
# Generic Plan: Microservice Integration (Config Server, Eureka, Gateway, Feign, RabbitMQ)

## 0. Principles (for every service)

| Element                | Rule                                                                 |
|------------------------|----------------------------------------------------------------------|
| Service Name           | Must match `spring.application.name` everywhere (Eureka, Feign, Gateway, Config file). |
| Config Server (native) | One file per service: `config/<spring.application.name>.properties` or `.yml`. |
| Local Bootstrap        | Local config only contains app name and Config Server import.        |
| RabbitMQ               | Use the same exchange and routing key names on both producer and consumer. Queue/binding usually declared on the consumer side. |
| Startup Order          | RabbitMQ (Docker) → Eureka → Config Server → Business Services → Gateway |
=======
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
>>>>>>> 863bddf7f4b51f2da4fdfa6d25056051499b8424

---

## 1. Config Server

<<<<<<< HEAD
### 1.1 Create Centralized Config Files

- Location: `demoConfigServer/src/main/resources/config/`
- For each service S (`spring.application.name=S`):
  - Create `S.properties` (or `S.yml`).
  - Move all config (port, Eureka, datasource, JPA, RabbitMQ, etc.) from the service’s local config to this file.

### 1.2 Minimize Local Config

- In each microservice, keep only:
  ```properties
  spring.application.name=<SERVICE_NAME>
  spring.cloud.config.enabled=true
  spring.config.import=optional:configserver:http://localhost:8888
  management.endpoints.web.exposure.include=refresh
  ```
- For YAML, use the same keys under `spring:` and `management:`.

### 1.3 Restart Config Server

- After adding/modifying files in `config/`, rebuild/restart `demoConfigServer` to reload configs.

---

## 2. Eureka (Service Discovery)

- Each microservice: set `eureka.client.register-with-eureka=true` and `eureka.client.service-url.defaultZone=...` (usually in the Config Server file).
- No separate registration for the gateway: the gateway is an Eureka client and resolves `lb://<service-name>` via the registry.
=======
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
>>>>>>> 863bddf7f4b51f2da4fdfa6d25056051499b8424

---

## 3. API Gateway

<<<<<<< HEAD
- In `demoApiGateway` (either in `RouteLocator` Java code or `application.yml`):
  - Add one route per HTTP prefix exposed to clients, with `uri("lb://<spring.application.name>")`.
- Example:
  | Client Prefix      | Eureka Target         |
  |--------------------|----------------------|
  | `/ordonnances/**`  | `lb://MSOrdonnance`  |
  | `/laboratoires/**` | `lb://MSLaboratoir`  |
- Ensure `spring.application.name` matches the name used in `lb://...`.

---

## 4. Synchronous Communication (OpenFeign)

### 4.1 Maven Dependency

- In the client service:
  ```xml
  <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-openfeign</artifactId>
  </dependency>
  ```

### 4.2 Enable Feign

- Add `@EnableFeignClients` on the main application class.

### 4.3 Feign Client Interface

- Use `@FeignClient(name = "<target-service-name>")`.
- Methods use `@GetMapping`/`@PostMapping` with paths matching the target service’s REST API.

### 4.4 DTO/JSON

- Create DTOs matching the expected JSON.
- Use `@JsonIgnoreProperties(ignoreUnknown = true)` if needed.
- Handle errors: `FeignException.NotFound`, `FeignException` for service unavailability.

---

## 5. Asynchronous Communication (RabbitMQ)

### 5.1 Infrastructure

- Ensure RabbitMQ is running (see `docker-compose.yml`).

### 5.2 Spring Configuration

- In each relevant service’s Config Server file:
  ```properties
  spring.rabbitmq.host=localhost
  spring.rabbitmq.port=5672
  spring.rabbitmq.username=guest
  spring.rabbitmq.password=guest
  ```

### 5.3 Maven Dependency

  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-amqp</artifactId>
  </dependency>
  ```

### 5.4 Code Model

- **Producer:**  
  - Message class (POJO, JSON-serializable).
  - `@Configuration` for Exchange/Key constants.
  - `@Component` with `RabbitTemplate` to send messages.
- **Consumer:**  
  - Mirror message class.
  - `@Configuration` for Queue/Exchange/Binding.
  - `@Component` with `@RabbitListener`.

### 5.5 Name Consistency

- Document exchange, routing key, and queue names for each flow.

---

## 6. Replication Checklist (per service)

- [ ] `spring.application.name` consistent everywhere.
- [ ] Config file in `demoConfigServer/.../config/<name>.properties` created/updated.
- [ ] Local config reduced to bootstrap.
- [ ] Dependencies: config, eureka, amqp (if Rabbit), openfeign (if REST sync).
- [ ] Gateway route: path → `lb://<name>`.
- [ ] Rabbit: producer/consumer + documented topology.
- [ ] Feign: `@EnableFeignClients` + interfaces matching real APIs.
- [ ] Tests: via gateway and direct service port.

---

## 7. Test Startup Order

1. `docker compose up -d` (RabbitMQ)
2. Eureka
3. Config Server
4. Business services (e.g., MSOrdonnance, MSLaboratoir)
5. API Gateway

---

## 8. Common Pitfalls

- Duplicate ports: fix in Config Server.
- Service name mismatch: config file name ≠ `spring.application.name`.
- `lb://` with no instance: service not started or wrong name.
- Missing queue: consumer started before beans declared.
- Incompatible JSON: use the same property names/types in producer and consumer.

---
=======
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
>>>>>>> 863bddf7f4b51f2da4fdfa6d25056051499b8424
