# ─────────────────────────────────────────────────────────────────────────────
# Stage 1 : Build – compile le frontend React avec Vite
# ─────────────────────────────────────────────────────────────────────────────
FROM node:22-alpine AS builder

WORKDIR /app

# Copier package.json en premier pour le cache Docker
COPY package.json package-lock.json* ./
RUN npm ci || npm install

# Copier le code source
COPY . .

# Build production (les variables d'environnement sont injectées à l'exécution via Nginx)
RUN npm run build

# ─────────────────────────────────────────────────────────────────────────────
# Stage 2 : Runtime – Nginx pour servir les fichiers statiques
# ─────────────────────────────────────────────────────────────────────────────
FROM nginx:1.27-alpine

LABEL maintainer="MiniDesk Team" \
      app="minidesk-frontend" \
      version="1.0"

# Supprimer la conf Nginx par défaut
RUN rm /etc/nginx/conf.d/default.conf

# Copier notre configuration Nginx personnalisée
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copier les fichiers buildés depuis le stage builder
COPY --from=builder /app/dist /usr/share/nginx/html

# Script d'entrée pour injecter VITE_API_URL au runtime
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

EXPOSE 80

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]
