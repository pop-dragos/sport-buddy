# Documentație endpoint-uri (REST API)

**Base URL:** `http://localhost:8080/api`

## Convenții pentru tot proiectul

- **Autentificare**: antet `Authorization: Bearer <jwt>`. Fără token merg doar `POST /auth/**`, `GET /catalog/**` (pagina de cont nou are nevoie de sporturi și orașe) și `GET /health`. În mediul `dev` se poate folosi antetul `X-Dev-User: ana` în loc de token. Utilizatorul curent se ia în controller din `CurrentUser.id()`.
- **Date**: `2026-10-24T18:00:00` pentru dată cu oră, `2026-10-24` pentru zi, `2026-10` pentru lună. Fus orar `Europe/Bucharest`.
- **Bani**: numere întregi, în RON.
- **Id-uri**: `Long`.
- **Erori**: toate răspunsurile de eroare au forma

```json
{ "code": "LOBBY_FULL", "message": "Lobby-ul este complet." }
```

  La validare apare și `fields`, de exemplu `{ "code": "VALIDATION_ERROR", "message": "Date invalide.", "fields": { "age": "Vârsta minimă este 16." } }`.

| Cod HTTP | Când |
|---|---|
| 400 | date invalide (`VALIDATION_ERROR`) |
| 401 | lipsește sau a expirat token-ul (`UNAUTHORIZED`) |
| 402 | plată refuzată (`CARD_DECLINED`) |
| 403 | nu ai dreptul (`FORBIDDEN`, `NOT_HOST`, `NOT_PARTICIPANT`) |
| 404 | nu există (`NOT_FOUND`) |
| 409 | conflict de stare (`LOBBY_FULL`, `LOBBY_NOT_OPEN`, `JOIN_CLOSED` etc.) |

- **Module independente**: frontend-ul compune paginile din mai multe apeluri (de exemplu profilul = `/users/{id}` + `/ratings/users/{id}` + `/friends/status/{id}`), ca modulele să nu depindă circular unul de altul.

---

## 1. Auth, Users, Friends & Feed (Persoana A)

### Auth

| Metodă și cale | Rol |
|---|---|
| `POST /auth/register` | cont nou |
| `POST /auth/login` | conectare |
| `POST /auth/forgot-password` | „Ai uitat parola?" (simulat) |
| `POST /auth/social` | Google sau iCloud (simulat) |

`POST /auth/register`, cerere:
```json
{
  "fullName": "Ana Popescu",
  "email": "ana@exemplu.ro",
  "password": "parola123",
  "age": 21,
  "sports": [ { "sportId": 1, "level": "INTERMEDIATE" } ],
  "phone": "0722123456",
  "cityId": 4,
  "description": "Joc în weekend.",
  "authProvider": "LOCAL"
}
```
Obligatorii: `fullName`, `email`, `password` (minim 6 caractere, lipsește la `GOOGLE` și `ICLOUD`), `age` (minim 16), `sports` (cel puțin unul). `phone`, `cityId`, `description` sunt opționale. Poza se încarcă separat (vezi `POST /users/me/avatar`). Username-ul se generează automat.
Răspuns `201`: `{ "token": "...", "expiresAt": "2026-10-20T10:00:00", "user": <UserMe> }`.
Erori: `409 EMAIL_TAKEN`, `400 VALIDATION_ERROR`.

`POST /auth/login`, cerere: `{ "identifier": "ana", "password": "demo123", "rememberMe": true }` (`identifier` este username sau email).
Răspuns `200`: ca la înregistrare. Token valabil 1 zi, 30 de zile cu `rememberMe`. Eroare: `401 INVALID_CREDENTIALS`.

`POST /auth/forgot-password`, cerere: `{ "identifier": "ana@exemplu.ro" }`. Răspuns `202`: `{ "message": "Dacă există un cont, vei primi un email." }`. Nu trimite nimic în realitate și nu arată dacă contul există.

`POST /auth/social`, cerere: `{ "provider": "GOOGLE" }` (sau `ICLOUD`). Răspuns `200`, una dintre două forme:
- cont existent: `{ "token": "...", "user": <UserMe> }`
- cont inexistent: `{ "needsSignup": true, "profile": { "fullName": "...", "email": "..." } }`; frontend-ul completează restul câmpurilor și apelează `POST /auth/register` cu `authProvider` setat.

