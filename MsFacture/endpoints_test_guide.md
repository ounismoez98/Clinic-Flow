# MsFacture API Endpoints Test Guide

This file contains all the available endpoints in the **MsFacture** service for testing purposes, using the gateway address.

---

### 1. Create a Facture
- **Method:** `POST`
- **URL:** `http://localhost:8085/factures`
- **Body (JSON):**
```json
{
  "dateFacture": "2026-05-01",
  "patientId": 1,
  "statut": "NON_PAYEE",
  "tva": "TVA_20",
  "lignes": [
    {
      "description": "Consultation",
      "quantite": 1,
      "prix": 50.0
    },
    {
      "description": "Médicaments",
      "quantite": 2,
      "prix": 15.0
    }
  ]
}
```

### 2. Get All Factures
- **Method:** `GET`
- **URL:** `http://localhost:8085/factures`

### 3. Get Facture by ID
- **Method:** `GET`
- **URL:** `http://localhost:8085/factures/1` (Replace `1` with actual ID)

### 4. Delete Facture
- **Method:** `DELETE`
- **URL:** `http://localhost:8085/factures/1` (Replace `1` with actual ID)

### 5. Mark Facture as Paid
- **Method:** `PUT`
- **URL:** `http://localhost:8085/factures/1/payer` (Replace `1` with actual ID)

### 6. Get Factures by Patient ID
- **Method:** `GET`
- **URL:** `http://localhost:8085/factures/patient/1` (Replace `1` with Patient ID)

### 7. Get Factures by Status
- **Method:** `GET`
- **URL:** `http://localhost:8085/factures/statut/NON_PAYEE`
- **Statuses:** `NON_PAYEE`, `PAYEE`, `ANNULEE`

### 8. Search by Patient and Status
- **Method:** `GET`
- **URL:** `http://localhost:8085/factures/search?patientId=1&statut=NON_PAYEE`

### 9. Get PDF for Patient's Latest Facture
- **Method:** `GET`
- **URL:** `http://localhost:8085/factures/1/pdf` (Replace `1` with Patient ID)
- **Note:** Downloads the PDF of the most recent invoice for that patient.

### 10. Search Factures by Patient Name
- **Method:** `GET`
- **URL:** `http://localhost:8085/factures/search-by-patient?nom=Doe&prenom=John`

### 11. Count Invoices Created Today
- **Method:** `GET`
- **URL:** `http://localhost:8085/factures/count-today`

### 12. Count Invoices by Specific Date
- **Method:** `GET`
- **URL:** `http://localhost:8085/factures/count-by-date?date=2026-05-01`
