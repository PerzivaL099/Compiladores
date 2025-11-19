package compilador;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import spark.Session;

public class Main {

    public static void main(String[] args) {
        
        CompilerService compilerService = new CompilerService();
        Gson gson = new Gson();

        // 1. CONFIGURACIÓN DEL SERVIDOR
        ipAddress("127.0.0.1");
        port(4567);

        // 2. ⭐ CONFIGURACIÓN CORS - DEBE IR ANTES DE TODAS LAS RUTAS
        before((request, response) -> {
            // IMPORTANTE: Usar el origin específico en lugar de "*" cuando usas credentials
            response.header("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
            response.header("Access-Control-Allow-Credentials", "true");
        });

        // Manejar solicitudes OPTIONS (preflight de CORS)
        options("/*", (request, response) -> {
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

        // 3. FILTRO DE SEGURIDAD PARA /compile
        before("/compile", (request, response) -> {
            Session session = request.session(false);
            if (session == null || session.attribute("user_id") == null) {
                halt(401, gson.toJson(new CompilerResult("Acceso denegado. Por favor, inicie sesión.")));
            }
        });

        // --- ENDPOINTS DE LA APLICACIÓN ---

        // 4. ENDPOINT DE LOGIN
        post("/login", (request, response) -> {
            response.type("application/json");
            
            try {
                JsonObject json = gson.fromJson(request.body(), JsonObject.class);
                String username = json.get("username").getAsString();
                String password = json.get("password").getAsString();

                System.out.println("🔐 Intento de login - Usuario: " + username);

                // Validación de credenciales
                if ("admin".equals(username) && "1234".equals(password)) { 
                    request.session(true).attribute("user_id", username);
                    System.out.println("✅ Login exitoso para: " + username);
                    return gson.toJson(new LoginResult(true, "Sesión iniciada."));
                }
                
                System.out.println("❌ Credenciales inválidas para: " + username);
                response.status(401);
                return gson.toJson(new LoginResult(false, "Credenciales inválidas."));
                
            } catch (Exception e) {
                System.out.println("⚠️ Error en login: " + e.getMessage());
                e.printStackTrace();
                response.status(400);
                return gson.toJson(new LoginResult(false, "Formato de petición incorrecto."));
            }
        });

        // 5. ENDPOINT DE COMPILACIÓN
        post("/compile", (request, response) -> {
            response.type("application/json");
            
            String userId = request.session().attribute("user_id");
            String sourceCode = request.body();
            
            System.out.println("📝 Compilando código para usuario: " + userId);
            
            CompilerResult result = compilerService.compile(sourceCode, userId); 
            
            return gson.toJson(result);
        });

        // 6. ENDPOINT DE PRUEBA (para verificar que el servidor funciona)
        get("/", (request, response) -> {
            response.type("application/json");
            return "{\"status\":\"Server is running\",\"version\":\"1.0\"}";
        });

        System.out.println("✅ SERVIDOR COMPILADOR INICIADO EN http://127.0.0.1:4567");
        System.out.println("📋 Endpoints disponibles:");
        System.out.println("   - POST /login");
        System.out.println("   - POST /compile");
        System.out.println("   - GET  /");
    }
}

// Clase auxiliar para el resultado del login
class LoginResult {
    boolean success;
    String message;

    public LoginResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}