### Users

| Metodă și cale | Rol |
|---|---|
| `GET /users/me` | profilul meu (`UserMe`) |
| `PUT /users/me` | actualizare date profil |
| `PUT /users/me/sports` | înlocuiește lista de sporturi cu nivel |
| `POST /users/me/avatar` | încărcare poză (`multipart/form-data`, câmpul `file`) |
| `DELETE /users/me/avatar` | șterge poza |
| `PUT /users/me/password` | schimbare parolă |
| `DELETE /users/me` | șterge contul (devine inactiv și anonimizat) |
| `GET /users/me/settings` | setările mele |
| `PUT /users/me/settings` | salvează setările |
| `GET /users/{id}` | profil public al altui utilizator |
| `GET /users` | lista utilizatorilor demo (doar mediul `dev`) |

`UserMe`:
```json
{
  "id": 7, "username": "ana.popescu", "fullName": "Ana Popescu", "email": "ana@exemplu.ro",
  "phone": null, "age": 21, "avatarUrl": null, "cityId": 4, "description": null,
  "createdAt": "2026-10-19T12:00:00",
  "sports": [ { "sportId": 1, "level": "INTERMEDIATE" } ],
  "profileCompletion": { "percent": 40, "missing": ["phone", "avatar", "city", "description"] }
}
```

`PUT /users/me`, cerere (toate câmpurile opționale, se trimit doar cele schimbate): `{ "fullName", "email", "phone", "age", "cityId", "description" }`. Răspuns `200`: `UserMe`.

`PUT /users/me/sports`, cerere: `[ { "sportId": 1, "level": "BEGINNER" } ]`. Nivelurile: `BEGINNER`, `INTERMEDIATE`, `ADVANCED`. Un sport lipsit din listă este scos. Minim un sport.

`PUT /users/me/password`, cerere: `{ "currentPassword": "...", "newPassword": "..." }` (minim 6 caractere). Eroare: `400 WRONG_PASSWORD`.

`UserSettings` (răspuns la `GET`, cerere la `PUT`):
```json
{
  "theme": "SYSTEM", "showWeather": true,
  "notifyJoinRequests": true, "notifyMessages": true, "notifyReminder": true, "notifyWeeklyDigest": false,
  "profileVisibility": "EVERYONE", "whoCanAddMe": "EVERYONE", "showFriendsList": true
}
```
Nu există câmp pentru rating: ratingul nu se poate ascunde.

`GET /users/{id}` (profil public):
```json
{
  "id": 12, "username": "bogdan", "fullName": "Bogdan Pop", "avatarUrl": null,
  "cityId": 4, "description": "Joc seara.", "memberSince": "2026-03-01",
  "sports": [ { "sportId": 3, "level": "ADVANCED" } ], "restricted": false
}
```
Dacă utilizatorul are `profileVisibility = FRIENDS` și cel care se uită nu îi este prieten, răspunsul are `"restricted": true` și conține doar `id`, `username`, `fullName`, `avatarUrl` (restul câmpurilor sunt goale, `sports` este o listă goală).
Rating, fiabilitate și etichete vin din `GET /ratings/users/{id}` (C). Numărul de meciuri vine din `GET /lobbies/stats/{userId}` (B). Starea prieteniei vine din `GET /friends/status/{id}` (A, mai jos).

### Friends

| Metodă și cale | Rol |
|---|---|
| `GET /friends?query=` | lista prietenilor, cu căutare |
| `GET /friends/suggestions` | „Posibili cunoscuți", din lobby-urile mele |
| `GET /friends/requests` | cereri primite și trimise |
| `POST /friends/requests` | trimite cerere, corp `{ "userId": 12 }` |
| `POST /friends/requests/{id}/accept` | acceptă |
| `POST /friends/requests/{id}/reject` | refuză |
| `DELETE /friends/requests/{id}` | anulează o cerere trimisă |
| `DELETE /friends/{userId}` | șterge un prieten |
| `GET /friends/status/{userId}` | starea prieteniei cu un utilizator |
| `GET /friends/{userId}/messages?after={messageId}` | mesaje directe (interogare la 3 secunde) |
| `POST /friends/{userId}/messages` | trimite mesaj, corp `{ "message": "..." }` |

