# ═══════════════════════════════════════════════════════════════════════════════
# init-k8s.ps1 — Script PowerShell d'initialisation et déploiement Kubernetes MiniDesk
# ═══════════════════════════════════════════════════════════════════════════════

param(
    [string]$Namespace = "minidesk-prod",
    [string]$GitLabUser = "KhmaichAbdellah",
    [string]$GitLabToken = "",
    [string]$DbPassword = "MinideskSecurePassword2026!",
    [string]$AdminPassword = "Admin@2026!MiniDesk",
    [string]$MailUser = "admin@minidesk.com",
    [string]$MailPassword = "",
    [string]$OpenAiApiKey = "",
    [switch]$ApplyManifests
)

Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "🚀 Initialisation de l'environnement Kubernetes MiniDesk ($Namespace)" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan

# 1. Vérifier kubectl
try {
    $null = kubectl version --client 2>&1
    Write-Host "✅ kubectl est disponible" -ForegroundColor Green
} catch {
    Write-Error "❌ kubectl n'est pas installé ou n'est pas dans le PATH."
    exit 1
}

# 2. Créer le namespace s'il n'existe pas
Write-Host "📦 Création du namespace '$Namespace'..." -ForegroundColor Yellow
kubectl create namespace $Namespace --dry-run=client -o yaml | kubectl apply -f -

# 3. Générer un JWT Secret fort (64 hex / 256 bits)
$jwtBytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($jwtBytes)
$jwtSecret = -join ($jwtBytes | ForEach-Object { "{0:x2}" -f $_ })
Write-Host "🔑 JWT_SECRET fort généré automatiquement" -ForegroundColor Green

# 4. Créer le secret applicatif minidesk-secrets
Write-Host "🔐 Création du secret 'minidesk-secrets'..." -ForegroundColor Yellow
kubectl create secret generic minidesk-secrets `
  --from-literal=db-password="$DbPassword" `
  --from-literal=jwt-secret="$jwtSecret" `
  --from-literal=mail-username="$MailUser" `
  --from-literal=mail-password="$MailPassword" `
  --from-literal=admin-password="$AdminPassword" `
  --from-literal=openai-api-key="$OpenAiApiKey" `
  --namespace="$Namespace" `
  --dry-run=client -o yaml | kubectl apply -f -

# 5. Créer le secret GitLab Registry si un token est fourni
if ($GitLabToken) {
    Write-Host "🐳 Configuration de 'gitlab-registry-secret'..." -ForegroundColor Yellow
    kubectl create secret docker-registry gitlab-registry-secret `
      --docker-server="registry.gitlab.com" `
      --docker-username="$GitLabUser" `
      --docker-password="$GitLabToken" `
      --namespace="$Namespace" `
      --dry-run=client -o yaml | kubectl apply -f -
    Write-Host "✅ Secret de registre configuré" -ForegroundColor Green
} else {
    Write-Host "⚠️ Aucun GitLabToken fourni. N'oubliez pas de créer 'gitlab-registry-secret' pour pull les images privées." -ForegroundColor Yellow
}

# 6. Déploiement optionnel des manifestes
if ($ApplyManifests) {
    $manifestDir = if ($Namespace -match "staging") { "k8s/staging" } else { "k8s/prod" }
    Write-Host "🚀 Déploiement des manifestes depuis $manifestDir..." -ForegroundColor Cyan
    kubectl apply -f $manifestDir/ --namespace="$Namespace"
}

Write-Host "🎉 Initialisation terminée avec succès pour le namespace '$Namespace' !" -ForegroundColor Green
