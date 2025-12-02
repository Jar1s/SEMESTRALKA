# Database Migration Guide

## Problém s SQLite a Hibernate DDL Auto Update

SQLite má obmedzenia pri `ALTER TABLE` - nemôže jednoducho pridať nové stĺpce do existujúcej tabuľky v niektorých prípadoch. Keď pridáte nové polia do entity tried, Hibernate s `ddl-auto=update` nemusí správne aktualizovať schému.

## Riešenie

### Pre Development (s testovacími dátami)

1. **Vymazať databázu:**
   ```bash
   rm server/study_platform.db
   # alebo
   rm study_platform.db  # ak je v root priečinku
   ```

2. **Nastaviť `ddl-auto=create` v `application.properties`:**
   ```properties
   spring.jpa.hibernate.ddl-auto=create
   ```

3. **Reštartovať server** - databáza sa vytvorí s novou schémou

4. **Vrátiť späť na `update`:**
   ```properties
   spring.jpa.hibernate.ddl-auto=update
   ```

### Pre Production (s dôležitými dátami)

**Možnosť 1: Manuálna migrácia**

1. Zálohovať databázu:
   ```bash
   cp study_platform.db study_platform.db.backup
   ```

2. Spustiť SQL migračný skript:
   ```sql
   ALTER TABLE users ADD COLUMN auth_provider VARCHAR(20) DEFAULT 'LOCAL';
   ALTER TABLE users ADD COLUMN google_id VARCHAR(255);
   -- password_hash už existuje, ale môže byť nullable
   ```

3. Overiť migráciu:
   ```bash
   sqlite3 study_platform.db ".schema users"
   ```

**Možnosť 2: Použiť migračný nástroj**

Pre produkciu odporúčame použiť:
- **Flyway** - https://flywaydb.org/
- **Liquibase** - https://www.liquibase.org/

Tieto nástroje poskytujú lepšiu kontrolu nad migráciami a históriou zmien.

## Aktuálne zmeny v User entite

Pridané stĺpce:
- `auth_provider` (VARCHAR, NOT NULL, DEFAULT 'LOCAL')
- `google_id` (VARCHAR, nullable, unique)
- `password_hash` (teraz nullable - pre OAuth2 používateľov)

## Kontrola schémy

Skontrolovať aktuálnu schému:
```bash
sqlite3 study_platform.db ".schema users"
```

Očakávaná schéma:
```sql
CREATE TABLE users (
    user_id INTEGER PRIMARY KEY,
    email VARCHAR NOT NULL UNIQUE,
    name VARCHAR NOT NULL,
    password_hash VARCHAR,
    google_id VARCHAR UNIQUE,
    auth_provider VARCHAR NOT NULL,
    created_at TIMESTAMP
);
```

