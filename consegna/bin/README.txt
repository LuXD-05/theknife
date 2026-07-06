TheKnife - Pacchetto eseguibile
================================

Contenuto di questa cartella:
  - theknife-backend.jar               Backend (Quarkus, uber-jar autonomo)
  - theknife-frontend-win.jar          Client JavaFX per Windows
  - theknife-frontend-linux.jar        Client JavaFX per Linux (x86_64)
  - theknife-frontend-mac.jar          Client JavaFX per macOS Intel (x86_64)
  - theknife-frontend-mac-aarch64.jar  Client JavaFX per macOS Apple Silicon (arm64)
  - data/                              Dati per l'importazione iniziale (seed)
  - docker-compose.yml                 Definizione del container PostgreSQL
  - avvia_postgres.(sh|bat)            Avvia il database
  - avvia_ngrok.(sh|bat)               Avvia ngrok per il tunneling (facoltativo)
  - avvia_server.(sh|bat)              Avvia il backend
  - avvia_client.(sh|bat)              Avvia il client

Prerequisiti:
  - JDK 21 installato e "java" nel PATH.
  - Docker installato e in esecuzione (per il database PostgreSQL).

Ordine di avvio:
  1) avvia_postgres   -> avvia PostgreSQL (porta 5432).
  2) avvia_ngrok      -> avvia ngrok per il tunneling. NOTA: impostare variabile d'ambiente
                         NGROK_AUTHTOKEN per il token di autenticazione.
  3) avvia_server     -> avvia il backend (porta 8080). Al primo avvio, se il
                         database e' vuoto, i dati vengono importati da "data/".

  4) avvia_client     -> avvia il client. Come indirizzo del server usare:
                            ws://localhost:8080
                         L'indirizzo puo' essere passato come argomento
                         (es. ./avvia_client.sh ws://localhost:8080) oppure
                         inserito nella finestra che appare all'avvio.

Uso su Windows:  eseguire i file .bat
Uso su Linux/macOS:  eseguire i file .sh   (es.  ./avvia_server.sh )

Note:
  - avvia_client.sh sceglie automaticamente il jar giusto per il sistema
    operativo/architettura. Per architetture non incluse (es. Linux arm64)
    ricompilare il frontend:
      mvn -pl theknife-frontend -am package -Djavafx.platform=linux-aarch64
  - I jar del client contengono le librerie native JavaFX della sola
    piattaforma indicata nel nome.
