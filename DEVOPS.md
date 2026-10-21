# 🚀 MiniDesk — Guide DevOps : Docker · GitLab · Kubernetes · Jenkins

## 📋 Table des matières

1. [Architecture du projet](#architecture)
2. [Pré-requis](#pre-requis)
3. [Lancer en local avec Docker Compose](#docker-local)
4. [CI/CD avec GitLab et Jenkins](#cicd)
5. [Déploiement Kubernetes](#kubernetes)
6. [Gestion des secrets](#secrets)
7. [Checklist avant mise en production](#checklist)

---

## 🏗️ Architecture du projet <a name="architecture"></a>

```
MiniDesk/
├── backend/                    # Spring Boot (Java 21)
│   ├── src/
│   ├── Dockerfile              ✅ Multi-stage (Maven → JRE Alpine)
│   └── pom.xml
├── frontend/                   # React 19 + Vite 8
│   ├── src/
│   ├── Dockerfile              ✅ Multi-stage (Node → Nginx Alpine)
│   ├── nginx.conf              ✅ SPA fallback + proxy API
│   └── docker-entrypoint.sh   ✅ Injection runtime de VITE_API_URL
├── k8s/
│   ├── prod/                   ✅ Manifestes Kubernetes production
│   │   ├── namespace.yaml
│   │   ├── configmap.yaml
│   │   ├── secrets.yaml
│   │   ├── postgres.yaml
│   │   ├── backend.yaml        (+ HPA auto-scaling)
│   │   ├── frontend.yaml       (+ HPA auto-scaling)
│   │   └── ingress.yaml        (HTTPS via cert-manager)
│   └── staging/                ✅ Manifestes Kubernetes staging
│       ├── namespace.yaml
│       └── all.yaml
├── docker-compose.yml          ✅ Stack complète pour développement local
├── Jenkinsfile                 ✅ Pipeline Jenkins CI/CD
├── .gitlab-ci.yml              ✅ Pipeline GitLab CI/CD (alternative)
└── .env.example                ✅ Template des variables d'environnement
```

---

## ⚙️ Pré-requis <a name="pre-requis"></a>

| Outil | Version minimum | Usage |
|---|---|---|
| Docker | 24+ | Conteneurisation |
| Docker Compose | 2.20+ | Dev local |
| kubectl | 1.28+ | Déploiement Kubernetes |
| Java | 21 | Développement backend |
| Node.js | 22 | Développement frontend |
| Jenkins | 2.440+ | CI/CD (si utilisé) |
| GitLab | 16+ | SCM + Registry + CI/CD |

---

## 🐳 Lancer en local avec Docker Compose <a name="docker-local"></a>

### 1. Configurer les variables d'environnement

```bash
# Copier le template
cp .env.example .env

# Éditer avec vos valeurs réelles
notepad .env    # Windows
```

### 2. Démarrer la stack complète

```bash
# Premier lancement (build des images + démarrage)
docker compose up --build -d

# Vérifier que tous les services sont up
docker compose ps

# Voir les logs en temps réel
docker compose logs -f

# Voir uniquement les logs du backend
docker compose logs -f backend
```

### 3. URLs de développement

| Service | URL |
|---|---|
| Frontend React | http://localhost |
| Backend API | http://localhost:8081 |
| Swagger UI | http://localhost:8081/swagger-ui/index.html |
| Ollama LLM | http://localhost:11434 |
| PostgreSQL | localhost:5432 |

### 4. Commandes utiles

```bash
# Arrêter la stack
docker compose down

# Arrêter + supprimer les volumes (reset de la DB !)
docker compose down -v

# Rebuilder une seule image
docker compose build backend
docker compose up -d backend

# Accéder au shell PostgreSQL
docker compose exec postgres psql -U postgres -d minideskdb
```

### 5. Télécharger le modèle Ollama (LLM)

```bash
# Après démarrage de la stack
docker compose exec ollama ollama pull phi3:mini
```

---

## 🔄 CI/CD avec GitLab et Jenkins <a name="cicd"></a>

### Option A : GitLab CI/CD (`.gitlab-ci.yml`)

**Variables GitLab à configurer** (Settings → CI/CD → Variables) :

| Variable | Description | Protégée |
|---|---|---|
| `KUBECONFIG_STAGING` | Contenu du kubeconfig staging | ✅ |
| `KUBECONFIG_PROD` | Contenu du kubeconfig prod | ✅ |

> Le reste (registry, commit SHA, etc.) est injecté automatiquement par GitLab.

**Flux des branches :**
```
feature/* → develop  →  main
               ↓           ↓
           Staging     Production (manuel)
```

### Option B : Jenkins (Jenkinsfile)

**Credentials Jenkins à créer** (Manage Jenkins → Credentials) :

| ID | Type | Description |
|---|---|---|
| `gitlab-registry-credentials` | Username/Password | Login GitLab Registry |
| `kubeconfig-secret` | Secret file | Fichier kubeconfig |

**Configurer le pipeline Jenkins :**
1. Nouveau projet → Pipeline
2. Definition : "Pipeline script from SCM"
3. SCM : Git → URL de votre repo GitLab
4. Credentials : token GitLab (type Personal Access Token)
5. Branch : `*/main`, `*/develop`

---

## ☸️ Déploiement Kubernetes <a name="kubernetes"></a>

### 1. Créer le secret pour GitLab Registry

```bash
kubectl create secret docker-registry gitlab-registry-secret \
  --docker-server=registry.gitlab.com \
  --docker-username=YOUR_GITLAB_USER \
  --docker-password=YOUR_GITLAB_TOKEN \
  --namespace=minidesk-prod

# Copier aussi pour staging
kubectl create secret docker-registry gitlab-registry-secret \
  --docker-server=registry.gitlab.com \
  --docker-username=YOUR_GITLAB_USER \
  --docker-password=YOUR_GITLAB_TOKEN \
  --namespace=minidesk-staging
```

### 2. Créer les secrets applicatifs

```bash
kubectl create secret generic minidesk-secrets \
  --from-literal=db-password='YOUR_SECURE_DB_PASSWORD' \
  --from-literal=jwt-secret='$(openssl rand -hex 64)' \
  --from-literal=mail-username='your-email@gmail.com' \
  --from-literal=mail-password='your-16-char-app-password' \
  --from-literal=admin-password='YOUR_ADMIN_PASSWORD' \
  --from-literal=openai-api-key='sk-proj-YOUR_OPENAI_KEY' \
  --namespace=minidesk-prod
```

### 3. Déployer manuellement

```bash
# Production
kubectl apply -f k8s/prod/ --namespace=minidesk-prod

# Staging
kubectl apply -f k8s/staging/ --namespace=minidesk-staging

# Vérifier le déploiement
kubectl get pods -n minidesk-prod
kubectl get services -n minidesk-prod
kubectl get ingress -n minidesk-prod
```

### 4. Rollback en cas de problème

```bash
# Rollback du backend
kubectl rollout undo deployment/minidesk-backend -n minidesk-prod

# Voir l'historique des déploiements
kubectl rollout history deployment/minidesk-backend -n minidesk-prod
```

### 5. Auto-scaling (HPA)

Le backend et le frontend ont un HPA configuré :
- **Backend** : 2 → 6 répliques selon CPU/RAM
- **Frontend** : 2 → 4 répliques selon CPU

```bash
# Voir l'état du HPA
kubectl get hpa -n minidesk-prod
```

---

## 🔐 Gestion des secrets <a name="secrets"></a>

> ⚠️ **RÈGLE D'OR** : Ne jamais committer de secrets dans Git !

### Ce qui est sécurisé ✅

- `application.yaml` → utilise des placeholders `${VAR:default}`
- `.env` → dans `.gitignore` (jamais commité)
- Kubernetes Secrets → script automatisé `k8s/init-k8s.ps1` ou `k8s/init-k8s.sh`

### Script d'automatisation des secrets Kubernetes 🚀

Pour initialiser automatiquement le namespace, générer un JWT fort et créer les secrets :

```powershell
# Windows PowerShell
.\k8s\init-k8s.ps1 -Namespace "minidesk-prod" -GitLabToken "VOTRE_TOKEN_GITLAB" -ApplyManifests
```

```bash
# Linux / macOS / Git Bash
./k8s/init-k8s.sh "minidesk-prod" "KhmaichAbdellah" "VOTRE_TOKEN_GITLAB"
```

---

## ✅ État d'avancement & Checklist <a name="checklist"></a>

### Validé & Intégré ✅
- [x] Images Docker buildées et testées localement (Multi-stage Maven + Nginx)
- [x] `GITLAB_GROUP` (`KhmaichAbdellah`) configuré dans Jenkinsfile et les manifestes K8s
- [x] Pipeline GitLab CI (`.gitlab-ci.yml`) et Jenkins (`Jenkinsfile`) prêts
- [x] Module IA & LLM (Ollama / OpenAI) avec ChatWidget interactif
- [x] Module Commentaires sur les tickets (`Comment`)
- [x] Module Historique des statuts (`StatutLog`) avec timeline visuelle
- [x] Base de connaissances / FAQ (`KnowledgeArticle`) avec recherche
- [x] 32 tests unitaires Spring Boot passants à 100%
- [x] Scripts d'automatisation K8s (`init-k8s.ps1` & `init-k8s.sh`)

### Configuration finale avant mise en production ⚠️
- [ ] Remplacer les noms de domaines réels dans `ingress.yaml`
- [ ] Renseigner le token GitLab dans le secret du registre Kubernetes
- [ ] Configurer `KUBECONFIG_PROD` dans les variables GitLab CI (si déploiement direct par la CI)
