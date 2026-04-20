@echo off
cd /d "%~dp0"

echo.
echo  Compilando...
if not exist out mkdir out

javac -encoding UTF-8 -d out src\*.java
if %ERRORLEVEL% neq 0 (
    echo.
    echo  [ERROR] La compilacion ha fallado.
    pause
    exit /b 1
)

echo  Compilacion correcta.
echo.
java -cp out JuegoJJK
pause
