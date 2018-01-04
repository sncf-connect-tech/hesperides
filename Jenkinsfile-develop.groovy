pipeline {
	agent any
	tools {
		maven 'maven3'
	}
	stages {
		stage('Build and push') {
			steps {
				sh 'mvn clean deploy'
			}
		}
		stage('Deploy dev') {
			steps {
				script {
					def Map options = [
							serveur: "deadbabyboy",
							refresh_puppet_agent: "complet_noop"
					]
					devrundeck.startAndWaitWithAutofill("HES", "refresh_puppet_agent", options)
				}
			}
		}
		stage('Mocked tests') {
			steps {
				// Tests bouchonnés (BDD, profil Maven)
				echo 'Mocked tests'
			}
		}
		stage('Deploy int') {
			steps {
				// Appeler un job rundeck (pprundeck.start...)
				script {
					def Map options = [
							serveur: "scat",
							refresh_puppet_agent: "complet_noop"
					]
					devrundeck.startAndWaitWithAutofill("HES", "refresh_puppet_agent", options)
				}
			}
		}
		stage('Integration tests') {
			steps {
				// Tests débouchonnés (BDD, profil Maven)
				echo 'Integration tests'
			}
		}
		stage('Deploy perf') {
			steps {
				// Créer une plateforme de tests de perf
				// Appeler un job rundeck (pprundeck.start...)
				echo 'Deploy perf'
			}
		}
		stage('Perf tests') {
			steps {
				// Tests de perf (tir gatling:execute)
				echo 'Perf tests'
			}
		}
		stage('Release') {
			steps {
				//input 'Release?'
				//milestone 1
				// milestone + maven release (crée la branche release et déploie sur le nexus)
				echo 'Release'
			}
		}
	}
}
