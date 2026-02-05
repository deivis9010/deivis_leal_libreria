def call(Map config = [:]) {
    // 1. Parámetros y detección de rama
    def abortManual = config.get('abortPipeline', false)
    def branchName = env.GIT_BRANCH ?: "unknown"
    
    // Limpieza de rama (origin/main -> main)
    if (branchName.contains('/')) {
        branchName = branchName.substring(branchName.lastIndexOf('/') + 1)
    }
    
    echo "Rama detectada: ${branchName}"

    
    def scannerHome = tool 'SonarScanner'

   
    timeout(time: 5, unit: 'MINUTES') {
        script {
            // Ejecución del análisis
            withSonarQubeEnv('Sonar Local') { 
                sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=practica_devops"
            }

            
            echo "Esperando resultados de SonarQube..."
            def qg = waitForQualityGate() 

            // 4. Lógica de decisión (Heurística)
            if (abortManual || qg.status != 'OK') {
                if (qg.status != 'OK') { 
                    echo "Quality Gate fallido: ${qg.status}" 
                }
                
                // Verificación de ramas críticas o aborto manual
                if (abortManual || branchName == "master" || branchName == "main" || branchName.startsWith("hotfix")) {
                    error "Pipeline abortado por calidad insuficiente en rama crítica (${branchName}) o decisión manual."
                } else {
                    echo "Quality Gate no es OK, pero se permite continuar en rama: ${branchName}"
                }
            } else {
                echo "Análisis de SonarQube Exitoso (OK)."
            }
        } 
    } 
}