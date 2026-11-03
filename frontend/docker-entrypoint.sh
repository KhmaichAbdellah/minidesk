#!/bin/sh
# docker-entrypoint.sh
# Injecte la variable VITE_API_URL dans le fichier JS buildé au runtime.
# Si VITE_API_URL est vide ou "relative", Nginx sert de reverse-proxy transparent (/api/ -> backend).

set -e

PLACEHOLDER="http://localhost:8081"
RUNTIME_API_URL="${VITE_API_URL:-}"

if [ -z "$RUNTIME_API_URL" ] || [ "$RUNTIME_API_URL" = "relative" ]; then
    echo "🔧 MiniDesk Frontend - Mode reverse-proxy relatif activé"
    find /usr/share/nginx/html/assets -name "*.js" -exec \
        sed -i "s|${PLACEHOLDER}||g" {} \;
else
    # Si une URL absolue est fournie sans protocole, ajouter https://
    case "$RUNTIME_API_URL" in
        http://*|https://*) ;;
        *) RUNTIME_API_URL="https://${RUNTIME_API_URL}" ;;
    esac
    RUNTIME_API_URL=$(echo "$RUNTIME_API_URL" | sed 's|/$||')
    echo "🔧 MiniDesk Frontend - Configuration de l'API URL: ${RUNTIME_API_URL}"
    find /usr/share/nginx/html/assets -name "*.js" -exec \
        sed -i "s|${PLACEHOLDER}|${RUNTIME_API_URL}|g" {} \;
fi

echo "✅ Configuration terminée. Démarrage de Nginx..."

# Exécuter la commande passée en argument (nginx)
exec "$@"
