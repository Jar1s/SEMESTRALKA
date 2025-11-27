# 🚀 Quick Start Guide

Rýchly sprievodca na spustenie Collaborative Study Platform.

## Predpoklady

- Java 17+ (skontrolujte: `java -version`)
- Maven 3.6+ (skontrolujte: `mvn -version`)

## Spustenie v 2 krokoch

### 1. Spustite server

```bash
cd server
mvn spring-boot:run
```

Počkajte, kým sa zobrazí:
```
Started ServerApplication in X.XXX seconds
```

### 2. Spustite klient (v novom termináli)

```bash
cd client
mvn javafx:run
```

Aplikácia sa otvorí automaticky.

## Prvé kroky v aplikácii

1. **Registrácia:** Kliknite na tab "Register" a vytvorte účet
2. **Prihlásenie:** Prihláste sa pomocou vytvoreného účtu
3. **Vytvorenie skupiny:** Kliknite na "Create Group" a zadajte názov
4. **Pridanie úlohy:** (Funkcionalita bude doplnená)

## Riešenie problémov

### Server sa nespustí
- Skontrolujte, či port 8080 nie je obsadený
- Skontrolujte Java verziu: `java -version` (musí byť 17+)

### Klient sa nespustí
- Skontrolujte, či je server spustený
- Skontrolujte JavaFX: `mvn javafx:run` by malo fungovať
- Ak nie, nainštalujte JavaFX SDK a upravte classpath

### WebSocket notifikácie nefungujú
- Skontrolujte, či server beží
- Skontrolujte konzolu pre chybové hlásenia
- Notifikácie fungujú len keď je klient prihlásený

## Zastavenie aplikácie

- **Server:** Stlačte `Ctrl+C` v termináli so serverom
- **Klient:** Zatvorte okno aplikácie

## Databáza

SQLite databáza sa automaticky vytvorí v `server/study_platform.db` pri prvom spustení.

Pre vymazanie dát jednoducho vymažte súbor `study_platform.db` a reštartujte server.










