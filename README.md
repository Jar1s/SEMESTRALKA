# 📘 CALLSTUDY - Platforma na kolaboratívne štúdium

**Autor:** Jaroslav Birčák, Tomáš Cingel
**Predmet:** Semestrálna práca – Klient-server architektúra  
**Rok:** 2. ročník

---

## 1. Stručný popis projektu a cieľov aplikácie

**CALLSTUDY** je desktopová aplikácia určená pre študentov, ktorá umožňuje kolaboratívne štúdium v rámci študijných skupín. Aplikácia rieši problém efektívnej spolupráce medzi študentmi pri štúdiu, organizácii úloh a zdieľaní materiálov.

### Hlavné funkcie:
- **Správa študijných skupín** – vytváranie skupín, pridávanie členov, správa oprávnení
- **Správa úloh** – vytváranie úloh s deadlinami, sledovanie ich stavu (OPEN, IN_PROGRESS, DONE)
- **Zdieľanie materiálov** – nahrávanie súborov a zdieľanie odkazov
- **Real-time komunikácia** – chat v rámci skupín a notifikácie o dôležitých udalostiach
- **Automatické upozornenia** – deadline reminders a notifikácie o zmene stavu úloh
- **Analytika** – prehľad aktivít a štatistiky skupín

### Komu je aplikácia určená:
- Študentom vysokých škôl a stredných škôl
- Študijným skupinám pracujúcim na projektoch
- Tímom potrebujúcim organizovať úlohy a zdieľať materiály

### Aký problém rieši:
- **Nedostatok organizácie** – študenti často strácajú prehľad o úlohách a deadlinoch
- **Komunikačné problémy** – chýba centralizované miesto pre diskusiu a zdieľanie
- **Nedostatočná spolupráca** – ťažko sa koordinuje práca v tíme bez vhodných nástrojov
- **Chýbajúce upozornenia** – študenti často zabudnú na dôležité termíny

---

## 2. Architektúra systému

Aplikácia je založená na **klient-server architektúre** s trojvrstvovým modelom - KLIENT (JavaFX), SERVER (Spring Boot),  DATABÁZA (SQLite)



### 2.1 Popis vrstiev

#### **Frontend (Klient) – JavaFX**
- **Úloha:** Poskytuje používateľské rozhranie a spracováva interakcie
- **Komponenty:**
  - **Controllers** (`controller/`) – spracúvajú udalosti z UI, volajú API
  - **Models** (`model/`) – reprezentujú dátové štruktúry na strane klienta
  - **Views** (`view/*.fxml`) – definujú rozloženie okien pomocou FXML
  - **Utils** (`util/`) – `ApiClient` pre REST komunikáciu, `NotificationWebSocketClient` a `ChatWebSocketClient` pre real-time komunikáciu

#### **Backend (Server) – Spring Boot**
- **Úloha:** Spracováva požiadavky, implementuje biznis logiku, spravuje databázu
- **Komponenty:**
  - **Controllers** (`controller/`) – REST API endpointy pre všetky operácie
  - **Services** (`service/`) – biznis logika (validácia, autorizácia, notifikácie)
  - **Repositories** (`repository/`) – prístup k databáze cez JPA
  - **Models** (`model/`) – JPA entity reprezentujúce databázové tabuľky
  - **Config** (`config/`) – WebSocket konfigurácia, bezpečnostné nastavenia

#### **Databáza – SQLite**
- **Úloha:** Ukladá všetky dáta aplikácie (používatelia, skupiny, úlohy, správy)
- **Výhody:** Jednoduchá, bez servera, vhodná pre vývoj a testovanie
- **ORM:** Hibernate/JPA automaticky vytvára schému z entít

### 2.3 Tok dát

1. **REST API komunikácia:**
   - Klient → `ApiClient.post/get()` → Server Controller → Service → Repository → Databáza
   - Odpoveď: Databáza → Repository → Service → Controller → JSON → Klient

