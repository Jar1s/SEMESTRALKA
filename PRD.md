# 📘 PRD – CALLSTUDY  
**Autor:** Jaroslav Birčák, Tomas Cingel
**Predmet:** Semestrálna práca – Klient-server architektúra  
**Rok:** 2. ročník  

---

## 1. Úvod a ciele projektu

Cieľom projektu je vytvoriť **platformu na kolaboratívne štúdium**, ktorá umožní študentom spolupracovať v skupinách, zdieľať materiály, sledovať úlohy a komunikovať v reálnom čase.  
Projekt kombinuje prácu s **databázou, REST API, real-time WebSocket komunikáciu a používateľské rozhranie v JavaFX**.  

Hlavnou myšlienkou je umožniť študentom:
- vytvárať študijné skupiny,  
- prideľovať úlohy,  
- zdieľať študijné materiály,  
- a sledovať aktivitu členov tímu.  

---

## 2. Architektúra systému

Aplikácia je založená na **modeli klient-server**.  
Klient (desktopová aplikácia) komunikuje so serverom pomocou REST API a WebSocketov.

### 2.1 Architektonické vrstvy:
- **Klient (JavaFX)**  
  - Zodpovedá za interakciu s používateľom.  
  - Posiela požiadavky na server (napr. prihlásenie, vytvorenie úlohy).  
  - Zobrazuje údaje (skupiny, úlohy, členovia).  

- **Server (Spring Boot)**  
  - Spracúva požiadavky klienta a pracuje s databázou.  
  - Implementuje REST API a WebSocket pre notifikácie.  
  - Obsahuje biznis logiku (napr. kontrola oprávnení, termíny úloh).  

- **Databáza (SQLite)**  
  - Ukladá údaje o používateľoch, skupinách, úlohách, materiáloch a aktivitách.  

---

## 3. Použité technológie

| Vrstva | Technológia | Účel |
|--------|--------------|------|
| Klient | JavaFX | GUI aplikácia, používateľská interakcia |
| Server | Spring Boot | REST API, biznis logika |
| Databáza | SQLite | Ukladanie dát |
| Komunikácia | REST API + WebSocket | Prenos dát, notifikácie |
| Build systém | Maven | Závislosti, kompilácia, spúšťanie |

---

## 4. Návrh databázy

### 4.1 Tabuľky

**USERS**  
- `user_id` (PK), `name`, `email`, `password_hash`  
→ uchováva používateľov, heslá sú hashované (bcrypt).

**GROUPS**  
- `group_id` (PK), `name`, `description`, `created_by`, `created_at`  
→ informácie o študijných skupinách.

**MEMBERSHIPS**  
- `membership_id` (PK), `user_id`, `group_id`, `role`, `joined_at`  
→ prepojenie používateľov a skupín, vrátane roly (member/admin).

**TASKS**  
- `task_id` (PK), `group_id`, `created_by`, `title`, `description`, `status`, `deadline`, `created_at`  
→ úlohy v rámci skupiny, stav: OPEN / IN_PROGRESS / DONE.

**RESOURCES**  
- `resource_id` (PK), `group_id`, `uploaded_by`, `title`, `type`, `path_or_url`, `uploaded_at`  
→ zdieľané materiály alebo odkazy.

**ACTIVITY_LOG**  
- `log_id` (PK), `user_id`, `action`, `timestamp`, `details`  
→ zaznamenáva aktivitu používateľov pre analytiku.

---

## 5. Hlavné funkcie aplikácie

1. **Registrácia a prihlásenie používateľov**  
   - Ukladanie hesiel s bcrypt hashovaním.  
   - Validácia emailu a hesla.  

2. **Správa profilov**  
   - Úprava mena, emailu, hesla.  

3. **Správa študijných skupín**  
   - Vytvorenie skupiny, pridanie členov, úprava informácií.  

4. **Úlohy v skupine**  
   - Pridávanie úloh, menenie statusu, sledovanie termínov.  

5. **Zdieľanie materiálov**  
   - Pridanie odkazov alebo nahranie súborov.  

6. **Real-time notifikácie (WebSocket)**  
   - Informovanie o nových úlohách, zmenách alebo členoch.  

7. **Analytika a prehľady**  
   - Počet splnených úloh, aktivita členov, grafické štatistiky.  

---

## 6. API a komunikácia

### 6.1 REST API (základné endpointy)

| Typ | Endpoint | Popis |
|------|-----------|--------|
| POST | `/api/users/register` | registrácia používateľa |
| POST | `/api/users/login` | prihlásenie používateľa |
| GET | `/api/groups` | zobrazenie všetkých skupín |
| POST | `/api/groups` | vytvorenie novej skupiny |
| GET | `/api/groups/{id}` | detaily skupiny |
| PUT | `/api/groups/{id}` | úprava informácií o skupine |
| DELETE | `/api/groups/{id}` | odstránenie skupiny |
| POST | `/api/tasks` | vytvorenie úlohy |
| PUT | `/api/tasks/{id}/status` | zmena statusu úlohy |
| GET | `/api/resources/{group_id}` | načítanie zdieľaných materiálov |

### 6.2 WebSocket (notifikácie)

- Kanál: `/ws/notifications`  
- Upozornenia:
  - nová úloha,  
  - nový člen,  
  - zmenený status úlohy.  

---

## 7. Používateľské rozhranie (JavaFX)

Plánované okná:
- **Login / Register window**  
- **Dashboard – prehľad skupín a úloh**  
- **Group detail view** (členovia, úlohy, materiály)  
- **Task editor**  
- **Statistics view**  

Použité budú komponenty `TableView`, `ListView`, `TextField`, `Button`, `Label` a `Charts` na vizualizáciu údajov.  

---

## 8. Bezpečnosť a validácia

- Heslá sa ukladajú hashované (bcrypt).  
- Každé API má kontrolu prístupu (JWT token alebo session).  
- Validácia vstupov na strane klienta aj servera.  
- Ochrana proti SQL injection a XSS.  

---

## 9. Záver

Projekt **CALLSTUDY** predstavuje funkčnú ukážku moderného klient-server systému,  
ktorý spája viac technológií – databázu, REST API, real-time komunikáciu a desktopové GUI.  

Pomohol mi pochopiť:
- ako funguje architektúra klient-server,  
- ako sa prenášajú dáta medzi JavaFX a Spring Boot,  
- a ako sa rieši bezpečnosť a validácia v praxi.  

Projekt by sa dal ďalej rozšíriť o mobilnú aplikáciu alebo integráciu s Google OAuth2.

---

## 10. Štruktúra priečinkov