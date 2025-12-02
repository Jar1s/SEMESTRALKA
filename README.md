# 📘 CALLSTUDY - Platforma na kolaboratívne štúdium

**Autor:** Jaroslav Birčák  
**Predmet:** Semestrálna práca – Klient-server architektúra  
**Rok:** 2. ročník

---

## 1. Stručný popis projektu a cieľov aplikácie

**CALLSTUDY** je desktopová aplikácia určená pre študentov, ktorá umožňuje kolaboratívne štúdium v rámci študijných skupín. Aplikácia rieši problém efektívnej spolupráce medzi študentmi pri štúdiu, organizácii úloh a zdieľaní materiálov.

### Hlavné funkcie:
- Správa študijných skupín (vytváranie, pridávanie členov)
- Správa úloh s deadlinami (OPEN, IN_PROGRESS, DONE)
- Zdieľanie materiálov (súbory a URL odkazy)
- Real-time komunikácia (chat a notifikácie cez WebSocket)
- Automatické deadline reminders

### Komu je určená:
Študentom vysokých a stredných škôl, študijným skupinám a tímom potrebujúcim organizovať úlohy a zdieľať materiály.

---

## 2. Architektúra systému

Aplikácia je založená na **klient-server architektúre** s trojvrstvovým modelom.

### Diagram architektúry

```
┌─────────────────┐
│ KLIENT (JavaFX) │
│ Controllers     │
│ Models + Views  │
│ ApiClient + WS  │
└────────┬────────┘
         │ HTTP/REST + WebSocket
┌────────▼────────┐
│ SERVER (Spring)  │
│ Controllers      │
│ Services         │
│ Repositories     │
│ WebSocket Handler│
└────────┬────────┘
         │ JPA/Hibernate
┌────────▼────────┐
│ DATABÁZA (SQLite)│
│ Users, Groups    │
│ Tasks, Resources │
│ ChatMessages     │
└─────────────────┘
```

### Popis vrstiev

**Frontend (JavaFX):** Controllers spracúvajú UI udalosti, Models reprezentujú dáta, Views sú FXML súbory. `ApiClient` komunikuje cez REST API, `WebSocketClient` pre real-time notifikácie.

**Backend (Spring Boot):** Controllers poskytujú REST API endpointy, Services obsahujú biznis logiku, Repositories pristupujú k databáze cez JPA. WebSocket Handler broadcastuje notifikácie.

**Databáza (SQLite):** Ukladá všetky dáta aplikácie. Hibernate automaticky vytvára schému z JPA entít.

---

## 3. Databázový model (ER diagram)

### ER Diagram

```
┌─────────┐
│  USERS  │
│ user_id │
│ email   │
│ name    │
│ pwd_hash│
└────┬────┘
     │ 1:N
┌────▼──────────┐
│ MEMBERSHIPS  │
│ user_id (FK) │
│ group_id(FK) │
│ role         │
└────┬──────────┘
     │
┌────▼──────────┐
│   GROUPS      │
│ group_id (PK) │
│ name          │
│ created_by    │
└────┬──────────┘
     │ 1:N
┌────▼─────┐  ┌──────────┐  ┌─────────────┐
│  TASKS   │  │RESOURCES │  │CHAT_MESSAGES│
│ task_id  │  │resource_id│ │message_id   │
│ group_id │  │group_id   │  │group_id      │
│ status   │  │type       │  │message       │
│ deadline │  │path_or_url│ │sent_at       │
└──────────┘  └──────────┘  └─────────────┘
```

### Hlavné tabuľky

- **USERS** – používatelia (email, name, password_hash)
- **GROUPS** – študijné skupiny (name, description, created_by)
- **MEMBERSHIPS** – prepojenie používateľov a skupín (user_id, group_id, role)
- **TASKS** – úlohy (title, description, status, deadline)
- **RESOURCES** – zdieľané materiály (type: FILE/URL, path_or_url)
- **CHAT_MESSAGES** – chat správy (message, user_id, sent_at)

### Podpora funkcií

- **Autentifikácia:** bcrypt hashovanie hesiel v USERS
- **Skupinová spolupráca:** MEMBERSHIPS umožňuje many-to-many vzťah
- **Správa úloh:** TASKS s deadline pre sledovanie termínov
- **Zdieľanie:** RESOURCES podporuje súbory aj URL
- **Chat:** CHAT_MESSAGES ukladá históriu správ

---

## 4. Dokumentácia REST API a WebSocket endpointov

### 4.1 REST API Endpointy

#### Autentifikácia
- `POST /api/users/register` – registrácia (email, name, password)
- `POST /api/users/login` – prihlásenie (email, password) → vráti UserDTO
- `GET /api/users/{id}` – informácie o používateľovi
- `PUT /api/users/{id}` – aktualizácia profilu