2. **WebSocket komunikácia:**
   - Server → `SimpleWebSocketHandler.broadcast()` → Všetci pripojení klienti
   - Používa sa pre notifikácie a chat správy v reálnom čase

---

## 3. Databázový model (ER diagram)

### 3.1 ER Diagram

```
┌─────────────┐
│    USERS    │
├─────────────┤
│ user_id (PK)│
│ email (UK)  │
│ name        │
│ password_   │
│   hash      │
│ google_id   │
│ auth_       │
│   provider  │
│ created_at  │
└──────┬──────┘
       │
       │ 1:N
       │
┌──────▼──────────────┐
│   MEMBERSHIPS       │
├─────────────────────┤
│ membership_id (PK)  │
│ user_id (FK)        │◄──┐
│ group_id (FK)       │   │
│ role                │   │
│ joined_at           │   │
└─────────────────────┘   │
                          │
┌─────────────┐           │
│   GROUPS    │           │
├─────────────┤           │
│ group_id(PK)│───────────┘
│ name        │     1:N
│ description │
│ created_by  │
│ created_at  │
└──────┬──────┘
       │
       │ 1:N
       │
┌──────▼──────────────┐  ┌─────────────┐  ┌──────────────┐
│      TASKS          │  │  RESOURCES  │  │ CHAT_MESSAGES│
├─────────────────────┤  ├─────────────┤  ├──────────────┤
│ task_id (PK)        │  │ resource_id │  │ message_id   │
│ group_id (FK)       │  │ group_id(FK)│  │ group_id(FK) │
│ created_by          │  │ uploaded_by │  │ user_id (FK) │
│ title               │  │ title       │  │ user_name    │
│ description         │  │ type        │  │ message      │
│ status              │  │ path_or_url │  │ sent_at      │
│ deadline            │  │ uploaded_at │  └──────────────┘
│ created_at          │  └─────────────┘
└─────────────────────┘
```

### 3.2 Popis tabuliek

#### **USERS**
- **Účel:** Ukladá informácie o používateľoch
- **Kľúčové polia:**
  - `user_id` (PK) – primárny kľúč
  - `email` (UNIQUE) – jedinečný email používateľa
  - `password_hash` – bcrypt hash hesla
  - `auth_provider` – LOCAL alebo GOOGLE
- **Vzťahy:** 1:N s MEMBERSHIPS

#### **GROUPS**
- **Účel:** Reprezentuje študijné skupiny
- **Kľúčové polia:**
  - `group_id` (PK) – primárny kľúč
  - `created_by` – ID používateľa, ktorý skupinu vytvoril
- **Vzťahy:** 1:N s MEMBERSHIPS, TASKS, RESOURCES, CHAT_MESSAGES

#### **MEMBERSHIPS**
- **Účel:** Prepojenie používateľov a skupín (many-to-many)
- **Kľúčové polia:**
  - `membership_id` (PK) – primárny kľúč
  - `user_id` (FK) → USERS
  - `group_id` (FK) → GROUPS
  - `role` – MEMBER alebo ADMIN
- **Vzťahy:** N:1 s USERS, N:1 s GROUPS

#### **TASKS**
- **Účel:** Úlohy v rámci skupín
- **Kľúčové polia:**
  - `task_id` (PK) – primárny kľúč
  - `group_id` (FK) → GROUPS
  - `status` – OPEN, IN_PROGRESS, DONE
  - `deadline` – dátum a čas termínu
- **Vzťahy:** N:1 s GROUPS

#### **RESOURCES**
- **Účel:** Zdieľané materiály (súbory alebo URL)
- **Kľúčové polia:**
  - `resource_id` (PK) – primárny kľúč
  - `group_id` (FK) → GROUPS
  - `type` – FILE alebo URL
  - `path_or_url` – cesta k súboru alebo URL adresa
- **Vzťahy:** N:1 s GROUPS

