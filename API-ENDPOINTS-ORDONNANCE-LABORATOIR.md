# API Endpoints – MSOrdonnance

## Base path : `/ordonnances`

### 1. `GET /ordonnances`
- **Description** : Récupérer toutes les ordonnances.
- **Réponse** : Liste d’objets Ordonnance.

### 2. `GET /ordonnances/{id}/medicaments`
- **Description** : Récupérer les médicaments d’une ordonnance.
- **Réponse** : Liste de MedicamentDTO.

### 3. `POST /ordonnances/{id}/medicaments/{medicamentId}`
- **Description** : Ajouter un médicament à une ordonnance.
- **Réponse** : Message de succès ou d’erreur.

### 4. `GET /ordonnances/medicaments`
- **Description** : Récupérer tous les médicaments.
- **Réponse** : Liste de MedicamentDTO.

### 5. `GET /ordonnances/medicaments/{id}`
- **Description** : Récupérer un médicament par ID.
- **Réponse** : Objet MedicamentDTO.

### 6. `POST /ordonnances/analysis-requests`
- **Description** : Publier une demande d’analyse (RabbitMQ).
- **Body JSON** :
```json
{
  "patientId": 1,
  "medcinId": 2,
  "laboratoireId": 3,
  "type": "sang",
  "status": "pending"
}
```
- **Réponse** : Message de succès.

---

# API Endpoints – MSLaboratoir

## Base path : `/laboratoires`

### 1. `GET /laboratoires`
- **Description** : Récupérer tous les laboratoires.
- **Réponse** : Liste d’objets Laboratoire.

### 2. `POST /laboratoires/analysis-requests`
- **Description** : Publier une demande d’analyse (RabbitMQ).
- **Body JSON** :
```json
{
  "patientId": 1,
  "medcinId": 2,
  "laboratoireId": 3,
  "type": "sang",
  "status": "pending"
}
```
- **Réponse** : Message de succès.

### 3. `POST /laboratoires/analysis-completed`
- **Description** : Publier la complétion d’une analyse (RabbitMQ).
- **Body JSON** :
```json
{
  // Structure selon AnalysisCompletedMessage
}
```
- **Réponse** : Message de succès.

---

Pour plus de détails sur les bodies ou les réponses, consulte les DTO ou demande un exemple précis !
