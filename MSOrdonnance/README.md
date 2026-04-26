# MSOrdonnance

## Overview
MSOrdonnance is a Spring Boot microservice for managing medical prescriptions (ordonnances) and their associated medicaments. It is part of a distributed clinic system and communicates with other microservices (such as MSLaboratoir) using both REST and RabbitMQ for asynchronous messaging.

## Features
- CRUD operations for ordonnances
- Association of medicaments to ordonnances
- Publishes analysis requests to MSLaboratoir via RabbitMQ
- Consumes analysis completed events from MSLaboratoir via RabbitMQ
- Service discovery with Eureka
- Configuration with Spring Cloud Config

## Architecture
- **REST Endpoints**: Exposes endpoints for managing ordonnances and medicaments
- **RabbitMQ Messaging**: Uses RabbitMQ to send analysis requests and receive analysis completed events
- **Feign Clients**: (Optional) Can be extended to call other microservices synchronously

## RabbitMQ Integration
- **Exchange:** `analysis.exchange` (for requests), `analysis.completed.exchange` (for completions)
- **Queues:** `analysis.queue`, `analysis.completed.queue`
- **Routing Keys:** `analysis.created`, `analysis.completed`
- **Producer:** Sends `AnalysisRequestMessage` to lab
- **Consumer:** Receives `AnalysisCompletedMessage` from lab

## Endpoints
- `GET /ordonnances` — List all ordonnances
- `POST /ordonnances/analysis-requests` — Publish analysis request to lab (RabbitMQ)
- `POST /ordonnances/{id}/medicaments/{medicamentId}` — Add medicament to ordonnance
- (Other endpoints for CRUD and medicament management)

## How to Run
1. Start RabbitMQ: `docker compose up -d` from project root (see `docker-compose.yml`)
2. Start Eureka, Config Server, and all required microservices
3. Start MSOrdonnance (`mvn spring-boot:run` or from your IDE)
4. Use REST tools (Postman, curl) to interact with endpoints

## How to Test Messaging
- To send an analysis request: `POST /ordonnances/analysis-requests` with body:
  ```json
  {
    "patientId": 1,
    "medcinId": 2,
    "laboratoireId": 1,
    "type": "Blood Test",
    "status": "PENDING"
  }
  ```
- To simulate analysis completion, use the `/laboratoires/analysis-completed` endpoint in MSLaboratoir
- Check MSOrdonnance logs for received completion event

## Project Structure
- `OrdonnanceRestApi.java`: REST controller
- `OrdonnanceService.java`: Business logic
- `AnalysisMessageProducer.java`: Publishes analysis requests
- `AnalysisCompletedConsumer.java`: Consumes analysis completed events
- `AnalysisRequestMessage.java`, `AnalysisCompletedMessage.java`: Messaging DTOs

## Configuration
- RabbitMQ settings in `src/main/resources/application.properties`
- Eureka and Config Server settings included

## Extending
- Add Feign clients to call other microservices (e.g., patient, medecin)
- Add more event types or queues as needed
- Implement security (Spring Security, OAuth2) as required

## Authors
- Your Name / Team

## License
- Specify your license here
