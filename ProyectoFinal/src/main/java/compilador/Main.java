package compilador;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject; 
//import spark.Session; 

public class Main {

    public static void main(String[] args) {
        
        CompilerService compilerService = new CompilerService();
        Gson gson = new Gson();

        // 1. CONFIGURACIÓN DEL SERVIDOR Y SESIONES
        ipAddress("127.0.0.1");
        port(4567);
        // La gestión de sesiones de Spark es complicada y no necesaria para la compilación, se mantiene comentada.
        /*
        // sessionManagement(config -> {
        //   config.sessionTrackingMode("cookie");
        // });
        */
        
        // --- FILTROS DE SEGURIDAD (DESACTIVADOS PARA LA PRUEBA) ---
        // Se elimina el filtro 'before /compile' que requiere el user_id
        
        // --- HABILITAR CORS (Cambios para solucionar 'Failed to fetch') ---
        // Se añade la cabecera Access-Control-Allow-Headers para permitir el Content-Type (necesario para POST)
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                // *** CAMBIO CLAVE: Permitir cabeceras como Content-Type y otras requeridas ***
                response.header("Access-Control-Allow-Headers", "Content-Type, " + accessControlRequestHeaders);
            } else {
                response.header("Access-Control-Allow-Headers", "Content-Type");
            }
            
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            } else {
                 response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            }
            return "OK";
        });
        
        // El * permite que cualquier origen (incluyendo tu front-end en 127.0.0.1:5500) acceda a los recursos
        before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));


        // --- ENDPOINTS DE LA APLICACIÓN ---

        // 3. ENDPOINT DE LOGIN (SE MANTIENE ACTIVO PARA PRUEBAS)
        // Nota: Es posible que necesites añadir headers CORS también en el post-response si el front-end usa credenciales.
        post("/login", (request, response) -> {
            response.type("application/json");
            
            try {
                JsonObject json = gson.fromJson(request.body(), JsonObject.class);
                String username = json.get("username").getAsString();
                String password = json.get("password").getAsString();

                // LÓGICA DE AUTENTICACIÓN HARDCODEADA
                if ("admin".equals(username) && "1234".equals(password)) { 
                    request.session(true).attribute("user_id", username); 
                    return gson.toJson(new LoginResult(true, "Sesión iniciada."));
                }
                
                response.status(401); 
                return gson.toJson(new LoginResult(false, "Credenciales inválidas."));
                
            } catch (Exception e) {
                 response.status(400); 
                 return gson.toJson(new LoginResult(false, "Formato de petición incorrecto."));
            }
        });


        // 4. ENDPOINT DE COMPILACIÓN (ACCESO ABIERTO)
        post("/compile", (request, response) -> {
            response.type("application/json");
            
            String sourceCode = request.body();
            
            // Llama al CompilerService sin pasar el userId
            CompilerResult result = compilerService.compile(sourceCode); 
            
            // ⭐⭐⭐ IMPLEMENTACIÓN DEL CÓDIGO DE ESTADO HTTP ⭐⭐⭐
            if (result.isSuccess()) {
                response.status(200); // OK: Todo el proceso de compilación fue exitoso
            } else {
                // ERROR: Error léxico, sintáctico o semántico.
                // Usamos 400 Bad Request para indicar un fallo en los datos de entrada (el código fuente).
                response.status(400); 
            }
            
            return gson.toJson(result);
        });

        System.out.println("--- SERVIDOR COMPILADOR INICIADO EN http://127.0.0.1:4567 ---");
    }
}

// Clase auxiliar para el resultado del login (necesaria para Gson)
class LoginResult {
    boolean success;
    String message;

    public LoginResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}