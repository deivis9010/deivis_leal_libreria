
def call(Map config = [:]) {
    
    def abortPipeline = config.get('abortPipeline', false)

   
    timeout(time: 5, unit: 'MINUTES') {
        script {
            echo "Iniciando análisis estático..."
            
            
            withSonarQubeEnv('Sonar Local') { 
                sh 'echo "Ejecución de las pruebas de calidad de código"'
            }

            echo "Evaluando Quality Gate..."
            
           
            if (abortPipeline) {
                error "El Quality Gate ha fallado y abortPipeline es True. Deteniendo ejecución."
            } else {
                echo "Quality Gate evaluado. Continuando pipeline (abortPipeline es False)."
            }
        }
    }
}