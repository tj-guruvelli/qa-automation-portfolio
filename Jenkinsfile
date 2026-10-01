// Declarative pipeline mirroring .github/workflows/ci.yml.
// Agent requirements: Linux node with Java 21 (tool "jdk21"), Node 20 (tool "node20"),
// a Chrome/Chromium binary on PATH, and network access to the app under test.
pipeline {
    agent any

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))
        disableConcurrentBuilds()
    }

    environment {
        BASE_URL = 'https://the-internet.herokuapp.com'
        CI = 'true'
    }

    stages {
        stage('Test suites') {
            parallel {
                stage('Selenium Java') {
                    tools { jdk 'jdk21' }
                    environment {
                        BROWSER = 'chrome'
                        HEADLESS = 'true'
                    }
                    steps {
                        dir('selenium-java') {
                            sh 'chmod +x mvnw && ./mvnw -B -ntp test'
                        }
                    }
                    post {
                        always {
                            junit allowEmptyResults: false,
                                  testResults: 'selenium-java/target/surefire-reports/*.xml'
                            archiveArtifacts artifacts: 'selenium-java/target/surefire-reports/**, selenium-java/target/screenshots/**',
                                             allowEmptyArchive: true,
                                             fingerprint: false
                        }
                    }
                }

                stage('Playwright TypeScript') {
                    tools { nodejs 'node20' }
                    steps {
                        dir('playwright-ts') {
                            sh 'npm ci'
                            sh 'npx playwright install --with-deps chromium'
                            sh 'npm run typecheck'
                            sh 'npx playwright test'
                        }
                    }
                    post {
                        always {
                            junit allowEmptyResults: false,
                                  testResults: 'playwright-ts/test-results/junit.xml'
                            archiveArtifacts artifacts: 'playwright-ts/playwright-report/**, playwright-ts/test-results/**',
                                             allowEmptyArchive: true,
                                             fingerprint: false
                        }
                    }
                }
            }
        }
    }

    post {
        failure {
            echo 'One or more suites failed. Open the JUnit trend and archived reports/traces for details.'
        }
    }
}