#### **CHAT_MESSAGES**
- **Účel:** Chat správy v rámci skupín
- **Kľúčové polia:**
  - `message_id` (PK) – primárny kľúč
  - `group_id` (FK) → GROUPS
  - `user_id` (FK) → USERS
  - `message` – text správy
  - `sent_at` – čas odoslania
- **Vzťahy:** N:1 s GROUPS, N:1 s USERS

### 3.3 Ako databáza podporuje funkcie aplikácie

- **Autentifikácia:** `USERS` tabuľka ukladá hashované heslá (bcrypt)
- **Skupinová spolupráca:** `MEMBERSHIPS` umožňuje viac používateľov v jednej skupine
- **Správa úloh:** `TASKS` s `deadline` umožňuje sledovanie termínov
- **Zdieľanie:** `RESOURCES` podporuje súbory aj URL odkazy
- **Real-time chat:** `CHAT_MESSAGES` ukladá históriu správ
- **Analytika:** Všetky tabuľky obsahujú časové značky (`created_at`, `sent_at`) pre štatistiky

---

## 4. Dokumentácia WebSocket endpointov

### 4.2 WebSocket Endpointy

#### **Notifikácie**

##### `ws://localhost:8081/ws/simple`
- **Popis:** WebSocket endpoint pre real-time notifikácie
- **Protokol:** Plain WebSocket (nie STOMP)
- **Typy notifikácií:**
  ```json
  {
    "type": "NEW_TASK",
    "message": "Nová úloha: Domáca úloha 1",
    "groupId": 1,
    "taskId": 1
  }
  ```

**Typy notifikácií:**
- `NEW_TASK` – nová úloha bola vytvorená
- `TASK_STATUS_CHANGED` – zmena statusu úlohy
- `NEW_MEMBER` – nový člen sa pridál do skupiny
- `NEW_GROUP` – nová skupina bola vytvorená
- `NEW_RESOURCE` – nový materiál bol pridaný
- `DEADLINE_REMINDER` – upozornenie na deadline (3 dni pred)
- `DEADLINE_WARNING` – varovanie pred deadlinom (6 hodín)
- `DEADLINE_URGENT` – urgentné upozornenie (1 hodina)
- `DEADLINE_OVERDUE` – úloha je po deadlínu
- `CHAT_MESSAGE` – nová chat správa

**Príklad notifikácie:**
```json
{
  "type": "DEADLINE_REMINDER",
  "message": "Úloha 'Domáca úloha 1' má deadline za 2 dni",
  "groupId": 1,
  "taskId": 1
}
```

#### **STOMP WebSocket (alternatíva)**

##### `ws://localhost:8080/ws/notifications`
- **Popis:** STOMP WebSocket endpoint (používa sa pre web klientov)
- **Protokol:** STOMP over WebSocket
- **Topics:**
  - `/topic/notifications` – notifikácie
  - `/topic/chat/group/{groupId}` – chat správy pre skupinu

---

## 5. Ukážky používateľského rozhrania

### 5.1 Prihlasovacie okno (Login)

**Súbor:** `login.fxml`

**Funkcie:**
- **Tab "Login"** – prihlásenie existujúceho používateľa
  - Email input
  - Password input
  - "Login" tlačidlo
- **Tab "Register"** – registrácia nového používateľa
  - Email input (validácia)
  - Name input
  - Password input (min. 6 znakov)
  - "Register" tlačidlo

**Tok práce:**
1. Používateľ zadá email a heslo
2. Klikne na "Login"
3. Aplikácia volá `POST /api/users/login`
4. Pri úspešnom prihlásení sa otvorí Dashboard

### 5.2 Dashboard (Hlavné okno)

**Súbor:** `dashboard.fxml`

**Komponenty:**
- **Tab "My Groups"** – zoznam skupín používateľa
  - TableView so skupinami (názov, popis, počet členov)
  - "Create Group" tlačidlo
  - "Refresh" tlačidlo
  - Dvojklik na skupinu otvorí detail
