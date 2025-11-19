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
    
    // Inicializar CodeMirror
    if (codeEditorContainer) {
        sourceCodeEditor = CodeMirror(codeEditorContainer, {
            value: "// Pega tu código de MiniJava aquí...\nint x;\nx = 5;",
            mode: "clike",
            lineNumbers: true,
            theme: "default"
        });
        console.log('✅ Editor CodeMirror inicializado');
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
                const response = await fetch('http://127.0.0.1:4567/compile', {
                    method: 'POST',
                    headers: { 'Content-Type': 'text/plain' },
                    body: code,
                    credentials: 'include'
                });

                console.log('📡 Respuesta recibida - Status:', response.status);
                
                const result = await response.json();
                console.log('📦 Resultado completo:', result);
                console.log('   - success:', result.success);
                console.log('   - asmCode length:', result.asmCode ? result.asmCode.length : 0);
                console.log('   - dotCode length:', result.dotCode ? result.dotCode.length : 0);
                console.log('   - error:', result.error);

                // Procesar resultados
                if (response.ok && result.success) {
                    // Compilación exitosa
                    console.log('✅ Compilación exitosa');
                    
                    if (result.asmCode) {
                        asmOutputEl.value = result.asmCode;
                        console.log('✅ Ensamblador mostrado');
                    } else {
                        console.warn('⚠️ No hay código ensamblador en la respuesta');
                    }
                    
                    if (result.dotCode) {
                        renderDiagram(result.dotCode);
                    } else {
                        console.warn('⚠️ No hay diagrama DOT en la respuesta');
                    }

                    if (errorOutputEl) {
                        errorOutputEl.innerText = '✅ Compilación y generación exitosa.';
                        errorOutputEl.style.color = 'green';
                    }
                    
                } else if (response.status === 401) {
                    // Sesión expirada
                    console.warn('⚠️ Sesión expirada');
                    if (errorOutputEl) {
                        errorOutputEl.innerText = '⚠️ Sesión expirada. Redirigiendo al login...';
                        errorOutputEl.style.color = 'orange';
                    }
                    setTimeout(() => {
                        window.location.href = 'login.html';
                    }, 2000);
                    
                } else {
                    // Error de compilación
                    console.error('❌ Error de compilación:', result.error);
                    if (errorOutputEl) {
                        errorOutputEl.innerText = `❌ ERROR DE COMPILACIÓN:\n${result.error || 'Error desconocido'}`;
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