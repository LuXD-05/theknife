#!/bin/bash
# Avvia il backend TheKnife (Quarkus). Richiede JDK 21 e PostgreSQL gia' avviato
# (vedi avvia_postgres.sh). Al primo avvio, se il database e' vuoto, importa i dati
# dalla cartella "data" presente in questa directory.
cd "$(dirname "$0")"
java -jar theknife-backend.jar
