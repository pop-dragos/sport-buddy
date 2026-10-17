# SportBuddy - Platformă pentru Meciuri Sportive Amatoare

Aplicație web creată pentru organizarea meciurilor amatoare de sport, gestionarea meciurilor, chat în timp real și sistem de rating.

## CERINȚE DE SISTEM
* **JDK 21** (Backend)
* **Node.js 20+** & npm (Frontend)
* **PostgreSQL** (Bază de date)

## CONFIGURARE BAZĂ DE DATE
1. Creează baza de date locală în PostgreSQL:
   `CREATE DATABASE sportbuddy;`
2. În folderul `backend/src/main/resources/`, creează fișierul `application-local.properties` cu datele tale locale:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/sportbuddy
   spring.datasource.username=postgres
   spring.datasource.password=parola_ta_locala