Element din lista de prieteni și din sugestii:
```json
{ "userId": 12, "username": "bogdan", "fullName": "Bogdan Pop", "avatarUrl": null,
  "cityName": "Baia Mare", "sportCodes": ["football", "tennis"], "matchesTogether": 3, "rating": 4.1 }
```
`rating` vine din `RatingService` (C), gol pentru utilizatorii noi. `matchesTogether` este gol până când `LobbyService` (B) oferă meciurile jucate împreună. Până atunci, sugestiile sunt jucători activi cu cel puțin un sport comun (cei din orașul meu primii), fără prietenii și cererile în așteptare.
`GET /friends/requests` răspunde `{ "incoming": [ { "requestId": 5, "user": {...}, "createdAt": "..." } ], "outgoing": [ ... ] }` (`user` are forma de mai sus).
`GET /friends/status/{userId}` răspunde `{ "status": "PENDING_SENT", "requestId": 5 }`, valori: `NONE`, `PENDING_SENT`, `PENDING_RECEIVED`, `FRIENDS`. `requestId` apare doar la cele două stări `PENDING_*`, ca profilul să poată anula sau accepta cererea.
`POST /friends/requests` răspunde `201` cu aceeași formă ca `status`: `PENDING_SENT` (cerere nouă) sau `FRIENDS` (dacă celălalt îmi trimisese deja o cerere, a mea o acceptă). O cerere refuzată mai demult nu blochează una nouă.
`accept`, `reject`, `DELETE /friends/requests/{id}` și `DELETE /friends/{userId}` răspund `204`. Doar destinatarul acceptă sau refuză, doar expeditorul anulează (altfel `404`); o cerere deja rezolvată dă `409 REQUEST_NOT_PENDING`.
Mesaj direct: `{ "id": 90, "senderId": 7, "message": "Salut", "sentAt": "2026-10-19T12:00:00" }`. Fără `after` vin ultimele 100 de mesaje, în ordine cronologică; cu `after` doar mesajele cu id mai mare. Citirea marchează mesajele primite ca citite. Mesaje se pot trimite doar între prieteni (`403 NOT_FRIENDS`).
Erori la cerere de prietenie: `400` (cerere către tine), `404` (utilizator inexistent sau șters), `409 ALREADY_FRIENDS`, `409 REQUEST_EXISTS`, `403 FRIEND_REQUESTS_DISABLED` (conform setării `whoCanAddMe`: `NOBODY`, sau `FRIENDS_OF_FRIENDS` fără niciun prieten comun).

### Feed

| Metodă și cale | Rol |
|---|---|
| `GET /feed?sportId=&page=0&size=20` | postările comunității, filtru opțional pe sport |
| `POST /feed` | postare nouă |
| `POST /feed/{id}/like` | like sau scoate like (comutator) |
| `GET /feed/{id}/comments` | comentariile unei postări |
| `POST /feed/{id}/comments` | adaugă comentariu, corp `{ "content": "..." }` |

`POST /feed`, cerere: `{ "content": "Căutăm doi jucători.", "sportId": 1, "tag": "LOOKING_FOR_PLAYERS" }`. Valori `tag`: `TOURNAMENT`, `DISCUSSION`, `LOOKING_FOR_PLAYERS`. Când filtrul de pe pagină este pe un sport, frontend-ul trimite acel `sportId`.
Postare în răspuns:
```json
{
  "id": 31, "author": { "id": 7, "username": "ana.popescu", "avatarUrl": null },
  "sportId": 1, "tag": "DISCUSSION", "content": "...", "createdAt": "2026-10-19T12:00:00",
  "likeCount": 4, "likedByMe": false, "commentCount": 2
}
```
`POST /feed/{id}/like` răspunde `{ "liked": true, "likeCount": 5 }`.

---

## 2. Catalog, Lobbies & Payments (Persoana B)

### Catalog (public, doar citire)

| Metodă și cale | Rol |
|---|---|
| `GET /catalog/sports` | cele 12 sporturi |
| `GET /catalog/cities?query=` | orașele; cu `query` filtrează după nume (pentru sugestii, maxim 20) |
| `GET /catalog/fields?cityId=&sportId=` | terenurile unui oraș, filtrate opțional după sport |

