pipeline {
	agent any
	tools {
		maven 'maven3'
	}
	stages {
		stage('BUILD AND PUSH') {
			steps {
				sh 'mvn clean deploy'
			}
		}
		stage('DEPLOY DEV') {
			steps {
				// Appeler un job rundeck (pprundeck.start...)
				sleep 0
			}
		}
		stage('MOCKED TESTS') {
			steps {
				// Tests bouchonnés (BDD, profil Maven)
				sleep 0
			}
		}
		stage('DEPLOY INT') {
			steps {
				// Appeler un job rundeck (pprundeck.start...)
				sleep 0
			}
		}
		stage('INTEGRATION TESTS') {
			steps {
				// Tests débouchonnés (BDD, profil Maven)
				sleep 0
			}
		}
		stage('DEPLOY PERF') {
			steps {
				// Créer une plateforme de tests de perf
				// Appeler un job rundeck (pprundeck.start...)
				sleep 0
			}
		}
		stage('PERF TESTS') {
			steps {
				// Tests de perf (tir gatling:execute)
				sleep 0
			}
		}
		stage('RELEASE') {
			steps {
				//input 'Release?'
				//milestone 1
				// milestone + maven release (crée la branche release et déploie sur le nexus)
				sleep 0
			}
		}
	}
}
