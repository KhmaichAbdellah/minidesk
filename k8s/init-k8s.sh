#!/usr/bin/env bash
# ═══════════════════════════════════════════════════════════════════════════════
# init-k8s.sh — Script Bash d'initialisation et déploiement Kubernetes MiniDesk
# ═══════════════════════════════════════════════════════════════════════════════

set -euo pipefail

NAMESPACE="${1:-minidesk-prod}"
GITLAB_USER="${2:-KhmaichAbdellah}"
GITLAB_TOKEN="${3:-}"
DB_PASSWORD="${4:-MinideskSecurePassword2026!}"
ADMIN_PASSWORD="${5:-Admin@2026!MiniDesk}"

echo "================================================================="
echo "🚀 Initialisation Kubernetes MiniDesk (Namespace: ${NAMESPACE})"
echo "================================================================="

# 1. Vérifier kubectl
if ! command -v kubectl &> /dev/null; then
    echo "❌ Erreur: kubectl n'est pas installé."
    exit 1
fi

# 2. Créer le namespace
echo "📦 Création du namespace '${NAMESPACE}'..."
kubectl create namespace "${NAMESPACE}" --dry-run=client -o yaml | kubectl apply -f -

# 3. Générer JWT Secret
JWT_SECRET=$(openssl rand -hex 64 2>/dev/null || date +%s | sha256sum | head -c 64)
echo "🔑 JWT_SECRET généré"

# 4. Créer le secret applicatif
echo "🔐 Création du secret 'minidesk-secrets'..."
kubectl create secret generic minidesk-secrets \
  --from-literal=db-password="${DB_PASSWORD}" \
  --from-literal=jwt-secret="${JWT_SECRET}" \
  --from-literal=mail-username="admin@minidesk.com" \
  --from-literal=mail-password="" \
  --from-literal=admin-password="${ADMIN_PASSWORD}" \
  --from-literal=openai-api-key="" \
  --namespace="${NAMESPACE}" \
  --dry-run=client -o yaml | kubectl apply -f -

# 5. Créer secret registry si token fourni
if [ -n "${GITLAB_TOKEN}" ]; then
  echo "🐳 Création du secret 'gitlab-registry-secret'..."
  kubectl create secret docker-registry gitlab-registry-secret \
    --docker-server="registry.gitlab.com" \
    --docker-username="${GITLAB_USER}" \
    --docker-password="${GITLAB_TOKEN}" \
    --namespace="${NAMESPACE}" \
    --dry-run=client -o yaml | kubectl apply -f -
fi

echo "✅ Initialisation Kubernetes terminée pour '${NAMESPACE}' !"
