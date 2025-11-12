// Espera a que la página cargue
document.addEventListener("DOMContentLoaded", () => {
    
    // Selecciona los elementos de la página
    const compileButton = document.getElementById('compileButton');
    const sourceCodeEl = document.getElementById('sourceCode');
    const asmOutputEl = document.getElementById('asmOutput');
    const dotOutputEl = document.getElementById('dotOutput');
    const errorOutputEl = document.getElementById('errorOutput');

    // Escucha el clic en el botón
    compileButton.addEventListener('click', async () => {
        
        // Limpiar salidas anteriores
        asmOutputEl.value = '';
        dotOutputEl.value = '';
        errorOutputEl.innerText = '';

        // 1. Obtener el código fuente del textarea
        const code = sourceCodeEl.value;

        try {
            // 2. Enviar el código al servidor Java (Back-End)
            // (Asegúrate de que tu Main.java esté corriendo en el puerto 4567)
            const response = await fetch('http://localhost:4567/compile', {
                method: 'POST',
                headers: { 'Content-Type': 'text/plain' },
                body: code
            });

            const result = await response.json(); // Esperar el JSON

            // 3. Mostrar los resultados
            if (result.error) {
                // Mostrar error de compilación
                errorOutputEl.innerText = result.error;
            } else {
                // Mostrar éxito
                asmOutputEl.value = result.asmCode;
                dotOutputEl.value = result.dotCode;
                
                // (Opcional: Aquí podrías usar Viz.js para renderizar el result.dotCode)
            }
        } catch (e) {
            // Error si el servidor Java no está corriendo
            errorOutputEl.innerText = 'Error de conexión: ¿Está el servidor Java (Main.java) en ejecución? \nDetalle: ' + e.message;
        }
    });
});