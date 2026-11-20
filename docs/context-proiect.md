# SportBuddy: brief de context

Document de pornire pentru fiecare membru al echipei și pentru conversațiile cu un asistent AI. Când deschizi un chat nou, lipești tot documentul și atașezi `docs/entitati.md`, `docs/api.md` și prototipul `docs/prototip/sportbuddy.html`. Apoi scrii ce funcționalitate faci (șabloanele de mesaj sunt la finalul documentului).

---

## 1. Ce este aplicația

SportBuddy este o aplicație web prin care oamenii găsesc parteneri pentru sporturi de echipă și individuale. Un utilizator alege orașul și sportul, vede pe calendar la ce terenuri se joacă în ziua respectivă, câți jucători sunt deja și de câți mai e nevoie, și cere să se alăture. Organizatorul (gazda) aprobă cererile. După meci, participanții își dau note, iar notele (rating) apar data viitoare, înainte ca cineva să se alăture unui lobby, ca fiecare să știe cu cine joacă.

Este un proiect de facultate (Ingineria Software, anul 3, UTCN CTI), făcut de trei persoane. Rulează doar pe localhost. Scopul este o aplicație care arată și se comportă ca una reală, cu arhitectură clară care poate fi explicată la evaluare. Plata, autentificarea cu Google/iCloud și datele meteo sunt parțial simulate.

Termeni folosiți în tot proiectul:
- **Lobby**: un eveniment sportiv (sport, oraș, teren, dată, oră, număr de locuri) la care se pot înscrie jucători.
- **Gazdă / organizator**: cel care creează lobby-ul.
- **Confirmare (lock)**: momentul în care lobby-ul se blochează și prețul devine final.
- **Jucători aduși de organizator**: oameni fără cont, pe care gazda îi numără în lobby ca să caute doar completarea locurilor.

## 2. Ce trebuie să facă aplicația

**Cont și profil**
- Conectare cu utilizator sau email și parolă, cu „Ține-mă minte" și „Ai uitat parola?". Conectare simulată cu Google și iCloud.
- Cont nou cu câmpuri **obligatorii**: nume complet, email, parolă (minim 6 caractere), vârstă (minim 16 ani), cel puțin un sport. **Opționale**, completabile ulterior din setări: telefon, poză, oraș, descriere.
- Profil cu rating cu stele, statistici, sporturi cu nivel declarat, etichete de la colegi. Pe prima pagină apare un card cu procentul de completare a profilului.
- Setări pe secțiuni: Cont, Aspect (temă și widget de vreme), Notificări, Confidențialitate, Plăți. **Ratingul nu se poate ascunde**.

**Orașe, sporturi, lobby-uri**
- Căutare de oraș în header (câmp cu sugestii, cu toate orașele României). Orașul ales filtrează calendarul, recomandările și vremea.
- 12 sporturi: Fotbal, Baschet, Tenis, Padel, Volei, Handbal, Badminton, Tenis de masă, Squash, Alergare, Ciclism, Înot.
- Click pe un sport deschide un calendar lunar (zilele cu lobby-uri au un punct). Alegi o zi și vezi lobby-urile ei: ora, terenul, locuri libere, nivel mediu.
- Butonul „+" din header creează un lobby: sport, oraș, dată (cu buton de calendar), ora (0–23, minute din 15 în 15), teren (dropdown cu terenurile orașului), număr de locuri, **cost teren**, **număr minim de jucători**, **câți jucători sunt deja** (cu organizatorul).
- Cerere de alăturare către gazdă, care o acceptă sau o refuză. Lista de participanți arată **ratingul și fiabilitatea** fiecăruia înainte de decizie.
- Agenda mea: Viitoare, Create de mine, Cereri trimise, Încheiate.

