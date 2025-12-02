# IntelliJ IDEA Setup pre Client Module

## Run Configuration

1. **Vytvorte novú Application Run Configuration:**
   - Run → Edit Configurations...
   - Kliknite na "+" → Application
   - Nastavte:
     - **Name:** CALLSTUDY Client
     - **Main class:** `sk.ikts.client.CollaborativeStudyPlatform`
     - **Module:** `client`
     - **Use classpath of module:** `client`
     - **Working directory:** `$MODULE_DIR$`

2. **VM options** (ak je potrebné):
   ```
   --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml
   ```
   Alebo použite Maven JavaFX plugin, ktorý to nastaví automaticky.

## Maven Reload

1. **Reload Maven Project:**
   - View → Tool Windows → Maven
   - Kliknite na "Reload All Maven Projects" (ikona obnovenia)
   - Alebo: File → Reload Maven Project

2. **Spustenie cez Maven:**
   - Maven tool window → client → Plugins → javafx → javafx:run
   - Alebo terminál: `cd client && mvn javafx:run`

## Troubleshooting

Ak dostanete `ClassNotFoundException`:
1. Skontrolujte, či je trieda skompilovaná: `target/classes/sk/ikts/client/CollaborativeStudyPlatform.class`
2. Skontrolujte, či je `client` modul správne označený ako Maven modul
3. Skontrolujte, či sú source roots správne nastavené:
   - File → Project Structure → Modules → client → Sources
   - Mal by byť: `src/main/java` ako Sources
   - Mal by byť: `src/main/resources` ako Resources

4. Skúste:
   - Build → Rebuild Project
   - File → Invalidate Caches / Restart

