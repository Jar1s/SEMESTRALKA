#!/bin/bash
# Script na spustenie klienta
cd "$(dirname "$0")/client"
echo "Spúšťam klient..."
echo "Poznámka: Potrebujete Maven nainštalovaný a v PATH"
echo ""
mvn javafx:run





