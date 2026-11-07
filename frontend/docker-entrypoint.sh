#!/bin/sh
# docker-entrypoint.sh
# Injecte dynamiquement la variable VITE_API_URL dans les bundles JS au runtime.

set -e

PLACEHOLDER="http://localhost:8081"
RUNTIME_API_URL="${VITE_API_URL:-}"

if [ -z "$RUNTIME_API_URL" ] || [ "$RUNTIME_API_URL" = "relative" ]; then
    echo "🔧 MiniDesk Frontend - Mode URL relative activé"
    find /usr/share/nginx/html/assets -name "*.js" -exec \
        sed -i "s|${PLACEHOLDER}||g" {} \;
else
    # Si le nom fourni par Render Blueprint (property: host) ne contient pas de point (ex: minidesk-backend-9a2q),
    # ajouter automatiquement le suffixe public .onrender.com pour que le navigateur puisse s'y connecter
    case "$RUNTIME_API_URL" in
        *.*) ;;
        *) RUNTIME_API_URL="${RUNTIME_API_URL}.onrender.com" ;;
    esac

    # Ajouter le protocole https:// si absent
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

# Exécuter la commande (nginx -g 'daemon off;')
exec "$@"