- **Tab "All Groups"** – všetky dostupné skupiny
  - Možnosť pridať sa do skupiny
- **Tab "Tasks"** – prehľad úloh
  - Filtrovanie podľa skupiny
  - Zobrazenie statusu (OPEN, IN_PROGRESS, DONE)
  - Deadline indikátory
- **Status bar** – zobrazuje notifikácie a stav pripojenia

**Tok práce:**
1. Po prihlásení sa načítajú skupiny (`GET /api/groups`)
2. Používateľ môže vytvoriť novú skupinu
3. Dvojklikom na skupinu sa otvorí detail skupiny
4. Notifikácie sa zobrazujú v status bare

### 5.3 Detail skupiny (Group Detail)

**Súbor:** `group-detail.fxml`

**Komponenty:**
- **Informácie o skupine** – názov, popis, vlastník
- **Tab "Members"** – zoznam členov skupiny
  - TableView s členmi
  - "Join Group" / "Leave Group" tlačidlá
- **Tab "Tasks"** – úlohy skupiny
  - TableView s úlohami
  - "Create Task" tlačidlo
  - Možnosť zmeniť status úlohy
  - Deadline indikátory (červená = po deadlínu, oranžová = blíži sa)
- **Tab "Resources"** – zdieľané materiály
  - Zoznam súborov a URL odkazov
  - "Upload File" a "Share URL" tlačidlá
  - Stiahnutie súborov
- **Tab "Chat"** – diskusia skupiny
  - Zoznam správ s menami autorov
  - Textové pole pre novú správu
  - "Send" tlačidlo
  - Real-time aktualizácia nových správ

**Tok práce:**
1. Otvorenie detailu skupiny zobrazí všetky informácie
2. Vytvorenie úlohy: klik na "Create Task" → dialóg → `POST /api/tasks`
3. Zmena statusu: výber úlohy → zmena statusu → `PUT /api/tasks/{id}/status`
4. Nahrávanie súboru: "Upload File" → výber súboru → `POST /api/resources/upload`
5. Chat: písanie správy → "Send" → `POST /api/chat/send` → real-time zobrazenie

### 5.4 Vizuálny štýl

- **Moderný dizajn** – tmavý motív s farebnými akcentmi
- **Responsive layout** – adaptívne rozloženie pre rôzne veľkosti okien
- **Ikony a indikátory** – vizuálne rozlíšenie stavov (červená/oranžová/zelená pre deadliny)
- **Toast notifikácie** – dočasné upozornenia v pravom dolnom rohu

---

## 6. Popis výziev a riešení

### 6.1 Validácia vstupov

**Výzva:** Zabezpečiť, aby používatelia nemohli odoslať neplatné dáta (prázdne polia, neplatné emaily, atď.)

**Riešenie:**
- **Server-side validácia:** Použitie `@Valid` anotácií a `jakarta.validation` (Bean Validation)
  ```java
  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
      // Validácia sa vykoná automaticky
  }
  ```
- **Client-side validácia:** Kontrola pred odoslaním požiadavky
  ```java
  if (email.isEmpty() || password.isEmpty()) {
      showError("Please fill in all fields");
      return;
  }
  ```
- **DTO validácia:** `RegisterRequest` a `LoginRequest` majú `@NotBlank` a `@Email` anotácie

### 6.2 Autentifikácia a bezpečnosť

**Výzva:** Zabezpečiť, aby heslá neboli uložené v plain texte a aby sa zabránilo neoprávnenému prístupu.

**Riešenie:**
- **Bcrypt hashovanie:** Heslá sa hashujú pomocou `BCryptPasswordEncoder` pred uložením
  ```java
  String hashedPassword = passwordEncoder.encode(request.getPassword());
  ```
- **Verifikácia hesla:** Pri prihlásení sa porovnáva hash pomocou `passwordEncoder.matches()`
- **Session management:** Používateľské ID sa ukladá lokálne v klientovi po úspešnom prihlásení
- **CORS konfigurácia:** `@CrossOrigin(origins = "*")` umožňuje komunikáciu medzi klientom a serverom

