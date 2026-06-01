#!/bin/bash

# 1. Vérification des variables d'environnement
ENV_FILE=".maestro/.env"

# On se place à la racine du projet si on lance le script depuis le dossier scripts
cd "$(dirname "$0")/../.."

if [ ! -f "$ENV_FILE" ]; then
    echo "❌ ERREUR : Le fichier $ENV_FILE est introuvable."
    echo "   Veuillez créer ce fichier sur le modèle suivant :"
    echo "   MAESTRO_APP_ID=\"io.element.android.x.debug\""
    echo "   MAESTRO_APP_NAME=\"Tchap dbg\""
    echo "   MAESTRO_USERNAME=\"votre_user\""
    echo "   MAESTRO_PASSWORD=\"votre_mdp\""
    echo "   MAESTRO_RECOVERY_KEY=\"votre_cle\""
    echo "   MAESTRO_ROOM_NAME=\"MyRoom\""
    echo "   MAESTRO_INVITEE1_MXID=\"user2\""
    echo "   MAESTRO_INVITEE2_MXID=\"user3\""
    exit 1
fi

echo "📄 Lecture des variables depuis $ENV_FILE..."
# Charger les variables du fichier (set -a permet de les exporter automatiquement)
set -a
source "$ENV_FILE"
set +a

# Liste des variables obligatoires
REQUIRED_VARS=(
    "MAESTRO_APP_ID"
    "MAESTRO_APP_NAME"
    "MAESTRO_USERNAME"
    "MAESTRO_PASSWORD"
    "MAESTRO_RECOVERY_KEY"
    "MAESTRO_ROOM_NAME"
    "MAESTRO_INVITEE1_MXID"
    "MAESTRO_INVITEE2_MXID"
)

MISSING_VARS=0
for var in "${REQUIRED_VARS[@]}"; do
    if [ -z "${!var}" ]; then
        echo "❌ ERREUR : La variable $var est manquante ou vide dans $ENV_FILE."
        MISSING_VARS=1
    fi
done

if [ $MISSING_VARS -eq 1 ]; then
    echo "Arrêt du script en raison de variables manquantes."
    exit 1
fi

# 2. Recherche et démarrage de l'émulateur
echo "🔍 Recherche des émulateurs disponibles..."
EMULATOR_CMD="$HOME/Library/Android/sdk/emulator/emulator"
AVDS=($($EMULATOR_CMD -list-avds))
NUM_AVDS=${#AVDS[@]}

if [ $NUM_AVDS -eq 0 ]; then
    echo "❌ Aucun émulateur trouvé. Veuillez en créer un dans Android Studio."
    exit 1
elif [ $NUM_AVDS -eq 1 ]; then
    SELECTED_AVD="${AVDS[0]}"
    read -p "Un seul émulateur trouvé ($SELECTED_AVD). Appuyez sur Entrée pour le démarrer... "
else
    echo "Émulateurs disponibles :"
    for i in "${!AVDS[@]}"; do
        echo "$((i+1)). ${AVDS[$i]}"
    done
    
    while true; do
        read -p "Quel émulateur voulez-vous utiliser ? (Entrez un numéro de 1 à $NUM_AVDS) : " CHOIX
        if [[ "$CHOIX" =~ ^[0-9]+$ ]] && [ "$CHOIX" -ge 1 ] && [ "$CHOIX" -le "$NUM_AVDS" ]; then
            SELECTED_AVD="${AVDS[$((CHOIX-1))]}"
            break
        else
            echo "Choix invalide. Veuillez réessayer."
        fi
    done
fi

echo "📱 Démarrage de l'émulateur $SELECTED_AVD en arrière-plan..."
$EMULATOR_CMD -avd "$SELECTED_AVD" > /dev/null 2>&1 &

echo "⏳ Attente que l'émulateur soit complètement démarré (cela peut prendre un moment)..."
adb wait-for-device
while [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]; do
    sleep 1
done

# 3. Configuration de l'environnement de test
echo "⚙️ Initialisation de l'environnement de test..."

echo "⌨️ Configuration du clavier..."
adb shell settings put secure spell_checker_enabled 0
adb shell settings put secure show_ime_with_hard_keyboard 1 

echo "🧹 Nettoyage de l'application (clearState)..."
adb shell pm clear "$MAESTRO_APP_ID" > /dev/null 2>&1

echo "🌍 Configuration de la langue..."
API_LEVEL=$(adb shell getprop ro.build.version.sdk | tr -d '\r')

if [[ "$API_LEVEL" =~ ^[0-9]+$ ]] && [ "$API_LEVEL" -ge 33 ]; then
    echo "📱 Android 13+ (API $API_LEVEL) détecté. Changement de la langue de l'application en français..."
    adb shell cmd locale set-app-locales "$MAESTRO_APP_ID" --locales fr-FR
else
    echo "⚠️ Version Android de l'émulateur (API ${API_LEVEL:-inconnue}) inférieure à 13."
    echo "   Impossible de changer automatiquement la langue de l'application."
    echo "   Veuillez changer manuellement la langue du système en français dans les paramètres Android."
    read -p "   Appuyez sur Entrée lorsque c'est fait... "
fi

echo "✅ Emulateur configuré !"

# 3. Lancement des tests Maestro
echo "🚀 Lancement de la suite de tests Maestro..."

maestro test \
    -e MAESTRO_APP_ID="$MAESTRO_APP_ID" \
    -e MAESTRO_APP_NAME="$MAESTRO_APP_NAME" \
    -e MAESTRO_USERNAME="$MAESTRO_USERNAME" \
    -e MAESTRO_PASSWORD="$MAESTRO_PASSWORD" \
    -e MAESTRO_RECOVERY_KEY="$MAESTRO_RECOVERY_KEY" \
    -e MAESTRO_ROOM_NAME="$MAESTRO_ROOM_NAME" \
    -e MAESTRO_INVITEE1_MXID="$MAESTRO_INVITEE1_MXID" \
    -e MAESTRO_INVITEE2_MXID="$MAESTRO_INVITEE2_MXID" \
    .maestro/allTests.yaml