#!/bin/bash
# Avvia il client JavaFX TheKnife. Richiede JDK 21.
# Sceglie automaticamente il jar corretto per il sistema operativo/architettura.
# Si puo' passare l'URL del server come argomento, es:
#   ./avvia_client.sh ws://localhost:8080
# Se omesso, il client chiedera' l'indirizzo in una finestra all'avvio.
cd "$(dirname "$0")"

OS="$(uname -s)"
ARCH="$(uname -m)"

case "$OS" in
    Darwin)
        if [ "$ARCH" = "arm64" ]; then
            JAR="theknife-frontend-mac-aarch64.jar"
        else
            JAR="theknife-frontend-mac.jar"
        fi
        ;;
    Linux)
        JAR="theknife-frontend-linux.jar"
        ;;
    *)
        echo "Sistema operativo non supportato da questo script: $OS"
        echo "Su Windows usa avvia_client.bat."
        exit 1
        ;;
esac

if [ ! -f "$JAR" ]; then
    echo "Jar non trovato: $JAR"
    echo "Per architetture non incluse, ricompilare il frontend con:"
    echo "  mvn -pl theknife-frontend -am package -Djavafx.platform=<win|mac|mac-aarch64|linux|linux-aarch64>"
    exit 1
fi

java -jar "$JAR" "$@"
