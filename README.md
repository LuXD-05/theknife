# TheKnife Project

Applicazione client/server per la consultazione e recensione di ristoranti (dataset Michelin).
È organizzata come **monorepo Maven multi-modulo**:

- **theknife-common** — DTO e protocollo dei messaggi WebSocket condivisi tra client e server.
- **theknife-backend** — backend **Quarkus** che espone le operazioni via **WebSocket**, con persistenza su **PostgreSQL** (Hibernate ORM/Panache + Flyway). Gestisce più client contemporaneamente e invia **notifiche in tempo reale** (nuove recensioni, risposte del ristoratore, modifiche ai ristoranti) a tutti i client connessi.
- **theknife-frontend** — client desktop **JavaFX**: comunica con il backend via WebSocket.

## Architettura

```
JavaFX client  ──WebSocket (JSON)──►  Quarkus backend  ──JDBC──►  PostgreSQL
                ◄──eventi realtime──
```

- Un unico endpoint WebSocket (`/ws`). Ogni messaggio è una "envelope" JSON (`REQUEST`/`RESPONSE`/`EVENT`) con un `correlationId` per correlare richiesta e risposta.
- Le operazioni mutanti girano in transazione; al commit il backend fa il **broadcast** dell'evento agli altri client.
- Le password sono validate e hashate **solo lato server** (BCrypt): l'hash non lascia mai il backend.

### Requisiti di sistema

- **Java 21** (JDK)
- **Maven** (o il Maven Wrapper)
- **Docker** + Docker Compose (per PostgreSQL)
- Minimo 512 MB di memoria

## Avvio (in ordine)

### 1. Avviare PostgreSQL

Il `docker-compose.yml` sta nel modulo `theknife-backend` (è infrastruttura del backend). Dalla root del progetto:

```bash
docker compose -f theknife-backend/docker-compose.yml up -d postgres
```

(oppure `cd theknife-backend && docker compose up -d postgres`). Crea un database `theknife` (utente/password `theknife`) sulla porta `5432`.

### 2. Avviare il backend

```bash
mvn -pl theknife-backend quarkus:dev
```

Al primo avvio il backend:
- applica lo **script di inizializzazione del DB** (Flyway, `theknife-backend/src/main/resources/db/migration/V1__init.sql`);
- esegue il **seed** dei dati a partire dai file in `theknife-backend/data/` (CSV Michelin + JSON), ~17.700 ristoranti. Il seed parte solo se le tabelle sono vuote.

Il backend resta in ascolto su `http://localhost:8080` (WebSocket su `ws://localhost:8080/ws`).

### 3. Avviare il client JavaFX

```bash
mvn -pl theknife-frontend javafx:run
```

In alternativa, il jar eseguibile (con dipendenze incluse):

```bash
java -jar theknife-frontend/target/theknife-frontend-0.0.1-jar-with-dependencies.jar [URL_SERVER]
```

**Indirizzo del server** — il client lo risolve in quest'ordine:

1. **argomento all'avvio**: posizionale (`java -jar ... wss://xxxx.ngrok-free.app`) oppure `--backend-url=...`;
2. system property `-Dtheknife.backend.url=...`;
3. se non fornito, viene **chiesto in una finestra all'avvio** (primo step, prima del login).

L'indirizzo viene normalizzato automaticamente: si può incollare direttamente la URL pubblica di ngrok (`https://xxxx.ngrok-free.app`) e diventa `wss://xxxx.ngrok-free.app/ws`. In caso di errore di connessione l'indirizzo viene richiesto di nuovo.

## Demo remota: pubblicare il server con ngrok

Per far collegare client da altre macchine (es. in sede d'esame) si espone il backend con **ngrok** (in Docker). Il backend gira sull'host sulla porta `8080`; ngrok lo pubblica su una URL pubblica.

```bash
# serve un account ngrok (gratuito) e il relativo authtoken
NGROK_AUTHTOKEN=<il-tuo-token> docker compose -f theknife-backend/docker-compose.yml up -d ngrok

# leggere la URL pubblica assegnata:
curl -s localhost:4040/api/tunnels        # oppure apri http://localhost:4040
```

Sui client si passa quella URL:

```bash
java -jar theknife-frontend-0.0.1-jar-with-dependencies.jar https://xxxx.ngrok-free.app
```

> Nota: la URL di ngrok cambia ad ogni avvio del tunnel (piano free). Il backend deve essere già in ascolto su `:8080` prima di avviare il servizio `ngrok`.

## Compilazione

Compilare l'intero progetto:

```bash
mvn install
```

I moduli `theknife-common` e `theknife-backend` si compilano anche offline una volta scaricate le dipendenze; il fat-jar del frontend richiede il `maven-assembly-plugin`.

## Portabilità (multi-arch)

Il client JavaFX è multipiattaforma: macOS (Intel `x86_64` e Apple Silicon `aarch64`), Windows e Linux (`x86_64`/`aarch64`).

- Il `pom.xml` del frontend usa profili attivati automaticamente in base a **sistema operativo e architettura** del build host (`javafx-mac-aarch64`, `javafx-mac-x86_64`, `javafx-windows`, `javafx-linux-x86_64`, `javafx-linux-aarch64`), che impostano il classifier nativo JavaFX corretto (proprietà `javafx.platform`). Non serve alcuna configurazione manuale: basta compilare sulla macchina di destinazione.
- ControlsFX usa API interne di JavaFX; i necessari `--add-exports` sono già configurati nel `javafx-maven-plugin`, quindi `mvn -pl theknife-frontend javafx:run` funziona senza flag aggiuntivi.
- ⚠️ Il **fat-jar** (`jar-with-dependencies`) include le librerie native **della sola piattaforma su cui è stato compilato**. Per distribuirlo su un'altra piattaforma, ricompilarlo lì (`mvn -pl theknife-frontend -am package`). Il classifier può essere forzato con `-Djavafx.platform=<win|mac|mac-aarch64|linux|linux-aarch64>`.

## Utenti di esempio (seed)

| username        | ruolo        |
|-----------------|--------------|
| `mmordente-cli` | CLIENTE      |
| `mmordente-ris` | RISTORATORE  |
| `lucio-cl`, `nardo-cl`, `lucchetto-cl` | CLIENTE |
| `lucio-ris`, `nardo-ris`, `lucchetto-ris` | RISTORATORE |

(La password di `mmordente-cli` è `password`. Il ristorante "La Perla" è di proprietà di `mmordente-ris`.)

## Note

- Per arrestare il database: `docker compose -f theknife-backend/docker-compose.yml down` (aggiungere `-v` per cancellare anche i dati e ri-eseguire il seed al successivo avvio).
- Lo schema del DB è gestito da Flyway; Hibernate è in modalità `validate` (non modifica lo schema).
- Il seed può essere disabilitato con `-Dtheknife.seed.enabled=false`.
