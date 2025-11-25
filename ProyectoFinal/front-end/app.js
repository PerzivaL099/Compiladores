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
    
    // ⭐ NUEVAS REFERENCIAS DE TEMA Y PESTAÑAS ⭐
    const themeToggle = document.getElementById('themeToggle');
    const body = document.body;
    const tabButtons = document.querySelectorAll('.tab-button');
    const symbolTableBody = document.querySelector('#symbolTable tbody'); // Asume que la tabla está lista
    
    // -----------------------------------------------------
    // --- LÓGICA DE MANEJO DE TEMA (Modo Claro/Oscuro) ---
    // -----------------------------------------------------
    function toggleTheme() {
        body.classList.toggle('dark-mode');
        const isDarkMode = body.classList.contains('dark-mode');
        
        themeToggle.textContent = isDarkMode ? '☀️ Modo Claro' : '🌙 Modo Oscuro';
        sourceCodeEditor.setOption("theme", isDarkMode ? "monokai" : "default");
        localStorage.setItem('theme', isDarkMode ? 'dark' : 'light');
    }
    
    // Inicializar CodeMirror
    if (codeEditorContainer) {
        sourceCodeEditor = CodeMirror(codeEditorContainer, {
            value: "// Pega tu código de MiniJava aquí...\nint x;\nx = 5;",
            mode: "clike",
            lineNumbers: true,
            theme: "default" // Se establecerá el tema final al cargar
        });
        console.log('✅ Editor CodeMirror inicializado');
    }
    
    // ⭐ LÓGICA DE INICIALIZACIÓN DE TEMA AL CARGAR ⭐
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
        body.classList.add('dark-mode');
        themeToggle.textContent = '☀️ Modo Claro';
        // Esto funciona porque el CodeMirror ya está inicializado
        if (sourceCodeEditor) sourceCodeEditor.setOption("theme", "monokai");
    } else {
        themeToggle.textContent = '🌙 Modo Oscuro';
        if (sourceCodeEditor) sourceCodeEditor.setOption("theme", "default");
    }
    
    // ⭐ ASIGNAR EL EVENTO DEL BOTÓN DE TEMA ⭐
    if (themeToggle) {
        themeToggle.addEventListener('click', toggleTheme);
    }
    
    // -----------------------------------------------------------------
    // --- LÓGICA DE MANEJO DE PESTAÑAS (Ensamblador / Tabla Símbolos) ---
    // -----------------------------------------------------------------
    tabButtons.forEach(button => {
        button.addEventListener('click', (e) => {
            const targetId = e.target.getAttribute('data-target');
            const targetContent = document.getElementById(targetId);
            // El contenedor padre debe ser #mid-output-column (ajustado en HTML)
            const parentContainer = e.target.closest('#mid-output-column'); 
            
            if (!parentContainer) return;

            // Ocultar todos los contenidos y desactivar todos los botones
            parentContainer.querySelectorAll('.tab-content').forEach(content => {
                content.classList.add('hidden');
            });
            parentContainer.querySelectorAll('.tab-button').forEach(btn => {
                btn.classList.remove('active');
            });

            // Mostrar el contenido objetivo y activar el botón
            if (targetContent) {
                targetContent.classList.remove('hidden');
            }
            e.target.classList.add('active');
        });
    });

    // -------------------------------------------------------------
    // --- FUNCIÓN DE RENDERIZADO DE TABLA DE SÍMBOLOS ---
    // -------------------------------------------------------------
    function renderSymbolTable(symbolTable) {
        if (!symbolTableBody) return;

        // Limpiar contenido anterior
        symbolTableBody.innerHTML = ''; 

        if (!symbolTable || symbolTable.length === 0) {
            symbolTableBody.innerHTML = '<tr><td colspan="4">No hay símbolos definidos o detectados.</td></tr>';
            return;
        }

        // Llenar la tabla con los datos del JSON (SimboloDTO)
        symbolTable.forEach(simbolo => {
            const row = symbolTableBody.insertRow();
            // Los campos coinciden con el DTO (name, type, scope, address)
            row.insertCell().textContent = simbolo.name; 
            row.insertCell().textContent = simbolo.type;
            row.insertCell().textContent = simbolo.scope;
            row.insertCell().textContent = simbolo.address || 'N/A';
        });

        console.log(`✅ Tabla de Símbolos renderizada con ${symbolTable.length} entradas.`);
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


    // -------------------------------------------------------------
    // --- FUNCIÓN PRINCIPAL DE COMPILACIÓN (con consumo de Tabla) ---
    // -------------------------------------------------------------
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
                // Enviar el código al servidor Java
                const response = await fetch('http://127.0.0.1:4567/compile', {
                    method: 'POST',
                    headers: { 'Content-Type': 'text/plain' },
                    body: code,
                });

                console.log('📡 Respuesta recibida - Status:', response.status);
                
                const result = await response.json();
                // console.log('📦 Resultado completo:', result); // Mantener para depuración
                
                // Procesar resultados
                if (response.ok && result.success) {
                    // Compilación exitosa
                    console.log('✅ Compilación exitosa');
                    
                    if (result.asmCode) {
                        asmOutputEl.value = result.asmCode;
                    } 
                    if (result.dotCode) {
                        renderDiagram(result.dotCode);
                    }
                    
                    // ⭐ CONSUMIR LA TABLA DE SÍMBOLOS ⭐
                    if (result.symbolTable) {
                        renderSymbolTable(result.symbolTable);
                        // Opcional: Cambiar automáticamente a la pestaña Ensamblador después de compilar
                        document.getElementById('asmTabButton').click(); 
                    }

                    if (errorOutputEl) {
                        errorOutputEl.innerText = '✅ Compilación y generación exitosa.';
                        errorOutputEl.style.color = 'green';
                    }
                    
                } else if (response.status === 401) {
                    // Sesión expirada
                    // Lógica de redireccionamiento
                    // ...
                    
                } else {
                    // Error de compilación (status 400)
                    console.error('❌ Error de compilación:', result.error);
                    if (errorOutputEl) {
                        errorOutputEl.innerText = `❌ ERROR DE COMPILACIÓN:\n${result.error || 'Error desconocido'}`;
                        errorOutputEl.style.color = 'red';
                    }
                }
                
            } catch (e) {
                // Error de red/conexión
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