# Schema Entităților Bazei de Date (pe Module)

## 1. Modul Users & Auth (Persoana A)
* **User**:
  - `id` (Long, Primary Key)
  - `email` (String, Unique)
  - `password_hash` (String)
  - `full_name` (String)
  - `avatar_url` (String)
  - `created_at` (Timestamp)

## 2. Modul Lobbies & Catalog (Persoana B)
* **City**:
  - `id` (Long, Primary Key)
  - `name` (String)
  - `latitude` (Double)
  - `longitude` (Double)

* **Sport**:
  - `id` (Long, Primary Key)
  - `name` (String - ex: Fotbal, Baschet, Tenis)

* **Field**:
  - `id` (Long, Primary Key)
  - `name` (String)
  - `address` (String)
  - `city_id` (FK -> City)

* **Lobby (Meci)**:
  - `id` (Long, Primary Key)
  - `title` (String)
  - `sport_id` (FK -> Sport)
  - `field_id` (FK -> Field)
  - `max_players` (Integer)
  - `status` (Enum: OPEN, FULL, CANCELLED, COMPLETED)
  - `date_time` (LocalDateTime)
  - `created_by_user_id` (FK -> User)

* **LobbyParticipant**:
  - `id` (Long, Primary Key)
  - `lobby_id` (FK -> Lobby)
  - `user_id` (FK -> User)
  - `joined_at` (Timestamp)

## 3. Modul Chat, Ratings & Weather (Persoana C)
* **ChatMessage**:
  - `id` (Long, Primary Key)
  - `lobby_id` (FK -> Lobby)
  - `sender_id` (FK -> User)
  - `message` (Text)
  - `sent_at` (Timestamp)

* **UserRating**:
  - `id` (Long, Primary Key)
  - `evaluator_id` (FK -> User)
  - `evaluated_user_id` (FK -> User)
  - `rating_score` (Integer: 1-5)
  - `comment` (String)
  - `created_at` (Timestamp)