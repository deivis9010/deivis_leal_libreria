

def call(Map config = [:]) {
   
    def abortManual = config.get('abortPipeline', false)
       
    // Captura de variables de entorno nativas según la doc oficial
    // GIT_BRANCH es la variable estándar para pipelines de una sola rama
    def branchName = env.GIT_BRANCH ?: env.BRANCH_NAME ?: "unknown"
     echo "Rama detectada: ${branchName}"
    
    // Limpieza necesaria: GIT_BRANCH suele devolver 'origin/main' o 'origin/master'
    if (branchName.contains('/')) {
        branchName = branchName.substring(branchName.lastIndexOf('/') + 1)
    }
    
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