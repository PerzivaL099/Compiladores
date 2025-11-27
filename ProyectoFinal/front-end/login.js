// login.js - Maneja la autenticación de usuarios

document.addEventListener('DOMContentLoaded', () => {
    const loginButton = document.getElementById('loginButton');
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const messageDiv = document.getElementById('message');

    // Función de login
    const performLogin = async () => {
        const username = usernameInput.value;
        const password = passwordInput.value;
        messageDiv.innerText = ''; // Limpiar mensajes

        // Validación básica
        if (!username || !password) {
            messageDiv.style.color = 'red';
            messageDiv.innerText = 'Por favor ingrese usuario y contraseña.';
            return;
        }

        const payload = JSON.stringify({ username: username, password: password });

        try {
            console.log('🔐 Intentando login con usuario:', username);

            // Enviar las credenciales al servidor
            const response = await fetch('http://127.0.0.1:4567/login', {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json'
                }, 
                body: payload,
                credentials: 'include' // 
            });

            console.log('📡 Respuesta del servidor - Status:', response.status);

            const result = await response.json();
            console.log('📦 Datos recibidos:', result);

            // Procesar el resultado
            if (response.ok) { 
                console.log('✅ Login exitoso');
                messageDiv.style.color = 'green';
                messageDiv.innerText = result.message + " Redirigiendo...";
                
                // Redirigir después del éxito
                setTimeout(() => {
                    window.location.href = 'index.html';
                }, 1000);
                
            } else { 
                console.log('❌ Login fallido:', result.message);
                messageDiv.style.color = 'red';
                messageDiv.innerText = "Error: " + (result.message || "Credenciales inválidas.");
            }

        } catch (e) {
            // Error de red (Servidor Java no corriendo o CORS)
            console.error('💥 Error de conexión:', e);
            messageDiv.style.color = 'red';
            messageDiv.innerText = 'Error de conexión: Asegúrese que el servidor Java esté corriendo.';
        }
    };

    // Click en el botón de login
    loginButton.addEventListener('click', async (e) => {
        e.preventDefault();
        await performLogin();
    });

    // Permitir login con Enter en el campo de contraseña
    passwordInput.addEventListener('keypress', async (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            await performLogin();
        }
    });

    // Permitir login con Enter en el campo de usuario
    usernameInput.addEventListener('keypress', async (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            await performLogin();
        }
    });
    
    console.log('✅ Sistema de login inicializado');
});