#### Skupiny
- `GET /api/groups` – zoznam všetkých skupín
- `POST /api/groups` – vytvorenie skupiny (name, description, createdBy)
- `GET /api/groups/{id}` – detail skupiny
- `PUT /api/groups/{id}` – aktualizácia (len vlastník)
- `DELETE /api/groups/{id}` – odstránenie
- `GET /api/groups/{groupId}/members` – členovia skupiny
- `POST /api/groups/{groupId}/join` – pridanie do skupiny
- `DELETE /api/groups/{groupId}/leave` – opustenie skupiny

#### Úlohy
- `POST /api/tasks` – vytvorenie úlohy (groupId, title, description, deadline)
- `GET /api/tasks/group/{groupId}` – úlohy skupiny
- `GET /api/tasks/{id}` – detail úlohy
- `PUT /api/tasks/{id}/status` – zmena statusu (OPEN, IN_PROGRESS, DONE)
- `PUT /api/tasks/{id}` – kompletná aktualizácia

#### Materiály
- `GET /api/resources/group/{groupId}` – materiály skupiny
- `POST /api/resources/upload` – nahrávanie súboru (multipart/form-data)
- `POST /api/resources/url` – zdieľanie URL odkazu
- `GET /api/resources/{id}/download` – stiahnutie súboru
- `DELETE /api/resources/{id}` – odstránenie

#### Chat
- `GET /api/chat/group/{groupId}` – správy skupiny
- `POST /api/chat/send` – odoslanie správy (groupId, userId, message)

### 4.2 WebSocket Endpointy

**`ws://localhost:8081/ws/simple`** – Plain WebSocket pre real-time notifikácie

**Typy notifikácií:**
- `NEW_TASK`, `TASK_STATUS_CHANGED`, `NEW_MEMBER`, `NEW_GROUP`
- `NEW_RESOURCE`, `CHAT_MESSAGE`
- `DEADLINE_REMINDER` (3 dni pred), `DEADLINE_WARNING` (6 hodín), `DEADLINE_URGENT` (1 hodina), `DEADLINE_OVERDUE`

**Príklad notifikácie:**
```json
{
  "type": "DEADLINE_REMINDER",
  "message": "Úloha 'Domáca úloha 1' má deadline za 2 dni",
  "groupId": 1,
  "taskId": 1
}
```

**STOMP WebSocket:** `ws://localhost:8080/ws/notifications` (pre web klientov)

---

## 5. Ukážky používateľského rozhrania

### Prihlasovacie okno (`login.fxml`)
- **Tab "Login"** – email, heslo, prihlásenie
- **Tab "Register"** – email, meno, heslo (min. 6 znakov), registrácia
- Tok: Zadanie údajov → API volanie → pri úspechu otvorenie Dashboardu

### Dashboard (`dashboard.fxml`)
- **Tab "My Groups"** – zoznam skupín používateľa, "Create Group", dvojklik otvorí detail
- **Tab "All Groups"** – všetky skupiny, možnosť pridať sa
- **Tab "Tasks"** – prehľad úloh s filtrom, deadline indikátory
- **Status bar** – notifikácie a stav pripojenia

### Detail skupiny (`group-detail.fxml`)
- **Tab "Members"** – zoznam členov, "Join/Leave Group"
- **Tab "Tasks"** – úlohy skupiny, "Create Task", zmena statusu, deadline indikátory
- **Tab "Resources"** – súbory a URL, "Upload File", "Share URL", stiahnutie
- **Tab "Chat"** – diskusia skupiny, real-time aktualizácia správ

**Vizuálny štýl:** Moderný tmavý motív, farebné indikátory pre deadliny, toast notifikácie

---

## 6. Popis výziev a riešení

### Validácia vstupov
**Výzva:** Zabezpečiť neplatné dáta neprejdú.  
**Riešenie:** Server-side `@Valid` anotácie (Bean Validation), client-side kontrola pred odoslaním, DTO s `@NotBlank` a `@Email`.

### Autentifikácia a bezpečnosť
**Výzva:** Bezpečné ukladanie hesiel.  
**Riešenie:** Bcrypt hashovanie (`BCryptPasswordEncoder`), verifikácia pri prihlásení, session management v klientovi.

### Real-time komunikácia (WebSocket)
**Výzva:** Real-time notifikácie bez pollingu.  
**Riešenie:** Plain WebSocket pre JavaFX (`SimpleWebSocketHandler`), STOMP pre web, broadcast cez `CopyOnWriteArraySet<WebSocketSession>`, automatické pripojenie a reconnection.