**Confirmare, anulări, plată (regulile de business)**
- Costul terenului îl plătește gazda. Cât timp lobby-ul e deschis, aplicația arată un **preț estimat** (cost împărțit la jucătorii de acum) și prețul cu lobby-ul plin.
- **Termen de confirmare**: meci peste 3 zile sau mai mult → cu 12 ore înainte; între 1 și 3 zile → cu 6 ore; mai puțin de o zi → cu 2 ore. La termen (sau când confirmă gazda) prețul devine final și nu mai intră jucători. Dacă nu s-a atins minimul, gazda poate anula fără penalizare.
- Plata se face la fața locului. După confirmare există și plată online simulată (card de test `4242 4242 4242 4242` trece, `4000 0000 0000 0002` e refuzat), cu istoric în Setări.
- Anulare cât e deschis: fără penalizare. Anulare după confirmare: review automat de 2 stele pentru cel care anulează. Absență fără anunț (bifată de gazdă după meci): review automat de 1 stea. Gazda care anulează târziu e penalizată la fel.
- Formula: `mediaNouă = (media * n + v) / (n + 1)`, unde `n` este numărul de review-uri și `v` valoarea penalizării. Fiabilitate: `prezențe / (prezențe + 0.5 * anulăriTârzii + absențe)`.
- După meci, fiecare participant dă note de 1 la 5 stele colegilor (jucătorii fără cont nu primesc note).

