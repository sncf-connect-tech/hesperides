pipeline {
	agent any
	stages {
		stage('BUILD AND PUSH') {
			when {
				branch 'develop'
			}
			steps {
				// Maven clean deploy + TU
				sleep 1
			}
		}
		stage('DEPLOY DEV') {
			when {
				branch 'develop'
			}
			steps {
				// Appeler un job rundeck (pprundeck.start...)
				sleep 1
			}
		}
		stage('MOCKED TESTS') {
			when {
				branch 'develop'
			}
			steps {
				// Tests bouchonnés (BDD, profil Maven)
				sleep 1
			}
		}
		stage('DEPLOY INT') {
			when {
				branch 'develop'
			}
			steps {
				// Appeler un job rundeck (pprundeck.start...)
				sleep 1
			}
		}
		stage('INTEGRATION TESTS') {
			when {
				branch 'develop'
			}
			steps {
				// Tests débouchonnés (BDD, profil Maven)
				sleep 1
			}
		}
		stage('DEPLOY PERF') {
			when {
				branch 'develop'
			}
			steps {
				// Créer une plateforme de tests de perf
				// Appeler un job rundeck (pprundeck.start...)
				sleep 1
			}
		}
		stage('PERF TESTS') {
			when {
				branch 'develop'
			}
			steps {
				// Tests de perf (tir gatling:execute)
				sleep 1
			}
		}
		stage('RELEASE') {
			when {
				branch 'develop'
			}
			steps {
				// milestone + maven release (crée la branche release et déploie sur le nexus)
				sleep 1
			}
		}
		// Il faut un jenkins de prod
		stage('DEPLOY BLUE') {
			when {
				branch 'release'
			}
			steps {
				// Appeler un job rundeck (rundeck.start...)
				sleep 1
			}
		}
		stage('VALIDATION') {
			when {
				branch 'release'
			}
			steps {
				input 'Prod valide ?'
			}
		}
		stage('SWITCH AND MERGE') {
			when {
				branch 'release'
			}
			steps {
				// switch HAP
				// Merge git de release vers prod et dev
				sleep 1
			}
		}
	}
}