**Poznámka:** V produkčnom prostredí by sa mali použiť JWT tokeny alebo Spring Security pre pokročilejšiu autentifikáciu.

### 6.3 Real-time komunikácia (WebSocket)

**Výzva:** Implementovať real-time notifikácie a chat bez zbytočného pollingu.

**Riešenie:**
- **Dvojitá implementácia:**
  - **Plain WebSocket** (`SimpleWebSocketHandler`) – pre JavaFX klienta
  - **STOMP WebSocket** (`WebSocketConfig`) – pre web klientov (budúcnosť)
- **Broadcast mechanizmus:** `CopyOnWriteArraySet<WebSocketSession>` pre správu pripojení
  ```java
  public void broadcastNotification(NotificationDTO notification) {
      String json = gson.toJson(notification);
      sessions.forEach(session -> {
          try {
              session.sendMessage(new TextMessage(json));
          } catch (IOException e) {
              // Handle error
          }
      });
  }
  ```
- **Automatické pripojenie:** Klient sa pripojí k WebSocket pri otvorení dashboardu
- **Reconnection logic:** Klient automaticky obnoví pripojenie pri strate spojenia

### 6.4 Deadline reminders

**Výzva:** Automaticky upozorňovať používateľov na blížiace sa deadliny.

**Riešenie:**
- **Scheduled service:** `DeadlineReminderService` beží každú hodinu (`@Scheduled`)
- **Inteligentné upozornenia:**
  - 3 dni pred: denné upozornenie
  - 6-24 hodín: varovanie (raz denne)
  - 1-6 hodín: varovanie (raz denne)
  - <1 hodina: urgentné upozornenie (len raz)
- **Deduplikácia:** Notifikácie sa posielajú len raz za deň pre každú úlohu

### 6.5 File upload

**Výzva:** Bezpečné nahrávanie a ukladanie súborov.

**Riešenie:**
- **UUID názvy:** Súbory sa ukladajú s UUID názvami, aby sa zabránilo kolíziám
  ```java
  String filename = UUID.randomUUID().toString() + extension;
  ```
- **Izolácia:** Súbory sa ukladajú do `uploads/` priečinka
- **Validácia:** Kontrola, či súbor nie je prázdny pred uložením
- **Cleanup:** Pri odstránení resource sa odstráni aj súbor z disku

### 6.6 Databázová migrácia

**Výzva:** Zabezpečiť, aby sa databáza automaticky vytvorila pri prvom spustení.

**Riešenie:**
- **Hibernate auto-ddl:** `spring.jpa.hibernate.ddl-auto=update` automaticky vytvorí/aktualizuje schému
- **SQLite inicializácia:** Databáza sa vytvorí automaticky, ak neexistuje
- **Entity-first prístup:** Databázová schéma sa generuje z JPA entít

### 6.7 Error handling

**Výzva:** Poskytnúť používateľovi zrozumiteľné chybové správy.

**Riešenie:**
- **Structured responses:** Všetky chyby vracajú JSON s popisom
  ```json
  {
    "error": "Email already exists"
  }
  ```
- **Exception handlers:** `@ExceptionHandler` v controlleroch zachytáva výnimky
- **Client-side zobrazenie:** Chybové správy sa zobrazujú v labeloch alebo toast notifikáciách

---

## 7. Zhodnotenie práce s AI – čo pomohlo, čo muselo byť manuálne doladené

### 7.1 Čo AI pomohlo vygenerovať

#### **REST API štruktúra**
- **Generované pomocou AI:** Základná štruktúra controllerov, DTO triedy, service vrstvy
- **Príklad:** `UserController`, `GroupController`, `TaskController` boli navrhnuté AI


