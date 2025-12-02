#!/bin/bash
# Script na spustenie klienta pomocou JavaFX Maven pluginu

cd "$(dirname "$0")"

echo "Spúšťam CALLSTUDY klient..."
echo ""

# Spusti pomocou JavaFX Maven pluginu
mvn javafx:run

