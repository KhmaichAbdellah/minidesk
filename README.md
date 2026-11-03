<div align="center">

<img src="frontend/src/assets/MiniDesk.png" alt="MiniDesk Logo" width="180"/>

# MiniDesk

**Plateforme de support IT moderne — Ticketing, Base de connaissances & IA intégrée**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)](https://react.dev)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://adoptium.net)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](https://docs.docker.com/compose/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

</div>

---

## 📋 Sommaire

- [Aperçu](#-aperçu)
- [Fonctionnalités](#-fonctionnalités)
- [Architecture](#-architecture)
- [Stack technique](#-stack-technique)
- [Démarrage rapide](#-démarrage-rapide)
- [Configuration](#%EF%B8%8F-configuration)
- [API REST](#-api-rest)
- [Déploiement en production](#-déploiement-en-production)
- [CI/CD](#-cicd)
- [Structure du projet](#-structure-du-projet)
- [Contribuer](#-contribuer)

---

## 🎯 Aperçu

**MiniDesk** est une application complète de gestion de tickets de support IT. Elle permet aux utilisateurs de créer des tickets, aux techniciens de les traiter, et aux administrateurs de piloter l'activité via un tableau de bord. Un assistant IA (OpenAI / Ollama) est intégré pour aider à la résolution des incidents.

> Conçu pour les équipes IT qui veulent un outil **simple, rapide et auto-hébergeable**.

---

## ✨ Fonctionnalités

### 🎫 Gestion des tickets
- Création, suivi et résolution de tickets de support
- Priorités (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`) et catégories (`HARDWARE`, `SOFTWARE`, `NETWORK`, `OTHER`)
- Historique complet des changements de statut (timeline)
- Système de commentaires en temps réel

### 👥 Gestion des utilisateurs & rôles
| Rôle | Accès |
|------|-------|
| `USER` | Crée ses propres tickets, les consulte, les commente |
| `TECHNICIAN` | Gère tous les tickets, accès dashboard partiel |
| `ADMIN` | Accès complet, gestion des utilisateurs, statistiques |

### 📊 Dashboard & statistiques
- Compteurs de tickets par statut
- Vue en temps réel pour ADMIN et TECHNICIAN
- Graphiques et indicateurs clés

### 📚 Base de connaissances
- Articles consultables par tous les utilisateurs
- Création et édition réservées aux ADMIN/TECHNICIAN
- Liaison possible avec des tickets

### 🤖 Assistant IA intégré
- Support multi-provider : **OpenAI** (GPT-4o-mini) ou **Ollama** (local, phi3:mini)
- Historique de session conversationnelle
- Mode mock pour les tests sans clé API

### 🔐 Sécurité
- Authentification JWT (stateless)
- Mots de passe bcrypt
- Spring Security avec contrôle d'accès par rôle (`@PreAuthorize`)

### 📧 Notifications email
- Confirmation de création de ticket (Spring Mail + SMTP)
- Notification lors des changements de statut

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         CLIENT                              │
│           React 19 + MUI 6 + React Router 7                 │
│              Vite 8 · Axios · Lazy loading                  │
└───────────────────────┬─────────────────────────────────────┘
                        │ HTTP/REST (JWT)
┌───────────────────────▼─────────────────────────────────────┐
│                      BACKEND                                │
│          Spring Boot 3.5 (Java 21) · Port 8081              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │ Controllers  │  │   Services   │  │   Security (JWT) │   │
│  │  REST + OAS3 │  │  + LLM layer │  │   Spring Sec.    │   │
│  └──────┬───────┘  └──────┬───────┘  └──────────────────┘   │
│         └─────────────────▼                                  │
│              Spring Data JPA / Hibernate                     │
└───────────────────────┬─────────────────────────────────────┘
                        │
      ┌─────────────────┼──────────────────┐
      ▼                 ▼                  ▼
 PostgreSQL 16      OpenAI API       Ollama (local)
   Port 5432      (GPT-4o-mini)      Port 11434
```

---

## 🛠️ Stack technique

| Couche | Technologie | Version |
|--------|-------------|---------|
| **Frontend** | React + Vite | 19 / 8 |
| **UI** | Material UI | 6 |
| **Routing** | React Router | 7 |
| **HTTP client** | Axios | 1.18 |
| **Backend** | Spring Boot | 3.5.4 |
| **Langage** | Java | 21 |
| **ORM** | Spring Data JPA / Hibernate | - |
| **Base de données** | PostgreSQL | 16 |
| **Sécurité** | Spring Security + JWT (jjwt 0.12.7) | - |
| **Documentation API** | SpringDoc OpenAPI 3 | 2.8.9 |
| **Email** | Spring Mail | - |
| **IA** | OpenAI API / Ollama | - |
| **Conteneurisation** | Docker + Docker Compose | - |
| **Orchestration** | Kubernetes (optionnel) | - |
| **CI/CD** | Jenkins + GitLab CI | - |

---

## 🚀 Démarrage rapide

### Prérequis

- [Docker](https://docs.docker.com/get-docker/) ≥ 24 et Docker Compose ≥ 2
- Git

### 1. Cloner le dépôt

```bash
git clone https://github.com/KhmaichAbdellah/minidesk.git
cd minidesk
```

### 2. Configurer les variables d'environnement

```bash
cp .env.example .env
```

Éditer `.env` avec vos vraies valeurs (voir [Configuration](#%EF%B8%8F-configuration)).

### 3. Lancer avec Docker Compose

```bash
docker compose up -d
```

Les services démarrent dans l'ordre : `postgres` → `backend` → `frontend`.

| Service | URL |
|---------|-----|
| 🖥️ Frontend | http://localhost |
| ⚙️ Backend API | http://localhost:8081 |
| 📖 Swagger UI | http://localhost:8081/swagger-ui.html |
| 🗄️ PostgreSQL | localhost:5432 |
| 🤖 Ollama (optionnel) | http://localhost:11434 |

### 4. Connexion par défaut

| Champ | Valeur |
|-------|--------|
| Email | `admin@minidesk.com` |
| Mot de passe | `Admin@2026!` |

> ⚠️ **Changez ces identifiants** via la variable `ADMIN_EMAIL` / `ADMIN_PASSWORD` dans `.env` avant toute mise en production.

### Développement local (sans Docker)

**Backend :**
```bash
cd backend
./mvnw spring-boot:run
```

**Frontend :**
```bash
cd frontend
npm install
npm run dev
```

---

## ⚙️ Configuration

Toutes les variables sont définies dans `.env` (copie de `.env.example`) :

```bash
# Base de données
DB_PASSWORD=votre_mot_de_passe_postgres

# JWT — générer avec : openssl rand -hex 64
JWT_SECRET=votre_secret_jwt_min_64_chars
JWT_EXPIRATION=86400000

# Email SMTP (Gmail App Password)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=votre@gmail.com
MAIL_PASSWORD=votre_app_password

# Compte admin initial
ADMIN_EMAIL=admin@minidesk.com
ADMIN_PASSWORD=VotreMotDePasseSécurisé!

# LLM — 'openai' ou 'ollama'
LLM_PROVIDER=openai
OPENAI_API_KEY=sk-proj-...
OPENAI_MODEL=gpt-4o-mini

# URL publique du backend (pour le frontend en prod)
VITE_API_URL=https://votre-domaine.com:8081

# Spring profile
SPRING_PROFILES_ACTIVE=prod
```

### Choisir le provider LLM

| Provider | `LLM_PROVIDER` | Avantages |
|----------|---------------|-----------|
| OpenAI | `openai` | Qualité supérieure, sans infrastructure |
| Ollama | `ollama` | 100% local, gratuit, vie privée |
| Mock | (mettre `LLM_MOCK=true`) | Tests sans API |

---

## 📖 API REST

La documentation interactive est disponible sur **http://localhost:8081/swagger-ui.html** (Swagger UI / OpenAPI 3).

### Endpoints principaux

| Méthode | Endpoint | Rôle requis | Description |
|---------|----------|-------------|-------------|
| `POST` | `/api/auth/login` | Public | Authentification |
| `GET` | `/api/tickets` | Authentifié | Lister les tickets |
| `POST` | `/api/tickets` | Authentifié | Créer un ticket |
| `PUT` | `/api/tickets/{id}` | TECHNICIAN / ADMIN | Modifier un ticket |
| `DELETE` | `/api/tickets/{id}` | ADMIN | Supprimer un ticket |
| `GET` | `/api/tickets/{id}/comments` | Authentifié | Commentaires d'un ticket |
| `POST` | `/api/tickets/{id}/comments` | Authentifié | Ajouter un commentaire |
| `GET` | `/api/tickets/{id}/history` | Authentifié | Historique des statuts |
| `GET` | `/api/tickets/stats` | ADMIN / TECHNICIAN | Stats dashboard |
| `GET` | `/api/users` | ADMIN | Lister les utilisateurs |
| `GET` | `/api/knowledge` | Authentifié | Base de connaissances |
| `POST` | `/api/ai/chat` | Authentifié | Assistant IA |

---

## 🌐 Déploiement en production

### Option 1 — Docker Compose sur VPS (recommandé)

1. **Préparer votre serveur** (Ubuntu 22.04+ recommandé) :
```bash
# Installer Docker
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
```

2. **Copier les fichiers sur le serveur** :
```bash
scp -r . user@votre-serveur:/opt/minidesk
```

3. **Configurer et lancer** :
```bash
cd /opt/minidesk
cp .env.example .env
nano .env          # Remplir toutes les valeurs
docker compose up -d
```

4. **Reverse proxy Nginx** (recommandé pour HTTPS) :
```nginx
server {
    listen 80;
    server_name votre-domaine.com;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl;
    server_name votre-domaine.com;

    ssl_certificate     /etc/letsencrypt/live/votre-domaine.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/votre-domaine.com/privkey.pem;

    # Frontend
    location / {
        proxy_pass http://localhost:80;
    }

    # Backend API
    location /api/ {
        proxy_pass http://localhost:8081/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

5. **Certificat SSL gratuit** avec Certbot :
```bash
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d votre-domaine.com
```

### Option 2 — Kubernetes

Les manifests Kubernetes sont disponibles dans `k8s/` :

```bash
# Staging
kubectl apply -f k8s/staging/

# Production
kubectl apply -f k8s/prod/
```

Voir [`DEVOPS.md`](DEVOPS.md) pour le guide complet Kubernetes.

### Mise à jour en production

```bash
cd /opt/minidesk
git pull origin main
docker compose build
docker compose up -d
```

---

## 🔄 CI/CD

Le projet inclut un pipeline **Jenkinsfile** complet :

| Étape | Description |
|-------|-------------|
| ✅ Checkout | Récupère le code depuis Git |
| 🧪 Test Backend | `mvn test` (JUnit + Spring) |
| 📦 Build Backend | `mvn package` → JAR |
| 🎨 Build Frontend | `npm ci` + `npm run build` |
| 🐳 Docker Build | Construit les images multi-stage |
| 🚀 Docker Push | Pousse vers GitLab Container Registry |
| 🌿 Deploy Staging | `kubectl apply` sur le namespace `staging` |
| 🎯 Deploy Prod | `kubectl apply` sur `prod` (approbation manuelle) |

### Images Docker

```
registry.gitlab.com/KhmaichAbdellah/minidesk/backend:latest
registry.gitlab.com/KhmaichAbdellah/minidesk/frontend:latest
```

---

## 📁 Structure du projet

```
minidesk/
├── backend/                          # Spring Boot API
│   ├── src/main/java/com/minidesk/
│   │   ├── config/                   # Configs (OpenAPI, DataInit, LLM)
│   │   ├── controller/               # Contrôleurs REST
│   │   ├── dto/                      # DTOs (request / response)
│   │   ├── entity/                   # Entités JPA
│   │   ├── enums/                    # Priority, Role, TicketStatus...
│   │   ├── exception/                # Gestion globale des erreurs
│   │   ├── repository/               # Spring Data JPA repositories
│   │   ├── security/                 # JWT (JwtService, JwtAuthFilter)
│   │   └── service/                  # Logique métier + clients LLM
│   └── Dockerfile                    # Multi-stage build (Maven → JRE)
│
├── frontend/                         # React + Vite
│   ├── src/
│   │   ├── components/               # Composants réutilisables
│   │   ├── context/                  # AuthContext (JWT)
│   │   ├── pages/                    # Dashboard, Tickets, Users...
│   │   ├── routes/                   # Définition des routes
│   │   └── service/                  # Services API (axios)
│   ├── nginx.conf                    # Config Nginx pour la prod
│   └── Dockerfile                    # Multi-stage build (Vite → Nginx)
│
├── k8s/                              # Manifests Kubernetes
│   ├── prod/                         # Namespace prod
│   └── staging/                      # Namespace staging
│
├── docker-compose.yml                # Stack complète locale
├── Jenkinsfile                       # Pipeline CI/CD Jenkins
├── .env.example                      # Template de configuration
└── DEVOPS.md                         # Guide DevOps complet
```

---

## 🤝 Contribuer

1. **Fork** le projet
2. Créer une branche : `git checkout -b feature/ma-fonctionnalite`
3. Committer vos changements : `git commit -m 'feat: ajouter ma fonctionnalité'`
4. Pousser la branche : `git push origin feature/ma-fonctionnalite`
5. Ouvrir une **Pull Request**

### Convention de commits

```
feat:     Nouvelle fonctionnalité
fix:      Correction de bug
docs:     Documentation
style:    Formatage (sans changement logique)
refactor: Refactoring
test:     Ajout/modification de tests
chore:    Tâches de maintenance
```

---

## 📄 Licence

Ce projet est sous licence **MIT** — voir le fichier [LICENSE](LICENSE) pour plus de détails.

---

<div align="center">

Développé avec ❤️ par [KhmaichAbdellah](https://github.com/KhmaichAbdellah)

</div>
