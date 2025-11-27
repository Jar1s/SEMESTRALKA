# 📘 Collaborative Study Platform

Platforma na kolaboratívne štúdium - klient-server aplikácia pre študentov.

**Autor:** Jaroslav Birčák  
**Predmet:** Semestrálna práca – Klient-server architektúra

## 🏗️ Štruktúra projektu

```
collaborative-study-platform/
├── client/          # JavaFX desktop aplikácia
├── server/          # Spring Boot server s REST API
├── database/        # SQLite databáza a migrácie
├── docs/            # Dokumentácia
├── PRD.md           # Product Requirements Document
└── RULES.md         # Pravidlá pre prácu s AI
```

## 🚀 Spustenie projektu

### Požiadavky
- Java 17 alebo vyššia
- Maven 3.6 alebo vyššia
- SQLite (automaticky sa vytvorí databáza pri prvom spustení)

### Krok 1: Spustenie servera

Otvorte terminál a spustite:
```bash
cd server
mvn spring-boot:run
```

Server sa spustí na `http://localhost:8080`

**Poznámka:** Pri prvom spustení sa automaticky vytvorí SQLite databáza `study_platform.db` v priečinku `server/`.

### Krok 2: Spustenie klienta

V novom termináli spustite:
```bash
cd client
mvn javafx:run
```

Alebo ak máte problém s JavaFX, môžete použiť:
```bash
cd client
mvn clean compile
java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -cp target/classes:target/dependency/* sk.ikts.client.CollaborativeStudyPlatform
```

### Prvé použitie

1. Spustite server (Krok 1)
2. Spustite klient (Krok 2)
3. V aplikácii kliknite na tab "Register"
4. Vytvorte nový účet (email, meno, heslo min. 6 znakov)
5. Prihláste sa pomocou vytvoreného účtu
6. Vytvorte novú skupinu pomocou tlačidla "Create Group"
7. Skupiny a úlohy sa zobrazia v dashboarde

### Testovanie API

Server poskytuje REST API na `http://localhost:8080/api`:
- `POST /api/users/register` - registrácia
- `POST /api/users/login` - prihlásenie
- `GET /api/groups` - zoznam skupín
- `POST /api/groups` - vytvorenie skupiny
- `GET /api/tasks/group/{groupId}` - úlohy skupiny
- `POST /api/tasks` - vytvorenie úlohy

WebSocket notifikácie: `ws://localhost:8080/ws/notifications`

## 📋 Technológie

- **Klient:** JavaFX 21
- **Server:** Spring Boot 3.2.0
- **Databáza:** SQLite
- **Komunikácia:** REST API + WebSocket
- **Build:** Maven

## 📚 Dokumentácia

- [PRD.md](PRD.md) - Product Requirements Document
- [RULES.md](RULES.md) - Pravidlá pre prácu s AI
- [QUICKSTART.md](QUICKSTART.md) - Rýchly sprievodca na spustenie
- [INTELLIJ_SETUP.md](INTELLIJ_SETUP.md) - **Spustenie v IntelliJ IDEA** ⭐

