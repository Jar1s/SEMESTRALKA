#!/bin/bash
# Script na spustenie klienta pomocou JavaFX Maven pluginu

cd "$(dirname "$0")"

echo "Spúšťam Collaborative Study Platform klient..."
echo ""

# Spusti pomocou JavaFX Maven pluginu
mvn javafx:run

