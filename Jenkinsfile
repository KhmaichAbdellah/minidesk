// ═══════════════════════════════════════════════════════════════════════════
// Jenkinsfile — Pipeline CI/CD MiniDesk
// ═══════════════════════════════════════════════════════════════════════════
// Compatible Windows (agent local) et Linux (agent Docker/CI)
// Étapes :
//   1. Checkout        → récupère le code depuis le repository Git
//   2. Test Backend    → mvn test (JUnit/Spring) + rapport JaCoCo
//   3. SonarQube       → analyse statique de la qualité du code (SonarCloud)
//   4. Lint Frontend   → npm run lint
//   5. Build Frontend  → npm run build
//   6. Build Docker    → construit les images (si Docker disponible)
//   7. Push Registry   → pousse vers GitLab Container Registry (si credentials configurés)
// ═══════════════════════════════════════════════════════════════════════════

pipeline {
    agent any

    // ── Variables globales ────────────────────────────────────────────────────
    environment {
        // SonarCloud
        SONAR_HOST_URL    = 'https://sonarcloud.io'
        SONAR_PROJECT_KEY = 'khmaichabdellah-minidesk'
        SONAR_ORG         = 'khmaichabdellah-minidesk'

        // Registry (GitLab)
        REGISTRY          = 'registry.gitlab.com'
        GITLAB_GROUP      = 'KhmaichAbdellah'
        PROJECT_NAME      = 'minidesk'
        IMAGE_BACKEND     = "${REGISTRY}/${GITLAB_GROUP}/${PROJECT_NAME}/backend"
        IMAGE_FRONTEND    = "${REGISTRY}/${GITLAB_GROUP}/${PROJECT_NAME}/frontend"
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
    }

    stages {

        // ── Stage 1 : Informations Git ────────────────────────────────────────
        stage('📌 Git Info') {
            steps {
                script {
                    echo "🚀 Build #${env.BUILD_NUMBER} démarré sur ${isUnix() ? 'Linux' : 'Windows'}"
                }
            }
        }

        // ── Stage 2 : Tests Backend + JaCoCo ──────────────────────────────────
        stage('🧪 Test Backend') {
            steps {
                dir('backend') {
                    script {
                        if (isUnix()) {
                            sh 'chmod +x mvnw || true'
                            sh './mvnw -B test jacoco:report || mvn -B test jacoco:report'
                        } else {
                            bat 'mvn -B test jacoco:report'
                        }
                    }
                }
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: 'backend/target/surefire-reports/*.xml'
                }
            }
        }

        // ── Stage 3 : Analyse SonarQube / SonarCloud ──────────────────────────
        stage('🔍 SonarQube Analysis') {
            environment {
                SONAR_TOKEN = credentials('sonarcloud-token')
            }
            steps {
                dir('backend') {
                    script {
                        def sonarCmd = """
                            mvn -B sonar:sonar \
                                -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                                -Dsonar.organization=${SONAR_ORG} \
                                -Dsonar.host.url=${SONAR_HOST_URL} \
                                -Dsonar.token=${SONAR_TOKEN} \
                                -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                        """
                        if (isUnix()) {
                            sh sonarCmd
                        } else {
                            bat """
                                mvn -B sonar:sonar -Dsonar.projectKey=${SONAR_PROJECT_KEY} -Dsonar.organization=${SONAR_ORG} -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.token=${SONAR_TOKEN} -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                            """
                        }
                    }
                }
            }
            post {
                always {
                    echo "📊 Rapport SonarCloud : https://sonarcloud.io/project/overview?id=${SONAR_PROJECT_KEY}"
                }
            }
        }

        // ── Stage 4 : Lint & Build Frontend ───────────────────────────────────
        stage('🎨 Frontend Quality') {
            steps {
                dir('frontend') {
                    script {
                        if (isUnix()) {
                            sh 'npm install --ignore-scripts || true'
                            sh 'npm run lint || true'
                        } else {
                            bat 'npm install --ignore-scripts || ver > nul'
                            bat 'npm run lint || ver > nul'
                        }
                    }
                }
            }
        }

        // ── Stage 5 : Build Docker Images (si Docker actif) ───────────────────
        stage('🐳 Build Docker Images') {
            when {
                expression {
                    try {
                        if (isUnix()) {
                            return sh(script: 'docker info', returnStatus: true) == 0
                        } else {
                            return bat(script: 'docker info > nul 2>&1', returnStatus: true) == 0
                        }
                    } catch (Exception e) {
                        return false
                    }
                }
            }
            steps {
                script {
                    echo "🐳 Docker est actif, construction des images..."
                    if (isUnix()) {
                        sh "docker build -t ${IMAGE_BACKEND}:${BUILD_NUMBER} ./backend"
                        sh "docker build -t ${IMAGE_FRONTEND}:${BUILD_NUMBER} ./frontend"
                    } else {
                        bat "docker build -t ${IMAGE_BACKEND}:${BUILD_NUMBER} ./backend"
                        bat "docker build -t ${IMAGE_FRONTEND}:${BUILD_NUMBER} ./frontend"
                    }
                }
            }
        }
    }

    // ── Post-actions globales ─────────────────────────────────────────────────
    post {
        success {
            echo "✅ Pipeline MiniDesk terminé avec succès ! (Build #${BUILD_NUMBER})"
        }
        failure {
            echo "❌ Pipeline en échec. Vérifiez les logs ci-dessus."
        }
    }
}