**Social**
- Prieteni: cerere de prietenie, listă cu oraș, sporturi și meciuri jucate împreună, căutare, sugestii din lobby-urile tale, mesaje directe într-o fereastră separată.
- Feed pe prima pagină: postări (Turneu, Discuție, Căutăm jucători), like-uri, comentarii, filtru pe sport. Când filtrul e pe un sport, postarea nouă aparține acelui sport.
- Chat în fiecare lobby, vizibil doar participanților, cu mesaje de sistem („X s-a înscris", „Lobby confirmat").
- Vremea: prognoza pe 5 zile pentru orașul ales și o recomandare (de exemplu „mai bine baschet sau volei în sală" dacă plouă).

**Pagina de start**: widget-uri (următorul meci, de făcut, vreme, recomandate pentru nivelul tău, top jucători) și feed-ul.

## 3. Cum trebuie să arate (design)

Prototipul `docs/prototip/sportbuddy.html` este **referința vizuală și de comportament**: deschide-l în browser și respectă-l. Dacă o pagină din frontend diferă de prototip, prototipul are dreptate, cu excepția cazului în care echipa a decis altfel.

**Principii**
- Aspect curat și clasic: alb și negru ca bază, cu **accente de culoare puse punctual**. Nu folosiți degrade puternice și nu inventați alte culori.
- Aplicație cu temă luminoasă și întunecată (după setările sistemului sau alegerea din setări).
- Fără emoji, fără puncte de exclamare în texte. Ton simplu și direct, în română (de exemplu „Nu ai nimic în această listă.").
- Fontul este **Inter** (greutăți 400, 500, 600, 700), încărcat din Google Fonts. Mărimea de bază 14 px, `line-height` 1,45. Titlurile de pagină 17 px, titlul ecranului de conectare 22 px.
- Iconițele sunt liniare și simple (SVG, `stroke="currentColor"`, grosime 1,8), 18 × 18 px.

**Variabile de culoare** (se pun în `frontend/src/shared/styles.css`)

```css
:root {
  --bg: #f6f6f4; --sf: #ececea; --tx: #111; --mu: #6b6b6b; --bd: #dfdfdb;   /* alb puțin mai blând decât #fff */
  --ac: #111; --at: #fff;           /* butoane principale: negru pe alb */
  --card: rgba(252, 252, 250, 0.72); --card-bd: rgba(17, 17, 17, 0.08);       /* carduri translucide */
}
@media (prefers-color-scheme: dark) {
  :root:not([data-theme="light"]) {
    --bg: #0d0d0d; --sf: #181818; --tx: #f2f2f2; --mu: #9a9a9a; --bd: #2a2a2a;
    --ac: #f2f2f2; --at: #111;
    --card: rgba(24, 24, 24, 0.62); --card-bd: rgba(255, 255, 255, 0.08);
  }
}
:root[data-theme="dark"] {
  --bg: #0d0d0d; --sf: #181818; --tx: #f2f2f2; --mu: #9a9a9a; --bd: #2a2a2a;
  --ac: #f2f2f2; --at: #111;
  --card: rgba(24, 24, 24, 0.62); --card-bd: rgba(255, 255, 255, 0.08);
}
```

**Culori de accent**

| Folosire | Culoare |
|---|---|
| albastru principal (buton „+", element activ din meniu, chip-uri selectate, linkuri) | `#2563eb` (text activ în meniu `#3b82f6`) |
| avatar | degrade `#2563eb` → `#7c3aed` |
| stele de rating | `#f59e0b` |
| bara de progres, comutatoare active | `#16a34a` |
| fundal ecran de conectare | lumini difuze albastră și portocalie (`rgba(37,99,235,.22)` și `rgba(234,88,12,.2)`) peste `--bg` |
| fundal zonă principală | degrade foarte discret albastru → portocaliu cu transparență ~5–7% |

**Culori pe sporturi** (doar pentru iconițe): Fotbal `#16a34a`, Baschet `#ea580c`, Tenis `#ca8a04`, Padel `#0891b2`, Volei `#db2777`, Handbal `#7c3aed`, Badminton `#0d9488`, Tenis de masă `#dc2626`, Squash `#2563eb`, Alergare `#65a30d`, Ciclism `#4f46e5`, Înot `#0284c7`.

**Etichete colorate** (fundal / text): Turneu `#fef3c7` / `#92400e`; Discuție `#dbeafe` / `#1e40af`; Căutăm jucători `#dcfce7` / `#166534`; lobby Deschis `#dcfce7` / `#166534`; lobby Confirmat `#dbeafe` / `#1e40af`; Încheiat: gri neutru.

**Vreme** (iconițe SVG colorate): soare galben `#f59e0b`, nor gri `#94a3b8`, ploaie = nor gri cu picături albastre `#3b82f6`; soare cu nor pentru zilele cu risc mediu.

**Forme**: carduri translucide (fundal `--card`, blur 8 px) cu chenar de 1 px `--card-bd`, colțuri rotunjite 12 px, padding 14 px. Butoane și câmpuri 8 px. Chip-urile și etichetele complet rotunjite. Ferestre modale 14 px, cu fundal întunecat semitransparent. Cardul de conectare 16 px, cu umbră ușoară. Notificările scurte (toast) apar jos, centrat.

**Layout general**
- **Bară laterală** (190 px, 64 px pe ecrane înguste, fundal `--sf`): sigla „SportBuddy" cu „S" albastru, apoi Acasă, Agenda mea (cu un număr pentru ce ai de făcut), Prieteni, eticheta SPORTURI și cele 12 sporturi cu iconițe colorate. Jos, în colț: avatarul și **username-ul** (nu emailul, tăiat cu „…" dacă e lung). Click deschide un meniu cu „Profilul meu", „Setări", „Ieși din cont".
- **Header**: titlul paginii în stânga; în dreapta câmpul de căutare a orașului (cu sugestii) și butonul rotund albastru „+" pentru crearea unui lobby.
- **Pagina de start**: două coloane. Stânga: salut, cardul de completare a profilului (dacă e cazul), „Următorul meci" și „De făcut", vremea, recomandările, topul jucătorilor. Dreapta: comunitatea (feed). Celelalte pagini sunt centrate, cu lățime maximă ~960 px (containerul `.page` din `Layout.jsx`); pagina unui lobby are ~1100 px și două coloane pe ecran lat (detalii și acțiuni în stânga, participanți și chat în dreapta).

**Ecranele** (fiecare există în prototip)
1. **Conectare / Cont nou**: card centrat. Titlu, câmp „Utilizator sau email", „Parolă" cu pictograma ochi, link „Ai uitat parola?", bifa „Ține-mă minte", buton principal, rândul „Nu ai cont încă? Creează cont", separatorul „sau conectează-te cu" și butoane Google și iCloud cu logo-uri.
2. **Acasă**: descrisă mai sus.
3. **Fereastra calendar** (la click pe un sport): luna cu puncte pe zilele cu lobby-uri, lista lobby-urilor zilei, buton „Creează unul".
4. **Fereastra „Creează lobby"**.
5. **Pagina lobby-ului**: card cu sport, teren, dată, oră, oraș, vreme și stare; card „Cost și confirmare" (cost, minim, termen, preț estimat sau final, butoane); participanți cu rating, fiabilitate, „+ Prieten" și etichetă Online/Cash după confirmare; cereri de alăturare (doar gazdă); review cu stele după meci; chat; panou de simulare (doar în mediul `dev`).
6. **Agenda mea**, cu cele patru filtre.
7. **Prieteni**: căutare, listă, „Posibili cunoscuți", fereastra de mesaje.
8. **Profil**: antet cu avatar, rating, descriere; trei cărți cu statistici; sporturi; etichete; meciuri.
9. **Setări** cu cinci secțiuni.
10. **Fereastra de plată** (card de test, procesare, confirmare).

## 4. Tehnologii și rulare

| Strat | Alegere |
|---|---|
| Backend | Java 21, Spring Boot 3 (Web, Data JPA, Security cu JWT, Validation), Maven (`mvnw`) |
| Bază de date | PostgreSQL local, schema creată automat din entități (`ddl-auto=update`) |
| Frontend | React 18, Vite, JavaScript (**fără TypeScript**), React Router, CSS simplu |
| Chat | cereri HTTP, frontend-ul întreabă la 3 secunde (fără WebSocket) |
| Vreme | Open-Meteo (gratuit, fără cheie), apelat din backend |
| Joburi | `@Scheduled` pentru confirmarea automată a lobby-urilor |
| Colaborare | Git + GitHub, CI simplu (compilare și teste) |

**Nu folosim încă:** Docker, Flyway, WebSocket, Swagger, deploy, plăți reale.

**Rulare**: PostgreSQL pornit, baza `sportbuddy` creată, `application-local.properties` cu parola proprie (nu se urcă în Git). Backend: `cd backend`, `mvnw.cmd spring-boot:run` (port 8080). Frontend: `cd frontend`, `npm install`, `npm run dev` (port 5173). Vite trimite cererile `/api` către backend.

## 5. Structura proiectului și regulile ei

Repo-ul `sport-buddy/` conține `backend/`, `frontend/`, `docs/` și `.github/`. Pachetul de bază Java este `ro.utcn.sportbuddy`, clasa de pornire `SportbuddyApplication`.

```
backend/src/main/java/ro/utcn/sportbuddy/
  common/                         (comun: securitate, erori, evenimente)
  <modul>/model | repository | service | controller | dto | seed
frontend/src/
  api/<modul>.js                  apelurile către backend
  features/<modul>/               paginile și componentele unei funcționalități
  shared/                         componente comune și styles.css
  layout/                         bara laterală și header
  mocks/                          date false cât timp backend-ul nu e gata
```

**Module (foldere în backend):** `auth`, `users`, `friends`, `feed`, `catalog`, `lobbies`, `payments`, `ratings`, `chat`, `weather`, `dashboard`. Un modul NU există până nu îl creezi: este doar un folder cu acest nume. În frontend, `users` are paginile în `features/profile/`; restul folosesc același nume.

**Ce face fiecare strat în backend**

| Strat | Rol | Nu are voie |
|---|---|---|
| `model` | clasele care devin tabele (entități) | logică |
| `repository` | interfață pentru citit și scris în baza de date | logică |
| `service` | **regulile aplicației** (preț, penalizări, permisiuni) | să știe de HTTP |
| `controller` | adresa HTTP; validează forma datelor și apelează service-ul | reguli, apel direct la repository |
| `dto` | forma JSON-ului primit sau trimis (de exemplu fără parola) | |
| `seed` | date de test la pornire (doar profil `dev`, doar dacă tabela e goală) | |

Drumul unei cereri: React → `api/<modul>.js` → `controller` → `service` → `repository` → PostgreSQL, apoi înapoi ca JSON. Straturile se creează doar când sunt necesare. Orice funcționalitate cu reguli are `service`.

**Reguli care nu se încalcă**
1. Regulile de business stau în `service`, niciodată în controller sau în componentele React.
2. Un modul **nu importă `model` sau `repository` din alt modul**. Cere datele prin service-ul acelui modul sau reacționează la un eveniment din `common/events`.
3. Legăturile dintre module se fac prin id (`userId`, `cityId`), fără relații JPA între module.
4. Fiecare apel către backend din frontend trece prin `api/`.
5. Fișierele comune (`common/`, `pom.xml`, `application.properties`, `App.jsx`, `layout/`, `shared/`, `vite.config.js`, `docs/api.md`, `docs/entitati.md`) se modifică doar cu acordul celorlalți doi (Pull Request aprobat).
6. Ordinea la o funcționalitate nouă: contract în `docs/api.md` → `model` → `repository` → `dto` → `service` → `controller` → `seed` → `api/` → pagina React → ruta în `App.jsx`.
7. Utilizatorul curent se ia în controller din `CurrentUser.id()` (din `common/security`) și se trimite service-ului ca parametru. În mediul `dev`, antetul `X-Dev-User: ana` îl simulează.

**Evenimente între module** (clase în `common/events`, publicate de B, ascultate de C): `LobbyLockedEvent`, `PlayerCancelledEvent`, `AttendanceSavedEvent`, `LobbyCancelledEvent`.

## 6. Starea curentă

- Setup-ul este gata și funcționează: backend-ul rulează și răspunde la `GET /api/health`, frontend-ul afișează „Backend: ok", CI-ul rulează.
- Backend: există `common/`, `SportbuddyApplication` și fișierele de configurare. **Nu există încă niciun modul de funcționalitate.**
- Frontend: există folderele `api`, `features`, `layout`, `mocks`, `shared`. Stilurile din prototip nu sunt mutate încă în `shared/styles.css`.
- `docs/entitati.md` și `docs/api.md` descriu tabelele, regulile și endpoint-urile.

## 7. Împărțirea muncii

Fiecare face backend și frontend pentru funcționalitățile lui.

### Persoana A: utilizatori și social
Module: `auth`, `users`, `friends`, `feed`. Pagini: conectare și cont nou, profil, setări, prieteni, feed.
1. `users`: `User`, repository, service, controller, `UserSeeder` cu 12 utilizatori demo (parola `demo123`). Test: `GET /api/users`.
2. În `common/security`: `DevUserFilter` și `CurrentUser`. **Ceilalți așteaptă după el**, deci se unește primul.
3. `auth`: înregistrare și login cu parole criptate (BCrypt) și JWT. Pagina de conectare și cont nou.
4. Profil și setări (`UserSettings`, `UserSport`, poză, procentul de completare).
5. `friends`: cereri, listă, mesaje directe. 6. `feed`: postări, like-uri, comentarii, filtru pe sport.
Documentație: diagrama ER. Teste: validarea vârstei, parola.

### Persoana B: lobby-uri și bani
Module: `catalog`, `lobbies`, `payments`. Pagini: calendar, creare lobby, pagina lobby-ului, agendă, plată.
1. `catalog` (prioritar, ceilalți depind de el): `Sport`, `City` (cu coordonate), `Field`, `CatalogSeeder`, endpoint-uri pentru sporturi, orașe, terenuri. Sidebar-ul și căutarea de oraș citesc de la API.
2. `lobbies`: `Lobby`, `LobbyPlayer`, creare (termenul de confirmare și numărul de jucători aduși), listare pe oraș, sport și zi (calendar).
3. Pagina lobby-ului, `JoinRequest`, acceptare și refuz, participanți cu rating (din `RatingService`, modulul lui C).
4. Confirmare (manuală și cu `@Scheduled`), preț final, anulări, prezență, evenimentele către C, agenda. Endpoint-uri de simulare doar pe `dev`.
5. `payments`: interfața `PaymentProvider`, `MockPaymentProvider`, fereastra de plată, istoric.
Documentație: diagrama de stări (OPEN → LOCKED → FINISHED / CANCELLED) și de secvență. Teste: calculul prețului, termenul de confirmare.

### Persoana C: încredere și experiență
Module: `ratings`, `chat`, `weather`, `dashboard`. Pagini: prima pagină, review cu stele, chat, vreme.
1. `weather`: la început date fixe pe 5 zile, apoi apel către Open-Meteo. Widget de vreme cu iconițele colorate.
2. `ratings`: clasa `RatingCalculator` (media cu penalizări, fiabilitate) cu **teste unitare**, fără bază de date.
3. `RatingService.getStats(userId)` publicat devreme (la început răspuns „nou"), pentru A și B.
4. Pagina de start din `features/home/` (inițial cu date din `mocks/`).
5. `Review`, `UserRatingStats`, ascultători pentru evenimentele lui B, formularul cu stele.
6. `chat`: `LobbyMessage`, endpoint-uri, interogare la 3 secunde. 7. `dashboard` (agregă date din modulele celorlalți).
Documentație: diagrama de arhitectură, cazurile de utilizare, README final.

## 8. Reguli Git

- `main` nu se modifică direct. Fiecare lucrează pe `feature/<modul>-<ce>` și deschide Pull Request, cu aprobarea unui coleg care citește și rulează codul.
- PR-uri mici (o funcționalitate). `git pull --rebase origin main` în fiecare dimineață.
- Un issue pe funcționalitate pe board-ul GitHub. Nu se urcă `target/`, `node_modules/`, `application-local.properties`.
- Dacă schimbi serios o entitate (redenumești sau schimbi tipul unei coloane), anunți grupul: toți resetează baza (`DROP DATABASE sportbuddy; CREATE DATABASE sportbuddy;`).
- Seeder-ele sunt idempotente (adaugă date doar dacă tabela e goală).

## 9. Instrucțiuni pentru asistentul AI

Utilizatorul este student, la început cu Spring Boot și React, și învață din mers.
- Răspunde în română, simplu și pas cu pas. **Un pas pe rând**, cu explicația „de ce" în câteva cuvinte.
- Pentru fiecare fișier spune calea completă, tipul (clasă, interfață, record) și cum se creează (IntelliJ: click dreapta pe pachet → *New → Java Class*; VS Code: *New File*).
- Respectă straturile și regulile de la secțiunea 5. Nu pune logică în controller sau în componentele React și nu importa `model` sau `repository` din alt modul.
- Dacă ceva presupune modificarea unui fișier comun, un endpoint nou sau o bibliotecă nouă în `pom.xml` sau `package.json`, spune explicit că trebuie anunțat grupul.
- Nu introduce Docker, Flyway, TypeScript sau WebSocket și nu schimba stack-ul.
- Pentru interfață, pornește de la prototip: aceleași variabile de culoare, același font, aceleași forme și texte (secțiunea 3).
- La final de pas, spune cum se verifică (adresa din browser, tabela din pgAdmin, pagina React).
- Dacă lipsește o informație (câmp, endpoint), verifică `entitati.md` și `api.md` înainte să inventezi. Dacă nu e acolo, propune adăugarea ei.

## 10. Mesaje de start (copiază unul în chatul nou, după brief)

**Persoana A**
> Sunt persoana A. Urmează să construiesc modulul `users` (primul pas din secțiunea 7: `User`, repository, service, controller și seeder cu 12 utilizatori demo). Ghidează-mă pas cu pas, începând cu crearea pachetelor în IntelliJ.

**Persoana B**
> Sunt persoana B. Urmează să construiesc modulul `catalog` (sporturi, orașe cu coordonate și terenuri), cu seeder, și apoi să afișez sporturile și orașele în frontend. Ghidează-mă pas cu pas, începând cu entitatea `Sport`.

**Persoana C**
> Sunt persoana C. Urmează să construiesc modulul `weather`: întâi un endpoint cu date fixe pe 5 zile și widgetul de vreme din prototip, apoi apelul către Open-Meteo. Ghidează-mă pas cu pas.

**Pentru funcționalități ulterioare**
> Sunt persoana [A/B/C]. Vreau să implementez [funcționalitatea] din modulul `[modul]`. Spune-mi ce straturi are nevoie, în ce fișiere și în ce ordine, apoi ghidează-mă pas cu pas.
