#!/bin/bash
# Script na spustenie servera
cd "$(dirname "$0")/server"
echo "Spúšťam server..."
echo "Poznámka: Potrebujete Maven nainštalovaný a v PATH"
echo ""
mvn spring-boot:run










