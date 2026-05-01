# Generic Plan: Microservice Integration (Config Server, Eureka, Gateway, Feign, RabbitMQ)

## 0. Principles (for every service)

| Element                | Rule                                                                 |
|------------------------|----------------------------------------------------------------------|
| Service Name           | Must match `spring.application.name` everywhere (Eureka, Feign, Gateway, Config file). |
| Config Server (native) | One file per service: `config/<spring.application.name>.properties` or `.yml`. |
| Local Bootstrap        | Local config only contains app name and Config Server import.        |
| RabbitMQ               | Use the same exchange and routing key names on both producer and consumer. Queue/binding usually declared on the consumer side. |
| Startup Order          | RabbitMQ (Docker) → Eureka → Config Server → Business Services → Gateway |

---

## 1. Config Server

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

---

## 3. API Gateway

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
