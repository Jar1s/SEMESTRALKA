# 🚀 Spustenie projektu v IntelliJ IDEA

## Krok 1: Otvorenie projektu

1. Otvorte IntelliJ IDEA
2. **File → Open** alebo **File → Open Project**
3. Vyberte priečinok: `/Users/jaroslav/Documents/DOCUMENTS/SCHOOL/3rd Semester/IKTS/Semestralka`
4. IntelliJ automaticky rozpozná Maven projekt a začne indexovať

## Krok 2: Čakanie na indexovanie

- Počkajte, kým IntelliJ dokončí indexovanie a sťahovanie Maven závislostí
- V pravom dolnom rohu uvidíte progress bar
- Môže to trvať 2-5 minút pri prvom otvorení

## Krok 3: Nastavenie Java SDK

1. **File → Project Structure** (⌘;)
2. V **Project** sekcii:
   - **Project SDK:** Vyberte Java 17 alebo vyššiu
   - **Project language level:** 17
3. Kliknite **OK**

## Krok 4: Spustenie servera

1. Otvorte súbor: `server/src/main/java/sk/ikts/server/ServerApplication.java`
2. Kliknite pravým tlačidlom na `ServerApplication` class
3. Vyberte **Run 'ServerApplication'** alebo stlačte **⌃⇧R**
4. Server sa spustí a v konzole uvidíte:
   ```
   Started ServerApplication in X.XXX seconds
   ```
5. Server beží na: `http://localhost:8080`

## Krok 5: Spustenie klienta

1. Otvorte súbor: `client/src/main/java/sk/ikts/client/CollaborativeStudyPlatform.java`
2. Kliknite pravým tlačidlom na `CollaborativeStudyPlatform` class
3. Vyberte **Run 'CollaborativeStudyPlatform'** alebo stlačte **⌃⇧R**
4. Aplikácia sa otvorí automaticky

## ⚠️ Dôležité poznámky

### Ak sa klient nespustí (JavaFX problém):

1. **File → Project Structure → Modules**
2. Vyberte modul `client`
3. V **Dependencies** tab:
   - Skontrolujte, či sú JavaFX moduly pridané
   - Ak nie, pridajte ich manuálne alebo použite:

**Run Configuration pre klient:**
1. **Run → Edit Configurations**
2. Vytvorte novú **Application** konfiguráciu
3. Nastavte:
   - **Name:** Client
   - **Main class:** `sk.ikts.client.CollaborativeStudyPlatform`
   - **Module:** `client`
   - **VM options:** 
     ```
     --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
     ```
   (Nahraďte `/path/to/javafx-sdk/lib` skutočnou cestou k JavaFX SDK)

### Ak Maven závislosti nie sú stiahnuté:

1. **View → Tool Windows → Maven**
2. Kliknite na ikonu **Reload All Maven Projects** (🔄)
3. Alebo: **File → Invalidate Caches / Restart**

## 🎯 Rýchle spustenie (po nastavení)

1. Spustite **ServerApplication** (Run alebo Debug)
2. Počkajte na správu "Started ServerApplication"
3. Spustite **CollaborativeStudyPlatform** (Run alebo Debug)
4. Aplikácia sa otvorí

## 📝 Prvé použitie aplikácie

1. V aplikácii kliknite na tab **"Register"**
2. Vytvorte účet:
   - Email: napr. `test@example.com`
   - Name: napr. `Test User`
   - Password: min. 6 znakov
3. Kliknite **"Register"**
4. Prejdite na tab **"Login"**
5. Prihláste sa
6. Kliknite **"Create Group"** a vytvorte skupinu
7. Skupiny a úlohy sa zobrazia v dashboarde

## 🐛 Riešenie problémov

### Server sa nespustí
- Skontrolujte, či port 8080 nie je obsadený
- Skontrolujte Java verziu (musí byť 17+)
- Skontrolujte Maven závislosti

### Klient sa nespustí
- Skontrolujte, či je server spustený
- Skontrolujte JavaFX moduly
- Skontrolujte Run Configuration

### WebSocket notifikácie nefungujú
- Skontrolujte, či server beží
- Skontrolujte konzolu pre chyby
- Notifikácie fungujú len keď je klient prihlásený










