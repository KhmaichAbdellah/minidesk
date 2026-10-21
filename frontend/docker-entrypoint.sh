#!/bin/sh
# docker-entrypoint.sh
# Injecte la variable VITE_API_URL dans le fichier JS buildé au runtime.
# Ceci permet de configurer l'URL de l'API sans rebuilder l'image Docker.

set -e

# Remplacer le placeholder dans les fichiers JS buildés par Vite
PLACEHOLDER="http://localhost:8081"
RUNTIME_API_URL="${VITE_API_URL:-http://localhost:8081}"

echo "🔧 MiniDesk Frontend - Configuration de l'API URL: ${RUNTIME_API_URL}"

# Remplacer dans tous les fichiers JS du build
find /usr/share/nginx/html/assets -name "*.js" -exec \
    sed -i "s|${PLACEHOLDER}|${RUNTIME_API_URL}|g" {} \;

echo "✅ Configuration terminée. Démarrage de Nginx..."

# Exécuter la commande passée en argument (nginx)
exec "$@"
