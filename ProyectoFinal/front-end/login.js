document.addEventListener('DOMContentLoaded', () => {
    const loginButton = document.getElementById('loginButton');
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const messageDiv = document.getElementById('message');

    loginButton.addEventListener('click', async () => {
        
        const username = usernameInput.value;
        const password = passwordInput.value;
        messageDiv.innerText = ''; // Limpiar mensajes

        const payload = JSON.stringify({ username: username, password: password });

       try {
            // 1. Enviar las credenciales al servidor
            const response = await fetch('http://127.0.0.1:4567/login', {
                method: 'POST',
                // Indispensable para que el servidor Java sepa que el cuerpo es JSON
                headers: { 'Content-Type': 'application/json' }, 
                body: payload,
                // Crucial para enviar y recibir la cookie de sesión entre diferentes puertos
                credentials: 'include' 
            });

            const result = await response.json();

            // 2. Procesar el resultado
            if (response.ok) { // Verifica que el código de respuesta HTTP sea 200-299
                messageDiv.style.color = 'green';
                messageDiv.innerText = result.message + " Redirigiendo...";
                
                // Redirigir después del éxito
                setTimeout(() => {
                    window.location.href = 'index.html';
                }, 1000);
                
            } else { // Maneja 401, 400, etc.
                messageDiv.style.color = 'red';
                messageDiv.innerText = "Error: " + (result.message || "Credenciales inválidas o servidor no responde con JSON.");
            }

        } catch (e) {
            // Error de red (Servidor Java no corriendo)
            messageDiv.style.color = 'red';
            messageDiv.innerText = 'Error de conexión: Asegúrese que el servidor Java esté corriendo.';
            console.error(e);
        }
    });
});