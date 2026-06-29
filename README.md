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

```bash
docker compose up -d postgres
```

Crea un database `theknife` (utente/password `theknife`) sulla porta `5432`.

### 2. Avviare il backend

```bash
mvn -pl theknife-backend quarkus:dev
```

Al primo avvio il backend:
- applica lo **script di inizializzazione del DB** (Flyway, `theknife-backend/src/main/resources/db/migration/V1__init.sql`);
- esegue il **seed** dei dati a partire dai file in `data/` (CSV Michelin + JSON), ~17.700 ristoranti. Il seed parte solo se le tabelle sono vuote.

Il backend resta in ascolto su `http://localhost:8080` (WebSocket su `ws://localhost:8080/ws`).

### 3. Avviare il client JavaFX

```bash
mvn -pl theknife-frontend javafx:run
```

In alternativa, il jar eseguibile (con dipendenze incluse):

```bash
java -jar theknife-frontend/target/theknife-frontend-0.0.1-jar-with-dependencies.jar
```

L'URL del backend è configurabile con `-Dtheknife.backend.url=ws://host:porta/ws` (default `ws://localhost:8080/ws`).

## Compilazione

Compilare l'intero progetto:

```bash
mvn install
```

I moduli `theknife-common` e `theknife-backend` si compilano anche offline una volta scaricate le dipendenze; il fat-jar del frontend richiede il `maven-assembly-plugin`.

## Utenti di esempio (seed)

| username        | ruolo        |
|-----------------|--------------|
| `mmordente-cli` | CLIENTE      |
| `mmordente-ris` | RISTORATORE  |
| `lucio-cl`, `nardo-cl`, `lucchetto-cl` | CLIENTE |
| `lucio-ris`, `nardo-ris`, `lucchetto-ris` | RISTORATORE |

(La password di `mmordente-cli` è `password`. Il ristorante "La Perla" è di proprietà di `mmordente-ris`.)

## Note

- Per arrestare il database: `docker compose down` (aggiungere `-v` per cancellare anche i dati e ri-eseguire il seed al successivo avvio).
- Lo schema del DB è gestito da Flyway; Hibernate è in modalità `validate` (non modifica lo schema).
- Il seed può essere disabilitato con `-Dtheknife.seed.enabled=false`.