#### **Databázový model**
- **Generované pomocou AI:** JPA entity (`User`, `Group`, `Task`, `Membership`, atď.)
- **Výhody:** Konzistentné názvy, správne anotácie, vzťahy medzi entitami
- **Poznámka:** Niektoré vzťahy boli potrebné upraviť manuálne

#### **WebSocket konfigurácia**
- **Generované pomocou AI:** `WebSocketConfig`, `SimpleWebSocketHandler`
- **Výhody:** Funkčná základná implementácia, správna konfigurácia STOMP


### 7.2 Čo muselo byť manuálne doladené

#### **JavaFX UI a FXML**
- **Problém:** AI generovalo základné FXML, ale dizajn a layout boli nevyhovujúce
- **Manuálne úpravy:**
  - Vlastný CSS štýl (`modern.css`) pre moderný vzhľad
  - Komplexné rozloženie s TabPane a TableView
  - Event handling v controlleroch
  - Real-time aktualizácia UI pri notifikáciách

#### **WebSocket klient (JavaFX)**
- **Problém:** AI generovalo STOMP klienta, ale JavaFX potreboval plain WebSocket
- **Manuálne úpravy:**
  - Implementácia `NotificationWebSocketClient` s `java-websocket` knižnicou
  - Reconnection logic
  - Integrácia s JavaFX Platform.runLater() pre thread-safe UI aktualizácie

#### **Deadline Reminder Service**
- **Problém:** AI generovalo základnú logiku, ale timing a deduplikácia boli nesprávne
- **Manuálne úpravy:**
  - Inteligentné upozornenia (3 dni, 6 hodín, 1 hodina)
  - Zabránenie duplicitným notifikáciám
  - Správne časové výpočty

#### **Error handling a edge cases**
- **Problém:** AI generovalo základné error handling, ale edge cases chýbali
- **Manuálne úpravy:**
  - Kontrola null hodnôt
  - Validácia oprávnení (napr. len vlastník môže upraviť skupinu)
  - Graceful degradation pri WebSocket chybách

#### **API Client (klient)**
- **Problém:** AI generovalo základný HTTP klient, ale chýbala robustnosť
- **Manuálne úpravy:**
  - Error handling pre sieťové chyby
  - Gson konfigurácia pre správne parsovanie JSON
  - Retry logic pre neúspešné požiadavky

### 7.3 Čo som sa z toho naučil

#### **Pozitíva práce s AI:**
1. **Rýchle prototypovanie** – AI pomohlo rýchlo vytvoriť základnú štruktúru projektu
2. **Best practices** – AI navrhlo správne použitie Spring Boot anotácií a JPA
3. **Konzistentnosť** – Generovaný kód bol konzistentný v názvoch a štruktúre
4. **Dokumentácia** – AI pomohlo s komentármi a základnou dokumentáciou

#### **Obmedzenia AI:**
1. **Kontext UI** – AI malo problém s JavaFX FXML a komplexnými layoutmi
2. **Threading** – AI často generovalo kód, ktorý nebral do úvahy JavaFX thread model
3. **Integrácia** – AI malo problém s integráciou rôznych častí (WebSocket + REST + UI)
4. **Edge cases** – AI často generovalo "happy path" kód bez ošetrenia chýb

#### **Najlepšie praktiky:**
1. **Iteratívny prístup** – Používať AI na generovanie základu, potom manuálne doladiť
2. **Kontrola kódu** – Vždy skontrolovať a otestovať AI-generovaný kód
3. **Testovanie** – Písať testy pre AI-generovaný kód, aby sa zabezpečila funkčnosť

### 7.4 Rozdelenie práce

| Komponent | AI Generované | Manuálne Doladené |
|-----------|---------------|------------------|
| REST Controllers | ✅ 50% | ✅ 500% (error handling) |
| JPA Entities | ✅ 70% | ✅ 30% (vzťahy) |
| Services | ✅ 50% | ✅ 50% (biznis logika) |
| WebSocket Config | ✅ 30% | ✅ 700% (JavaFX klient) |
| JavaFX UI | ✅ 30% | ✅ 70% (layout, styling) |
| API Client | ✅ 50% | ✅ 50% (robustnosť) |
| Deadline Reminders | ✅ 20% | ✅ 80% (timing, dedup) |
| Error Handling | ✅ 40% | ✅ 60% (edge cases) |

