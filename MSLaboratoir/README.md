# MSLaboratoir

## Overview
MSLaboratoir is a Spring Boot microservice for managing laboratories and handling medical analysis requests. It is part of a distributed clinic system and communicates with other microservices (such as MSOrdonnance, MSPatientMedcin) using both REST (via OpenFeign) and RabbitMQ for asynchronous messaging.

## Features
- CRUD operations for laboratories
- Consumes analysis requests from MSOrdonnance via RabbitMQ
- Publishes analysis completed events to MSOrdonnance via RabbitMQ
- Calls Patient and Medecin microservices using OpenFeign
- Service discovery with Eureka
- Configuration with Spring Cloud Config

## Architecture
- **REST Endpoints**: Exposes endpoints for managing laboratories and testing messaging
- **RabbitMQ Messaging**: Uses RabbitMQ to receive analysis requests and send analysis completed events
- **Feign Clients**: Calls Patient and Medecin microservices for additional data

## RabbitMQ Integration
- **Consumes:** `AnalysisRequestMessage` from `analysis.queue` (exchange: `analysis.exchange`, routing key: `analysis.created`)
- **Publishes:** `AnalysisCompletedMessage` to `analysis.completed.exchange` (routing key: `analysis.completed`)

## Endpoints
- `GET /laboratoires` — List all laboratories
- `POST /laboratoires/analysis-requests` — Test publish analysis request (to self)
- `POST /laboratoires/analysis-completed` — Publish analysis completed event to MSOrdonnance
- (Other endpoints for CRUD)

## How to Run
1. Start RabbitMQ: `docker compose up -d` from project root (see `docker-compose.yml`)
2. Start Eureka, Config Server, and all required microservices
3. Start MSLaboratoir (`mvn spring-boot:run` or from your IDE)
4. Use REST tools (Postman, curl) to interact with endpoints

## How to Test Messaging
- To receive an analysis request, send a message from MSOrdonnance or use `/laboratoires/analysis-requests`
- To send an analysis completed event: `POST /laboratoires/analysis-completed` with body:
  ```json
  {
    "analysisId": 1,
    "ordonnanceId": 2,
    "patientId": 3,
    "medcinId": 4,
    "result": "Normal"
  }
  ```
- Check MSOrdonnance logs for received completion event

## Project Structure
- `LaboratoireRestApi.java`: REST controller
- `LaboratoireService.java`: Business logic
- `AnalysisMessageConsumer.java`: Consumes analysis requests
- `AnalysisCompletedProducer.java`: Publishes analysis completed events
- `PatientClient.java`, `MedecinClient.java`: Feign clients for other microservices
- `AnalysisRequestMessage.java`, `AnalysisCompletedMessage.java`: Messaging DTOs

## Configuration
- RabbitMQ settings in `src/main/resources/application.yaml`
- Eureka and Config Server settings included

## Extending
- Add more event types or queues as needed
- Implement security (Spring Security, OAuth2) as required

## Authors
- Your Name / Team

## License
- Specify your license here
