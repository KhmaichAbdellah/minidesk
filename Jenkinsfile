// ═══════════════════════════════════════════════════════════════════════════
// Jenkinsfile — Pipeline CI/CD MiniDesk
// ═══════════════════════════════════════════════════════════════════════════
// Étapes :
//   1. Checkout        → récupère le code depuis GitLab
//   2. Test Backend    → mvn test (JUnit/Spring) + rapport JaCoCo
//   3. SonarQube       → analyse statique de la qualité du code
//   4. Lint Frontend   → npm run lint
//   5. Build Backend   → mvn package → JAR
//   6. Build Frontend  → npm ci + npm run build
//   7. Docker Build    → construit les images backend & frontend
//   8. Docker Push     → pousse vers GitLab Container Registry
//   9. Deploy Staging  → kubectl apply sur le namespace staging
//  10. Deploy Prod     → kubectl apply sur le namespace prod (approbation manuelle)
// ═══════════════════════════════════════════════════════════════════════════

pipeline {
    agent {
        // Utilise un agent Docker-in-Docker ou un nœud avec Docker installé
        label 'docker-agent'
    }

    // ── Variables globales ────────────────────────────────────────────────────
    environment {
        // GitLab Registry
        REGISTRY          = 'registry.gitlab.com'
        GITLAB_GROUP      = 'KhmaichAbdellah'
        PROJECT_NAME      = 'minidesk'
        IMAGE_BACKEND     = "${REGISTRY}/${GITLAB_GROUP}/${PROJECT_NAME}/backend"
        IMAGE_FRONTEND    = "${REGISTRY}/${GITLAB_GROUP}/${PROJECT_NAME}/frontend"

        // Credential IDs définis dans Jenkins Credentials Manager
        GITLAB_CREDS      = credentials('gitlab-registry-credentials')
        KUBECONFIG_CREDS  = credentials('kubeconfig-secret')
        SONAR_TOKEN       = credentials('sonarcloud-token')   // Token SonarCloud

        // SonarCloud
        SONAR_HOST_URL    = 'https://sonarcloud.io'
        SONAR_PROJECT_KEY = 'KhmaichAbdellah_minidesk'
        SONAR_ORG         = 'khmaichabdellah'

        // Tag de l'image = numéro de build Jenkins + court SHA Git
        IMAGE_TAG         = "${BUILD_NUMBER}-${GIT_COMMIT.take(8)}"
    }

    options {
        // Timeout global du pipeline
        timeout(time: 45, unit: 'MINUTES')
        // Garder seulement les 10 derniers builds
        buildDiscarder(logRotator(numToKeepStr: '10'))
        // Pas de checkout automatique (on le fait explicitement)
        skipDefaultCheckout(true)
        // Horodatage dans les logs
        timestamps()
        // Couleurs ANSI dans la console Jenkins
        ansiColor('xterm')
    }

    stages {

        // ── Stage 1 : Checkout ────────────────────────────────────────────────
        stage('🔄 Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_COMMIT_SHORT = sh(
                        returnStdout: true,
                        script: 'git rev-parse --short HEAD'
                    ).trim()
                    env.GIT_BRANCH_NAME = env.BRANCH_NAME ?: sh(
                        returnStdout: true,
                        script: 'git rev-parse --abbrev-ref HEAD'
                    ).trim()
                    echo "📌 Branch: ${env.GIT_BRANCH_NAME} | Commit: ${env.GIT_COMMIT_SHORT}"
                }
            }
        }

        // ── Stage 2 : Tests Backend ───────────────────────────────────────────
        stage('🧪 Test Backend') {
            agent {
                docker {
                    image 'maven:3.9.9-eclipse-temurin-21'
                    args '-v $HOME/.m2:/root/.m2'  // Cache Maven local
                    reuseNode true
                }
            }
            steps {
                dir('backend') {
                    sh 'mvn test -B jacoco:report'
                }
            }
            post {
                always {
                    // Publier les rapports JUnit dans Jenkins
                    junit allowEmptyResults: true,
                          testResults: 'backend/target/surefire-reports/*.xml'
                    // Publier le rapport de couverture JaCoCo
                    jacoco(
                        execPattern: 'backend/target/jacoco.exec',
                        classPattern: 'backend/target/classes',
                        sourcePattern: 'backend/src/main/java',
                        exclusionPattern: '**/config/**,**/entity/**,**/dto/**,**/exception/**'
                    )
                }
            }
        }

        // ── Stage 3 : Analyse SonarQube ───────────────────────────────────────
        stage('🔍 SonarQube Analysis') {
            agent {
                docker {
                    image 'maven:3.9.9-eclipse-temurin-21'
                    args '-v $HOME/.m2:/root/.m2 -v $HOME/.sonar:/root/.sonar'
                    reuseNode true
                }
            }
            steps {
                dir('backend') {
                    sh """
                        mvn -B sonar:sonar \\
                            -Dsonar.projectKey=${SONAR_PROJECT_KEY} \\
                            -Dsonar.organization=${SONAR_ORG} \\
                            -Dsonar.host.url=${SONAR_HOST_URL} \\
                            -Dsonar.token=${SONAR_TOKEN} \\
                            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                    """
                }
            }
            post {
                always {
                    echo "📊 Rapport SonarCloud : https://sonarcloud.io/project/overview?id=${SONAR_PROJECT_KEY}"
                }
            }
        }

        // ── Stage 4 : Lint Frontend ───────────────────────────────────────────
        stage('🔍 Lint Frontend') {
            agent {
                docker {
                    image 'node:22-alpine'
                    args '-v $HOME/.npm:/root/.npm'
                    reuseNode true
                }
            }
            steps {
                dir('frontend') {
                    sh 'npm ci'
                    sh 'npm run lint'
                }
            }
        }

        // ── Stage 4 : Build Docker Images ─────────────────────────────────────
        stage('🐳 Build Docker Images') {
            parallel {
                stage('Build Backend Image') {
                    steps {
                        dir('backend') {
                            sh """
                                docker build \
                                    --tag ${IMAGE_BACKEND}:${IMAGE_TAG} \
                                    --tag ${IMAGE_BACKEND}:latest \
                                    --cache-from ${IMAGE_BACKEND}:latest \
                                    --build-arg BUILDKIT_INLINE_CACHE=1 \
                                    .
                            """
                        }
                    }
                }
                stage('Build Frontend Image') {
                    steps {
                        dir('frontend') {
                            sh """
                                docker build \
                                    --tag ${IMAGE_FRONTEND}:${IMAGE_TAG} \
                                    --tag ${IMAGE_FRONTEND}:latest \
                                    --cache-from ${IMAGE_FRONTEND}:latest \
                                    --build-arg BUILDKIT_INLINE_CACHE=1 \
                                    .
                            """
                        }
                    }
                }
            }
        }

        // ── Stage 5 : Push vers GitLab Registry ──────────────────────────────
        stage('📤 Push to Registry') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    branch pattern: 'release/.*', comparator: 'REGEXP'
                }
            }
            steps {
                sh "echo ${GITLAB_CREDS_PSW} | docker login ${REGISTRY} -u ${GITLAB_CREDS_USR} --password-stdin"
                sh "docker push ${IMAGE_BACKEND}:${IMAGE_TAG}"
                sh "docker push ${IMAGE_BACKEND}:latest"
                sh "docker push ${IMAGE_FRONTEND}:${IMAGE_TAG}"
                sh "docker push ${IMAGE_FRONTEND}:latest"
            }
            post {
                always {
                    sh "docker logout ${REGISTRY}"
                }
            }
        }

        // ── Stage 6 : Deploy Staging ──────────────────────────────────────────
        stage('🚀 Deploy → Staging') {
            when { branch 'develop' }
            environment {
                KUBECONFIG = "${KUBECONFIG_CREDS}"
                K8S_NAMESPACE = 'minidesk-staging'
            }
            steps {
                sh """
                    sed -i 's|IMAGE_TAG|${IMAGE_TAG}|g' k8s/staging/*.yaml
                    kubectl apply -f k8s/staging/ --namespace=${K8S_NAMESPACE}
                    kubectl rollout status deployment/minidesk-backend --namespace=${K8S_NAMESPACE} --timeout=120s
                    kubectl rollout status deployment/minidesk-frontend --namespace=${K8S_NAMESPACE} --timeout=120s
                """
            }
        }

        // ── Stage 7 : Deploy Production (avec approbation manuelle) ───────────
        stage('🏭 Deploy → Production') {
            when { branch 'main' }
            environment {
                KUBECONFIG = "${KUBECONFIG_CREDS}"
                K8S_NAMESPACE = 'minidesk-prod'
            }
            steps {
                // Demander une approbation manuelle avant de déployer en prod
                timeout(time: 30, unit: 'MINUTES') {
                    input message: "🚨 Déployer la version ${IMAGE_TAG} en PRODUCTION ?",
                          ok: "✅ Oui, déployer",
                          submitter: "admin,devops-team"
                }
                sh """
                    sed -i 's|IMAGE_TAG|${IMAGE_TAG}|g' k8s/prod/*.yaml
                    kubectl apply -f k8s/prod/ --namespace=${K8S_NAMESPACE}
                    kubectl rollout status deployment/minidesk-backend --namespace=${K8S_NAMESPACE} --timeout=180s
                    kubectl rollout status deployment/minidesk-frontend --namespace=${K8S_NAMESPACE} --timeout=180s
                """
            }
        }
    }

    // ── Post-actions globales ─────────────────────────────────────────────────
    post {
        success {
            echo "✅ Pipeline terminé avec succès ! Image: ${IMAGE_TAG}"
            // Optionnel : notification Slack/Teams/Email
            // slackSend channel: '#minidesk-deployments', message: "✅ Déployé: ${IMAGE_TAG}"
        }
        failure {
            echo "❌ Pipeline en échec ! Consulter les logs Jenkins."
            // emailext body: "Pipeline failed: ${env.BUILD_URL}",
            //          subject: "❌ MiniDesk Build #${BUILD_NUMBER} Failed",
            //          to: "devops@minidesk.com"
        }
        cleanup {
            // Nettoyage des images Docker locales pour libérer l'espace
            sh """
                docker rmi ${IMAGE_BACKEND}:${IMAGE_TAG} || true
                docker rmi ${IMAGE_FRONTEND}:${IMAGE_TAG} || true
                docker system prune -f || true
            """
        }
    }
}