**Záver:** AI bolo veľmi užitočné pre generovanie základnej štruktúry a boilerplate kódu, ale manuálne doladenie bolo nevyhnutné pre funkčnosť, bezpečnosť a používateľskú skúsenosť.

---

## 8. Technológie a nástroje

### Frontend
- **JavaFX 21** – desktopové GUI
- **FXML** – deklaratívne definovanie UI
- **CSS** – štýlovanie komponentov

### Backend
- **Spring Boot 3.2.0** – server framework
- **Spring Data JPA** – databázový prístup
- **Hibernate** – ORM
- **Spring WebSocket** – real-time komunikácia

### Databáza
- **SQLite** – embedded databáza
- **Hibernate DDL Auto** – automatická migrácia

### Komunikácia
- **REST API** – HTTP/JSON
- **WebSocket** – real-time notifikácie a chat
- **Gson** – JSON serializácia/deserializácia

### Build a nástroje
- **Maven** – dependency management a build
- **JUnit 5** – unit testy
- **JaCoCo** – code coverage

---

## 9. Spustenie projektu

### Požiadavky
- Java 17 alebo vyššia
- Maven 3.6 alebo vyššia
- SQLite (automaticky sa vytvorí databáza)

### Krok 1: Spustenie servera

```bash
cd server
mvn spring-boot:run
```

Server sa spustí na `http://localhost:8080` (REST API) a `ws://localhost:8081` (WebSocket)

### Krok 2: Spustenie klienta

```bash
cd client
mvn javafx:run
```

### Prvé použitie

1. Spustite server (Krok 1)
2. Spustite klient (Krok 2)
3. V aplikácii kliknite na tab "Register"
4. Vytvorte nový účet (email, meno, heslo min. 6 znakov)
5. Prihláste sa pomocou vytvoreného účtu
6. Vytvorte novú skupinu pomocou tlačidla "Create Group"
7. Skupiny a úlohy sa zobrazia v dashboarde

---

```

### CI/CD Pipeline

CI/CD pipeline sa automaticky spúšťa pri push do `main` alebo `develop` branch. Pozri `.github/workflows/ci-cd.yml` pre detaily.

---

## 10. Dokumentácia

- [PRD.md](PRD.md) - Product Requirements Document
- [QUICKSTART.md](QUICKSTART.md) - Rýchly sprievodca na spustenie
- [INTELLIJ_SETUP.md](INTELLIJ_SETUP.md) - Spustenie v IntelliJ IDEA
- [NOTIFICATIONS_AND_CHAT.md](NOTIFICATIONS_AND_CHAT.md) - Dokumentácia notifikácií a chatu
- [TESTING.md](TESTING.md) - Dokumentácia testovania

---

## 12. Záver

Projekt **CALLSTUDY** predstavuje funkčnú ukážku moderného klient-server systému, ktorý spája viac technológií – databázu, REST API, real-time komunikáciu a desktopové GUI.

**Hlavné úspechy:**
- ✅ Plne funkčná REST API s validáciou
- ✅ Real-time notifikácie a chat cez WebSocket
- ✅ Bezpečná autentifikácia s bcrypt hashovaním
- ✅ Moderné JavaFX používateľské rozhranie
- ✅ Automatické deadline reminders
- ✅ File upload a zdieľanie materiálov

**Možné rozšírenia:**
- Mobilná aplikácia (React Native / Flutter)
- Integrácia s Google OAuth2
- Pokročilejšia analytika a štatistiky
- Export dát (PDF, Excel)
- Email notifikácie

---

**Autor:** Jaroslav Birčák, Tomáš Cingel
**Dátum:** 2025
**Licencia:** Vzdelávacie účely
