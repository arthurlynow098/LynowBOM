@echo off
rem Abre o LynowBOM em modo demonstracao (H2 em memoria + planilha de exemplo).
rem Requer Java 21 e Maven no PATH.
cd /d "%~dp0"
start "" http://localhost:8080
mvn -q spring-boot:run -Dspring-boot.run.profiles=demo