### Deadline reminders
**Výzva:** Automatické upozornenia na deadliny.  
**Riešenie:** Scheduled service (`@Scheduled` každú hodinu), inteligentné upozornenia (3 dni, 6 hodín, 1 hodina), deduplikácia (raz denne).

### File upload
**Výzva:** Bezpečné nahrávanie súborov.  
**Riešenie:** UUID názvy súborov, izolácia v `uploads/` priečinku, validácia, cleanup pri odstránení.

### Databázová migrácia
**Výzva:** Automatické vytvorenie databázy.  
**Riešenie:** Hibernate auto-ddl (`spring.jpa.hibernate.ddl-auto=update`), entity-first prístup.

### Error handling
**Výzva:** Zrozumiteľné chybové správy.  
**Riešenie:** Structured JSON odpovede, `@ExceptionHandler` v controlleroch, client-side zobrazenie v labeloch/toast.

---

## 7. Zhodnotenie práce s AI

### Čo AI pomohlo vygenerovať
- **REST API štruktúra** – Controllers, DTO triedy, Service vrstvy (80% generované)
- **Databázový model** – JPA entity s anotáciami (90% generované)
- **WebSocket konfigurácia** – základná implementácia (60% generované)
- **Validácia** – `@Valid` anotácie, `BCryptPasswordEncoder` (70% generované)

### Čo muselo byť manuálne doladené
- **JavaFX UI** – FXML layout, CSS štýl, event handling (70% manuálne)
- **WebSocket klient** – JavaFX integrácia, reconnection logic (40% manuálne)
- **Deadline Reminders** – timing, deduplikácia (60% manuálne)
- **Error handling** – edge cases, null kontroly (60% manuálne)
- **API Client** – robustnosť, error handling (50% manuálne)

### Čo som sa naučil
**Pozitíva:** Rýchle prototypovanie, best practices, konzistentnosť kódu.  
**Obmedzenia:** Problémy s JavaFX FXML, threading, integrácia komponentov, edge cases.  
**Praktiky:** Iteratívny prístup (AI základ → manuálne doladenie), kontrola a testovanie kódu, dokumentácia AI-generovaných častí.

**Záver:** AI bolo užitočné pre základnú štruktúru, ale manuálne doladenie bolo nevyhnutné pre funkčnosť a bezpečnosť.

---

## 8. Technológie

- **Frontend:** JavaFX 21, FXML, CSS
- **Backend:** Spring Boot 3.2.0, Spring Data JPA, Hibernate, Spring WebSocket
- **Databáza:** SQLite
- **Komunikácia:** REST API (HTTP/JSON), WebSocket, Gson
- **Build:** Maven, JUnit 5, JaCoCo

---

## 9. Spustenie projektu

### Požiadavky
Java 17+, Maven 3.6+

### Spustenie servera
```bash
cd server
mvn spring-boot:run
```
Server: `http://localhost:8080` (REST), `ws://localhost:8081` (WebSocket)

### Spustenie klienta
```bash
cd client
mvn javafx:run
```

### Prvé použitie
1. Spustite server a klient
2. Registrácia cez tab "Register"
3. Prihlásenie a vytvorenie skupiny
4. Skupiny a úlohy sa zobrazia v dashboarde

---

## 10. Testovanie

```bash
# Server testy
cd server && mvn test

# S coverage
cd server && mvn clean test jacoco:report
# Otvorte: server/target/site/jacoco/index.html
```

---

## 11. Dokumentácia

- [PRD.md](PRD.md) - Product Requirements Document
- [RULES.md](RULES.md) - Pravidlá pre prácu s AI
- [QUICKSTART.md](QUICKSTART.md) - Rýchly sprievodca
- [NOTIFICATIONS_AND_CHAT.md](NOTIFICATIONS_AND_CHAT.md) - Notifikácie a chat
- [TESTING.md](TESTING.md) - Testovanie

---

## 12. Záver

**CALLSTUDY** je funkčná klient-server aplikácia kombinujúca databázu, REST API, real-time komunikáciu a JavaFX GUI.

**Hlavné úspechy:**
- ✅ REST API s validáciou
- ✅ Real-time notifikácie a chat cez WebSocket
- ✅ Bezpečná autentifikácia (bcrypt)
- ✅ Moderné JavaFX UI
- ✅ Automatické deadline reminders
- ✅ File upload a zdieľanie materiálov

**Možné rozšírenia:** Mobilná aplikácia, Google OAuth2, pokročilejšia analytika, email notifikácie

---

**Autor:** Jaroslav Birčák  
**Dátum:** 2024  
**Licencia:** Vzdelávacie účely
