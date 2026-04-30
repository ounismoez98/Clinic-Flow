# MSCandidat - Usage Guide

This guide provides instructions on how to interact with the MSCandidat microservice, including REST API endpoints, H2 Database access, and Eureka Service Discovery.

## 1. REST API (Postman Guide)

The microservice runs on **port 8081**.

### Base URL: `http://localhost:8081/rendezvous`

| Feature | Method | Endpoint | Description |
| :--- | :--- | :--- | :--- |
| Health Check | `GET` | `/hello` | Verify if the service is up. |
| Get All | `GET` | `/getall` | Retrieve all appointments. |
| Get by ID | `GET` | `/rendezvousbyid/{id}` | Retrieve a specific appointment. |
| Add | `POST` | `/addrendezvous` | Create a new appointment. |
| Update | `PUT` | `/updaterendezvous/{id}` | Modify an existing appointment. |
| Delete | `DELETE` | `/deleterendezvous/{id}` | Remove an appointment. |

---

### Request Examples

#### Add a New Appointment
- **Method:** `POST`
- **URL:** `http://localhost:8081/rendezvous/addrendezvous`
- **Body (JSON):**
```json
{
    "date": "2026-05-20",
    "cause": "Checkup",
    "patient": "John Doe",
    "medcin": "Dr. House"
}
```

#### Update an Appointment
- **Method:** `PUT`
- **URL:** `http://localhost:8081/rendezvous/updaterendezvous/1`
- **Body (JSON):**
```json
{
    "date": "2026-05-21",
    "cause": "Emergency",
    "patient": "John Doe",
    "medcin": "Dr. Strange"
}
```

---

## 2. H2 Database Console

You can manage the database directly via the web interface.

- **URL:** `http://localhost:8081/h2`
- **JDBC URL:** Check application logs for the dynamic URL (e.g., `jdbc:h2:mem:xxxx`) or your persistent file path if configured.
- **User Name:** `Maroua`
- **Password:** (Empty)

---

## 3. Eureka Service Discovery

The service is configured as a Eureka Client.

- **Service Name:** `MSCandidat`
- **Eureka Server URL:** Typically `http://localhost:8761/eureka` (Check your Eureka Server configuration).
- **Status:** Automatically registers upon startup.

---

## 4. Actuator Endpoints

Basic monitoring is available:
- **Health:** `http://localhost:8081/actuator/health`
