#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
mkdir -p out
echo ""
echo " Compilando..."
javac -encoding UTF-8 -d out src/*.java
echo " Compilacion correcta."
echo ""
java -cp out JuegoJJK
