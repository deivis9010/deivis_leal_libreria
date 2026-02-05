

def call(Map config = [:]) {
   
    def abortManual = config.get('abortPipeline', false)
       
    // BRANCH_NAME o de GIT_BRANCH
    def branchName = env.BRANCH_NAME ?: env.GIT_BRANCH ?: ""
    
    // Limpiamos el nombre (a veces GIT_BRANCH trae "origin/main")
    branchName = branchName.replace('origin/', '')

    echo "Rama detectada: ${branchName}"

    timeout(time: 5, unit: 'MINUTES') {
        script {
            withSonarQubeEnv('Sonar Local') { 
                sh 'echo "Ejecución de las pruebas de calidad de código"'
            }

            // LÓGICA DE DECISIÓN (HEURÍSTICA)
            // Regla 1: Si el argumento manual es True, aborta siempre.
            if (abortManual) {
                error "Abortando: Parámetro 'abortPipeline' es True."
            } 
            // Regla 2: Si es la rama master, aborta.
            else if (branchName == "master" || branchName == "main") {
                error "Abortando: Fallo en Quality Gate detectado en rama protegida (${branchName})."
            }
            // Regla 3: Si la rama empieza por 'hotfix', aborta.
            else if (branchName.startsWith("hotfix")) {
                error "Abortando: Fallo en Quality Gate detectado en rama de emergencia (${branchName})."
            }
            // Regla 4: Cualquier otra cosa, continúa.
            else {
                echo "Quality Gate no superado, pero se permite continuar en rama: ${branchName}"
            }
        }
    }
}