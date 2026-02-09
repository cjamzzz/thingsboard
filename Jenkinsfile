pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'maven3'
    }

    stages {
        stage('Checkout du répertoire') {
            steps {
                checkout scm
            }
        }

        stage('Compilation et exécution des tests unitaires') {
            steps {
                sh 'mvn clean install -T 1C -DskipTests -pl msa/tb -am -Dlicense.skip=true'
                sh 'mvn test --projects common/actor,common/transport/mqtt'
                sh 'mvn test --projects application -Dtest=Log8371PingControllerTest'
            }
            // Lecture des resultats des tests qu'on vient de run.
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Construire l\'image Docker Thingsboard') {
            steps {
                sh 'docker build -t thingsboard/tb:local -f ./msa/tb/target/docker-postgres/Dockerfile ./msa/tb/target/docker-postgres'
            }
        }

        stage('Déployer Thingsboard') {
            // Va déployer localement sur Windows
            steps {
                sh 'docker stop thingsboard-local || true'
                sh 'docker rm thingsboard-local || true'
                sh 'docker run --name thingsboard-local -p 9090:9090 -d thingsboard/tb:local'
            }
        }
    }

    post {
        success {
            echo 'Pipeline complétée, Thingsboard déployé et disponible'
        }
        failure {
            echo 'Pipeline échouée, faut tout revoir :('
        }
    }
}