Răspunsuri:
```json
[ { "id": 1, "code": "football", "name": "Fotbal", "defaultCapacity": 10, "venueLabel": "Teren de fotbal", "indoor": false } ]
[ { "id": 4, "name": "Satu Mare", "latitude": 47.79, "longitude": 22.88 } ]
[ { "id": 10, "name": "Teren de fotbal – Centru", "district": "Centru", "cityId": 4, "sportId": 1 } ]
```

### Lobbies

| Metodă și cale | Rol |
|---|---|
| `GET /lobbies?cityId=&sportId=&date=` | lobby-urile unei zile (lista din fereastra calendar) |
| `GET /lobbies/calendar?cityId=&sportId=&month=2026-10` | zilele lunii care au lobby-uri (punctul de pe calendar) |
| `GET /lobbies/{id}` | detaliile unui lobby |
| `POST /lobbies` | creare lobby |
| `POST /lobbies/{id}/join-requests` | cerere de alăturare |
| `POST /lobbies/{id}/join-requests/{requestId}/accept` | acceptă cererea (doar organizatorul) |
| `POST /lobbies/{id}/join-requests/{requestId}/reject` | refuză cererea (doar organizatorul) |
| `POST /lobbies/{id}/leave` | părăsește lobby-ul |
| `POST /lobbies/{id}/lock` | confirmă lobby-ul (doar organizatorul) |
| `POST /lobbies/{id}/cancel` | anulează lobby-ul (doar organizatorul) |
| `POST /lobbies/{id}/attendance` | salvează prezența (doar organizatorul) |
| `GET /lobbies/mine?filter=` | agenda mea |
| `GET /lobbies/mine/todo` | contoarele pentru „De făcut" |
| `GET /lobbies/by-user/{userId}?limit=3` | meciurile unui utilizator, pentru profil |
| `GET /lobbies/stats/{userId}` | statistici pentru profil |

`LobbySummary` (element în liste):
```json
{
  "id": 55, "sportId": 1, "sportCode": "football", "sportName": "Fotbal",
  "fieldId": 10, "fieldName": "Sala Polivalentă", "cityId": 4, "cityName": "Satu Mare",
  "dateTime": "2026-10-24T18:00:00", "status": "OPEN",
  "maxPlayers": 10, "currentPlayers": 6, "freeSlots": 4,
  "averageRating": 3.8, "myState": "NONE"
}
```
Câmpuri în plus pentru agendă: `"isHost": true, "pendingRequests": 2` (cererile în așteptare, completat doar pentru organizator).
`averageRating` este nivelul mediu al participanților (gol dacă nimeni nu are rating). `myState`: `NONE`, `PENDING`, `ACCEPTED`, `REJECTED`, `LEFT` (starea mea în lobby).

`GET /lobbies/calendar` răspunde `{ "days": ["2026-10-21", "2026-10-24"] }`.

`POST /lobbies`, cerere:
```json
{
  "sportId": 1, "fieldId": 10, "dateTime": "2026-10-24T18:00:00",
  "maxPlayers": 10, "minPlayers": 6, "alreadyPlaying": 3, "fieldCost": 200
}
```
- `alreadyPlaying` = câți jucători sunt deja, **cu organizatorul** (minim 1). Serverul salvează `broughtPlayers = alreadyPlaying - 1`.
- Orașul se deduce din teren.
- Reguli: `dateTime` cel puțin 3 ore în viitor; ora între 0 și 23, minute multiplu de 15; `2 <= maxPlayers`; `2 <= minPlayers <= maxPlayers`; `1 <= alreadyPlaying <= maxPlayers`; `fieldCost >= 0`; terenul trebuie să fie pentru sportul ales.
- Răspuns `201`: `LobbyDetails`. Erori: `400 VALIDATION_ERROR`, `400 INVALID_DATE_TIME`, `400 SPORT_FIELD_MISMATCH`.

