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
## PORNIRE CU DOCKER (recomandat)
Necesită doar Docker Desktop, fără JDK, Node sau PostgreSQL instalate.
1. Copiază `.env.example` în `.env` și completează parola bazei de date și secretul JWT (și, opțional, emailul și Google).
2. Din rădăcina proiectului: `docker compose up --build`
3. Aplicația: http://localhost:5173 (API-ul rulează pe http://localhost:8080).
4. Oprire: `docker compose down` (datele rămân). Ștergerea datelor: `docker compose down -v`.

Pornirea manuală (cele două terminale) rămâne posibilă, conform pașilor de mai sus.
