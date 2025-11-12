package compilador;

// Importa SparkJava
import static spark.Spark.*;

// Importa Gson (para convertir el resultado a JSON)
import com.google.gson.Gson;

public class Main {

    public static void main(String[] args) {
        
        // Inicializa el servicio del compilador
        CompilerService compilerService = new CompilerService();
        Gson gson = new Gson();

        // Configura el puerto del servidor (ej. 4567)
        port(4567);

        // Habilitar CORS para que tu front-end (en otro puerto) pueda llamarlo
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
        before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));


        // Define el "endpoint" de la API: /compile
        // El front-end enviará el código aquí usando un POST
        post("/compile", (request, response) -> {
            
            // 1. Obtener el código fuente del cuerpo de la petición
            String sourceCode = request.body();
            
            // 2. Compilarlo
            CompilerResult result = compilerService.compile(sourceCode);
            
            // 3. Devolver el resultado como JSON
            response.type("application/json");
            return gson.toJson(result);
        });

        System.out.println("--- SERVIDOR COMPILADOR INICIADO EN http://localhost:4567 ---");
    }
}