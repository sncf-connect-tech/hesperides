pipeline {
	agent any
	stages {
		stage('BUILD AND PUSH') {
			steps {
				// Maven clean deploy + TU
				sleep 1
			}
		}
		stage('DEPLOY DEV') {
			steps {
				// Appeler un job rundeck (pprundeck.start...)
				sleep 1
			}
		}
		stage('MOCKED TESTS') {
			steps {
				// Tests bouchonnés (BDD, profil Maven)
				sleep 1
			}
		}
		stage('DEPLOY INT') {
			steps {
				// Appeler un job rundeck (pprundeck.start...)
				sleep 1
			}
		}
		stage('INTEGRATION TESTS') {
			steps {
				// Tests débouchonnés (BDD, profil Maven)
				sleep 1
			}
		}
		stage('DEPLOY PERF') {
			steps {
				// Créer une plateforme de tests de perf
				// Appeler un job rundeck (pprundeck.start...)
				sleep 1
			}
		}
		stage('PERF TESTS') {
			steps {
				// Tests de perf (tir gatling:execute)
				sleep 1
			}
		}
		stage('RELEASE') {
			steps {
				input 'Release?'
				milestone 1
				// milestone + maven release (crée la branche release et déploie sur le nexus)
				sleep 1
			}
		}
	}
}
