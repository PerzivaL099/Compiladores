package compilador;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject; // Necesario para parsear el JSON de login
import spark.Session; // Necesario para gestionar sesiones

public class Main {

    public static void main(String[] args) {
        
        CompilerService compilerService = new CompilerService();
        Gson gson = new Gson();

        // 1. CONFIGURACIÓN DEL SERVIDOR Y SESIONES
        ipAddress("127.0.0.1");
        port(4567);
        /*
         // Habilitar sesiones (necesario para guardar el user_id)
        sessionManagement(config -> {
            config.sessionTrackingMode("cookie");
        });
         */
        


        // 2. FILTRO DE SEGURIDAD PARA /compile (Protege la ruta)
        before("/compile", (request, response) -> {
            Session session = request.session(false); // Obtener sesión, no crearla
            // Si no hay sesión O no hay user_id en la sesión, denegar el acceso
            if (session == null || session.attribute("user_id") == null) {
                // Halt detiene el procesamiento de la petición y devuelve un código de error
                halt(401, gson.toJson(new CompilerResult("Acceso denegado. Por favor, inicie sesión.")));
            }
        });
        
        // Habilitar CORS (sin cambios)
        options("/*", (request, response) -> {
            // ... (Lógica CORS sin cambios)
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });
        before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));


        // --- ENDPOINTS DE LA APLICACIÓN ---

        // 3. ENDPOINT DE LOGIN
        post("/login", (request, response) -> {
            response.type("application/json");
            
            // Asume que el front-end envía { "username": "user", "password": "pwd" }
            try {
                JsonObject json = gson.fromJson(request.body(), JsonObject.class);
                String username = json.get("username").getAsString();
                String password = json.get("password").getAsString();

                // *** LÓGICA DE AUTENTICACIÓN HARDCODEADA ***
                if ("admin".equals(username) && "1234".equals(password)) { 
                    // Éxito: Crear sesión y guardar ID
                    request.session(true).attribute("user_id", username); 
                    return gson.toJson(new LoginResult(true, "Sesión iniciada."));
                }
                
                response.status(401); // 401 Unauthorized
                return gson.toJson(new LoginResult(false, "Credenciales inválidas."));
                
            } catch (Exception e) {
                 response.status(400); // 400 Bad Request
                 return gson.toJson(new LoginResult(false, "Formato de petición incorrecto."));
            }
        });


        // 4. ENDPOINT DE COMPILACIÓN (MODIFICADO para incluir User ID)
        post("/compile", (request, response) -> {
            response.type("application/json");
            
            // Obtener el ID de usuario de la sesión (garantizado por el filtro 'before')
            String userId = request.session().attribute("user_id");
            
            String sourceCode = request.body();
            
            // Llama al CompilerService, pasándole el ID de usuario
            CompilerResult result = compilerService.compile(sourceCode, userId); 
            
            return gson.toJson(result);
        });

        System.out.println("--- SERVIDOR COMPILADOR INICIADO EN http://localhost:4567 ---");
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