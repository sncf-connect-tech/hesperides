pipeline {
	agent any
	stages {
		// Il faut un jenkins de prod
		stage('DEPLOY BLUE') {
			steps {
				// Appeler un job rundeck (rundeck.start...)
				sleep 1
			}
		}
		stage('VALIDATION') {
			steps {
				input 'Prod valide ?'
			}
		}
		stage('SWITCH AND MERGE') {
			steps {
				// switch HAP
				// Merge git de release vers prod et dev
				sleep 1
			}
		}
	}
}
