// app.js - Maneja el compilador MiniJava

// Variable para evitar compilaciones múltiples
let isCompiling = false;
let sourceCodeEditor;

document.addEventListener("DOMContentLoaded", () => {
    
    // Referencias a elementos del DOM
    const codeEditorContainer = document.getElementById('codeEditorContainer');
    const compileButton = document.getElementById('compileButton');
    const asmOutputEl = document.getElementById('asmOutput');
    const errorOutputEl = document.getElementById('errorOutput');
    const diagramContainer = document.getElementById('diagramContainer');
    
    // Referencia al botón de Modo Oscuro
    const themeBtn = document.getElementById('themeToggle');
    
    // 1. Inicializar CodeMirror
    if (codeEditorContainer) {
        sourceCodeEditor = CodeMirror(codeEditorContainer, {
            value: "// Pega tu código de MiniJava aquí...\nint x;\nx = 5;",
            mode: "clike",
            lineNumbers: true,
            theme: "default" // Tema claro por defecto
        });
        console.log('✅ Editor CodeMirror inicializado');
    }

    // ==========================================
    //  2. LÓGICA DE MODO OSCURO 
    // ==========================================
    if (themeBtn) {
        themeBtn.addEventListener('click', () => {
            // Alternar clase en el body
            document.body.classList.toggle('dark-mode');
            const isDarkMode = document.body.classList.contains('dark-mode');
            
            // A. Cambiar texto del botón
            themeBtn.innerText = isDarkMode ? "☀️ Modo Claro" : "🌙 Modo Oscuro";
            
            // B. Cambiar tema del editor CodeMirror (Monokai vs Default)
            if (sourceCodeEditor) {
                sourceCodeEditor.setOption("theme", isDarkMode ? "monokai" : "default");
            }
        });
    }
    
    // --- FUNCIÓN DE RENDERIZADO DE DIAGRAMA ---
    function renderDiagram(dotString) {
        if (!diagramContainer) {
            console.warn('⚠️ No se encontró el contenedor del diagrama');
            return;
        }
        
        diagramContainer.innerHTML = ''; 
        
        try {
            if (typeof Viz === 'undefined') {
                diagramContainer.innerText = "Error: La librería Viz.js no se cargó correctamente.";
                console.error('❌ Viz.js no está disponible');
                return;
            }
            
            const svgString = Viz(dotString, { format: "svg" }); 
            diagramContainer.innerHTML = svgString;
            console.log('✅ Diagrama renderizado correctamente');
            
        } catch (e) {
            diagramContainer.innerHTML = `<pre style="color: red;">Error al renderizar DOT:\n${e.message}\n\nCódigo DOT:\n${dotString}</pre>`;
            console.error("❌ Error de Viz.js:", e);
        }
    }

    // --- FUNCIÓN PRINCIPAL DE COMPILACIÓN ---
    if (compileButton && sourceCodeEditor) {
        
        compileButton.addEventListener('click', async (e) => {
             e.preventDefault();
             e.stopPropagation();
            // Prevenir compilaciones múltiples
            if (isCompiling) {
                console.log('⏳ Ya hay una compilación en progreso...');
                return;
            }
            
            isCompiling = true;
            compileButton.disabled = true;
            compileButton.textContent = 'Compilando...';
            
            // Limpiar salidas
            if (asmOutputEl) asmOutputEl.value = '';
            if (errorOutputEl) {
                errorOutputEl.innerText = 'Compilando...';
                errorOutputEl.style.color = 'blue';
            }
            if (diagramContainer) diagramContainer.innerHTML = '';

            // Obtener el código de CodeMirror
            const code = sourceCodeEditor.getValue(); 

            try {
                console.log('📝 Enviando código al servidor...');
                console.log('Código a compilar:', code.substring(0, 100) + '...');
                
                // Enviar el código al servidor Java
                // Asegúrate de que el puerto coincida con tu Main.java (4567 para Spark)
                const response = await fetch('http://127.0.0.1:4567/compile', {
                    method: 'POST',
                    headers: { 'Content-Type': 'text/plain' }, // O 'application/json' si cambiaste el backend
                    body: code, // O JSON.stringify({code: code}) si cambiaste el backend
                });

                console.log('📡 Respuesta recibida - Status:', response.status);
                
                const result = await response.json();
                console.log('📦 Resultado completo:', result);

                // Procesar resultados
                // Nota: Ajusta 'result.success' o 'result.isSuccess' según tu DTO Java
                if (response.ok) { 
                    // Compilación exitosa
                    console.log('✅ Compilación exitosa');
                    
                    // Manejo de ASM (ajusta el nombre del campo si es 'asmResult' o 'asmCode')
                    const asmText = result.asmCode || result.asmResult;
                    if (asmText) {
                        asmOutputEl.value = asmText;
                        console.log('✅ Ensamblador mostrado');
                    } else {
                        console.warn('⚠️ No hay código ensamblador en la respuesta');
                    }
                    
                    // Manejo de DOT (ajusta el nombre del campo si es 'dotResult' o 'dotCode')
                    const dotText = result.dotCode || result.dotResult;
                    if (dotText) {
                        renderDiagram(dotText);
                    } else {
                        console.warn('⚠️ No hay diagrama DOT en la respuesta');
                    }

                    if (errorOutputEl) {
                        errorOutputEl.innerText = '✅ Compilación y generación exitosa.';
                        errorOutputEl.style.color = 'green';
                    }
                    
                } else {
                    // Error de compilación (Status 400, etc)
                    console.error('❌ Error de compilación:', result.error);
                    if (errorOutputEl) {
                        // Muestra el mensaje de error que viene del backend
                        const errorMsg = result.error || result.message || 'Error desconocido';
                        errorOutputEl.innerText = `❌ ERROR DE COMPILACIÓN:\n${errorMsg}`;
                        errorOutputEl.style.color = 'red';
                    }
                }
                
            } catch (e) {
                console.error('💥 Error de conexión:', e);
                if (errorOutputEl) {
                    errorOutputEl.innerText = '❌ Error de conexión con el servidor: ¿Está el servidor Java en ejecución?\nDetalle: ' + e.message;
                    errorOutputEl.style.color = 'red';
                }
            } finally {
                // Restaurar estado del botón
                isCompiling = false;
                compileButton.disabled = false;
                compileButton.textContent = 'Compilar y Ejecutar';
            }
        });
        
        console.log('✅ Sistema de compilación inicializado');
        
    } else {
        console.error('❌ No se pudo inicializar el compilador. Verifica que existan los elementos necesarios.');
    }

}); // Fin DOMContentLoaded