`LobbyDetails`:
```json
{
  "id": 55,
  "sport": { "id": 1, "code": "football", "name": "Fotbal" },
  "field": { "id": 10, "name": "Teren de fotbal – Centru", "district": "Centru" },
  "city": { "id": 4, "name": "Satu Mare" },
  "dateTime": "2026-10-24T18:00:00",
  "status": "OPEN",
  "host": { "id": 7, "username": "ana.popescu" },
  "maxPlayers": 10, "minPlayers": 6, "broughtPlayers": 2,
  "currentPlayers": 6, "freeSlots": 4,
  "averageRating": 3.8,
  "fieldCost": 200,
  "confirmDeadline": "2026-10-24T12:00:00",
  "estimatedPrice": 34, "priceAtFull": 20, "finalPrice": null,
  "minNotReached": false, "joinClosed": false, "attendanceSaved": false,
  "myState": "ACCEPTED", "isHost": false,
  "participants": [
    { "userId": 7, "username": "ana.popescu", "isHost": true,
      "rating": 4.1, "reviewCount": 10, "reliabilityPercent": 100, "paymentMethod": null, "attended": null }
  ],
  "pendingRequests": []
}
```
- `estimatedPrice` și `priceAtFull` se completează doar cât e `OPEN`; `finalPrice` doar după confirmare. Cu `fieldCost = 0` toate trei sunt goale (lobby gratuit).
- `participants` conține doar jucătorii cu cont și statusul `ACCEPTED`. Jucătorii fără cont apar prin `broughtPlayers` (interfața afișează „Adus de organizator, fără cont"). `rating`, `reviewCount`, `reliabilityPercent` vin de la `RatingService` (C) și sunt goale pentru utilizatorii noi. `paymentMethod` este `ONLINE` sau `ON_SITE` după confirmare, altfel gol.
- `pendingRequests` este completat doar pentru organizator: `{ "requestId": 9, "userId": 21, "username": "...", "rating": 3.9, "reliabilityPercent": 92 }`.
- `attendanceSaved` este true după ce organizatorul a salvat prezența. `attended` (la fiecare participant) este gol până atunci, apoi true (a venit) sau false (absent).
- `minNotReached` este true dacă termenul a trecut și minimul nu e atins. `joinClosed` este true când nu se mai pot trimite cereri (lobby plin, confirmat, anulat sau termen depășit).

Acțiuni:
- `POST .../join-requests` răspunde `201` cu `{ "requestId": 9, "status": "PENDING" }`. Erori: `409 LOBBY_FULL`, `409 LOBBY_NOT_OPEN`, `409 JOIN_CLOSED` (termen depășit), `409 ALREADY_REQUESTED`.
- `accept` și `reject` răspund `200` cu `LobbyDetails`. Eroare: `409 LOBBY_FULL` la `accept` dacă nu mai e loc, `403 NOT_HOST`.
- `leave` răspunde `200` cu `{ "lateCancellation": false }`. Dacă lobby-ul era confirmat, este `true` (penalizare). Organizatorul nu poate folosi `leave`, ci `cancel` (`403 HOST_CANNOT_LEAVE`).
- `lock` răspunde `200` cu `LobbyDetails` (starea `LOCKED`, `finalPrice` setat). Eroare: `409 LOBBY_NOT_OPEN`.
- `cancel` răspunde `200` cu `{ "lateCancellation": false }` (true dacă era confirmat, caz în care organizatorul primește penalizare).
- `attendance`, cerere (se trimite doar după ce lobby-ul este `FINISHED`, o singură dată):
```json
{ "attendance": [ { "userId": 21, "present": true }, { "userId": 33, "present": false } ] }
```
  Răspuns `200`. Erori: `409 LOBBY_NOT_FINISHED`, `409 ATTENDANCE_ALREADY_SAVED`.

`GET /lobbies/mine?filter=` cu `filter` din `UPCOMING` (viitoare), `CREATED` (create de mine), `REQUESTS` (cereri trimise), `FINISHED` (încheiate). Răspunde cu o listă de `LobbySummary`.

`GET /lobbies/mine/todo`:
```json
{ "pendingRequests": 2, "attendanceToSave": 1, "reviewsToGive": 1, "paymentsToMake": 0, "total": 4 }
```
`total` este numărul din bara laterală de lângă „Agenda mea". `reviewsToGive` îl ia `LobbyService` de la `RatingService` (C).

`GET /lobbies/by-user/{userId}?limit=3`: pentru profilul meu întoarce meciurile mele recente, pentru alt utilizator doar meciurile jucate **împreună**. Listă de `LobbySummary`.
`GET /lobbies/stats/{userId}` răspunde `{ "matchesPlayed": 14, "matchesTogether": 3 }` (`matchesTogether` este gol pentru propriul profil).

### Payments

| Metodă și cale | Rol |
|---|---|
| `POST /payments` | plată online simulată |
| `GET /payments/history` | istoricul plăților mele (pagina Plăți din Setări) |

`POST /payments`, cerere:
```json
{ "lobbyId": 55, "cardNumber": "4242 4242 4242 4242", "expiry": "12/29", "cvc": "123" }
```
- Răspuns `201`: `{ "paymentId": 3, "status": "SUCCESS", "amount": 34 }`.
- Cardul `4242 4242 4242 4242` trece, `4000 0000 0000 0002` este refuzat (`402 CARD_DECLINED`, se salvează cu status `DECLINED`).
- Reguli: lobby-ul este `LOCKED`, eu sunt participant `ACCEPTED`, are preț final mai mare ca 0, nu am plătit deja. Erori: `409 LOBBY_NOT_LOCKED`, `409 ALREADY_PAID`, `403 NOT_PARTICIPANT`.
- Datele cardului nu se salvează.

`GET /payments/history` răspunde:
```json
[ { "paymentId": 3, "lobbyId": 55, "sportName": "Fotbal", "fieldName": "Sala Polivalentă",
    "dateTime": "2026-10-24T18:00:00", "amount": 34, "status": "SUCCESS", "createdAt": "2026-10-23T09:10:00" } ]
```

### Simulare (doar profilul `dev`)

Pentru panoul de simulare din pagina lobby-ului (vizibil doar organizatorului, ca în prototip). Toate răspund cu `LobbyDetails`.

| Metodă și cale | Rol |
|---|---|
| `POST /dev/lobbies/{id}/simulate-join` | un jucător demo se înscrie direct |
| `POST /dev/lobbies/{id}/simulate-cancel` | jucătorul din corpul `{ "userId": 21 }` anulează |
| `POST /dev/lobbies/{id}/pass-deadline` | trece termenul de confirmare (rulează logica jobului) |
| `POST /dev/lobbies/{id}/finish` | încheie meciul |

---

## 3. Chat, Ratings, Weather & Dashboard (Persoana C)

### Weather

`GET /weather?cityId=4&days=5` (implicit `days = 5`):
```json
{
  "cityId": 4,
  "days": [
    { "date": "2026-10-19", "condition": "RAIN", "tempMin": 7, "tempMax": 13, "rainChancePercent": 80 }
  ],
  "recommendation": "Plouă. Mai bine baschet sau volei în sală."
}
```
`condition`: `SUNNY`, `PARTLY_CLOUDY`, `CLOUDY`, `RAIN`. Coordonatele orașului le ia `WeatherService` din `CatalogService`. La început endpoint-ul întoarce date fixe, apoi apelează Open-Meteo. Pagina lobby-ului alege din listă ziua meciului.

### Chat

| Metodă și cale | Rol |
|---|---|
| `GET /lobbies/{id}/messages?after={messageId}` | mesajele chat-ului (interogare la 3 secunde) |
| `POST /lobbies/{id}/messages` | trimite mesaj, corp `{ "message": "..." }` |

Mesaj:
```json
{ "id": 120, "type": "USER", "senderId": 7, "senderUsername": "ana.popescu",
  "message": "Ne vedem la 18.", "sentAt": "2026-10-19T12:00:00" }
```
`type` este `USER` sau `SYSTEM` (mesaje precum „X s-a înscris" și „Lobby confirmat", cu `senderId` gol). `after` întoarce doar mesajele cu id mai mare. Doar participanții `ACCEPTED` pot citi și scrie (`403 NOT_PARTICIPANT`).

### Ratings

| Metodă și cale | Rol |
|---|---|
| `GET /lobbies/{id}/reviews/pending` | colegii pe care încă nu i-am notat |
| `POST /lobbies/{id}/reviews` | trimite toate notele după meci |
| `GET /ratings/users/{userId}` | rating, fiabilitate și etichete ale unui utilizator |

`GET /lobbies/{id}/reviews/pending` răspunde `[ { "userId": 21, "username": "bogdan" } ]` (fără jucătorii fără cont, fără mine).

`POST /lobbies/{id}/reviews`, cerere:
```json
{ "reviews": [ { "userId": 21, "score": 4 }, { "userId": 33, "score": 5 } ] }
```
Răspuns `201`. Reguli: lobby-ul este `FINISHED`, eu am fost participant, nota este între 1 și 5, nu mă notez pe mine, o singură trimitere pe lobby. Erori: `409 LOBBY_NOT_FINISHED`, `409 ALREADY_REVIEWED`, `403 NOT_PARTICIPANT`.

`GET /ratings/users/{userId}`:
```json
{ "userId": 21, "average": 4.2, "reviewCount": 12, "reliabilityPercent": 94, "tags": ["Punctual", "Fair-play"] }
```
Pentru utilizatori noi `average` și `reliabilityPercent` sunt goale, iar `tags` este o listă goală (interfața afișează „nou" și „Apar după primele review-uri").

### Dashboard

`GET /dashboard/home?cityId=4`:
```json
{
  "nextMatch": { "...": "un LobbySummary sau null" },
  "todo": { "pendingRequests": 2, "attendanceToSave": 1, "reviewsToGive": 1, "paymentsToMake": 0, "total": 4 },
  "recommended": [ { "...": "LobbySummary" } ],
  "topPlayers": [ { "userId": 21, "username": "bogdan", "avatarUrl": null, "average": 4.8, "reviewCount": 9 } ],
  "profileCompletion": { "percent": 70, "missing": ["avatar", "description"] }
}
```
- `recommended`: lobby-uri deschise din orașul ales, la nivelul meu.
- `topPlayers`: cei mai bine cotați jucători, cu cel puțin 5 review-uri.
- Vremea (`/weather`) și feed-ul (`/feed`) se cer separat, de frontend.

---

## 4. Evenimente între module (`common/events`)

Publicate de B (`lobbies`), ascultate de C (`ratings` și `chat`). Clasele se creează o singură dată, într-un Pull Request aprobat de toți, înainte ca B și C să le folosească.

| Eveniment | Câmpuri | Ce face C |
|---|---|---|
| `LobbyLockedEvent` | `lobbyId`, `finalPrice`, `participantUserIds` | mesaj de sistem „Lobby confirmat" în chat |
| `PlayerCancelledEvent` | `lobbyId`, `userId`, `late` | dacă `late`: review automat de 2 stele (`LATE_CANCEL`), crește `late_cancellations` |
| `LobbyCancelledEvent` | `lobbyId`, `hostUserId`, `late` | dacă `late`: review automat de 2 stele pentru organizator |
| `AttendanceSavedEvent` | `lobbyId`, `hostUserId`, `presentUserIds`, `absentUserIds` | crește `attendances` pentru prezenți (și organizator), review automat de 1 stea (`NO_SHOW`) pentru absenți |

**Propunere nouă (necesită acordul grupului):** `PlayerJoinedEvent` cu `lobbyId` și `userId`, publicat când organizatorul acceptă o cerere. Fără el, chat-ul nu poate afișa mesajul de sistem „X s-a înscris", cerut în brief.

---

## 5. Dependențe între module (apeluri service către service)

| Cine apelează | Ce apelează | Pentru |
|---|---|---|
| `lobbies` (B) | `UserService` (A) | username și nume pentru participanți |
| `lobbies` (B) | `RatingService.getStats(userId)` (C) | rating, număr de review-uri, fiabilitate |
| `chat` (C) | `LobbyService.isParticipant(lobbyId, userId)` (B) | cine are voie în chat |
| `ratings` (C) | `LobbyService` (B) | participanții unui lobby încheiat, pentru review-uri |
| `weather` (C) | `CatalogService` (B) | coordonatele orașului |
| `friends` (A) | `LobbyService` (B) | „Posibili cunoscuți" și „meciuri împreună" |
| `dashboard` (C) | `LobbyService` (B), `RatingService` (C), `UserService` (A) | agregare |

Apelurile merg doar între service-uri, niciodată către `model` sau `repository` din alt modul.
