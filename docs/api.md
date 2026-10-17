# Documentație Endpoints (REST API)

**Base URL:** `http://localhost:8080/api`

---

## 1. Auth & Users (Persoana A)
* `POST /api/auth/register` - Înregistrare utilizator nou
* `POST /api/auth/login` - Autentificare și returnare token JWT
* `GET /api/users/profile` - Preluare date profil utilizator logat
* `PUT /api/users/profile` - Actualizare profil utilizator

---

## 2. Lobbies & Catalog (Persoana B)
* `GET /api/catalog/cities` - Lista orașelor disponibile
* `GET /api/catalog/sports` - Lista sporturilor
* `GET /api/catalog/fields` - Lista terenurilor pe orașe
* `GET /api/lobbies` - Filtrare și listare meciuri disponibile
* `GET /api/lobbies/{id}` - Detalii meci specific
* `POST /api/lobbies` - Creare meci nou (Lobby)
* `POST /api/lobbies/{id}/join` - Înscriere la un meci
* `POST /api/lobbies/{id}/leave` - Părăsire meci

---

## 3. Chat, Ratings & Weather (Persoana C)
* `GET /api/weather?city={cityName}` - Prognoza meteo pentru un oraș (integrare Open-Meteo)
* `GET /api/lobbies/{id}/messages` - Preluare mesaje din chat-ul meciului
* `POST /api/lobbies/{id}/messages` - Trimitere mesaj în chat
* `POST /api/ratings` - Lăsare rating/review unui jucător
* `GET /api/users/{id}/ratings` - Lista de rating-uri primite de un jucător