// --- app.js CORREGIDO Y FINAL ---

// Definimos las variables globales necesarias fuera del listener
let sourceCodeEditor;
let compileButton;

// Esperamos a que todo el DOM esté cargado antes de inicializar el editor y adjuntar eventos
document.addEventListener("DOMContentLoaded", () => {
    
    // 1. Inicializa el editor CodeMirror AHORA que el contenedor existe
    sourceCodeEditor = CodeMirror(document.getElementById('codeEditorContainer'), {
        value: "// Pega tu código de MiniJava aquí...",
        mode: "clike", // Resaltado de sintaxis para C/Java
        lineNumbers: true,
        theme: "default"
    });

    // 2. Referencias a los elementos de salida (Ahora que sabemos que existen)
    compileButton = document.getElementById('compileButton');
    const asmOutputEl = document.getElementById('asmOutput');
    // NOTA: Eliminamos la búsqueda del elemento 'dotOutput' ya que era nulo en el HTML.
    const errorOutputEl = document.getElementById('errorOutput');
    const diagramContainer = document.getElementById('diagramContainer'); 
    
    // --- FUNCIÓN DE RENDERIZADO DE DIAGRAMA ---
    function renderDiagram(dotString) {
        diagramContainer.innerHTML = ''; 
        try {
            if (typeof Viz === 'undefined') {
                 diagramContainer.innerText = "Error: La librería Viz.js no se cargó correctamente.";
                 return;
            }
            const svgString = Viz(dotString, { format: "svg" }); 
            diagramContainer.innerHTML = svgString;
        } catch (e) {
            // Muestra el error de renderizado en la consola y en el contenedor
            diagramContainer.innerHTML = `<pre style="color: red;">Error al renderizar. Revisa el código DOT:\n${dotString}</pre>`;
            console.error("Error de Viz.js:", e);
        }
    }

    // --- FUNCIÓN PRINCIPAL DE COMPILACIÓN (Attach Listener) ---
    compileButton.addEventListener('click', async () => {
        
        // Limpiar salidas
        asmOutputEl.value = '';
        errorOutputEl.innerText = '';
        diagramContainer.innerHTML = ''; // Limpiamos el contenedor del diagrama

        // Obtener el código de CodeMirror
        const code = sourceCodeEditor.getValue(); 

        try {
            // 1. Enviar el código al servidor Java (Back-End)
            const response = await fetch('http://127.0.0.1:4567/compile', {
                method: 'POST',
                headers: { 'Content-Type': 'text/plain' },
                body: code,
                credentials: 'include' // Asegura que las cookies se envíen si es necesario
            });

            const result = await response.json(); 

            // 2. Procesar y mostrar los resultados
            if (result.error) {
                // Mostrar error de compilación en el registro
                errorOutputEl.innerText = `❌ ERROR DE COMPILACIÓN: \n${result.error}`;
            } else {
                // Éxito: Mostrar ensamblador y código DOT
                asmOutputEl.value = result.asmCode;
                
                // Renderizar el diagrama
                renderDiagram(result.dotCode);

                // AÑADIDO: Mostrar mensaje de éxito en el registro de estatus
                errorOutputEl.innerText = '✅ Compilación y generación exitosa. Servidor OK.';
            }
        } catch (e) {
            errorOutputEl.innerText = '❌ Error de conexión con el servidor: ¿Está el servidor Java (Main.java) en ejecución? \nDetalle: ' + e.message;
        }
    });

}); // <--- CIERRE DEL DOMContentLoaded