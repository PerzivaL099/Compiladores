package compilador.semantico;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogService {
    
    // El archivo de base de datos SQLite se creará en la raíz del proyecto
    private static final String DB_URL = "jdbc:sqlite:compiler_logs.db"; 
    private static final String TABLE_NAME = "compilation_logs";

    public LogService() {
        // Inicializa el driver y asegura que la tabla exista al iniciar el servidor
        try {
            // Cargar el driver de SQLite (requiere el slf4j-api.jar y el sqlite-jdbc.jar)
            Class.forName("org.sqlite.JDBC"); 
            createTable();
        } catch (ClassNotFoundException e) {
            System.err.println("Error: El driver SQLite JDBC no se encontró en el classpath.");
            throw new RuntimeException("Falta la dependencia SQLite JDBC.", e);
        }
    }

    private Connection getConnection() throws SQLException {
        // Establece la conexión. SQLite creará el archivo si no existe.
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Crea la tabla de logs si aún no existe.
     */
    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "user_id TEXT NOT NULL,"
                + "timestamp TEXT NOT NULL,"
                + "source_code TEXT," // Almacena el código fuente completo
                + "assembly_output TEXT," // Almacena el código ensamblador
                + "dot_output TEXT"       // Almacena el código DOT
                + ");";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("-> Log Service: Base de datos SQLite lista.");
        } catch (SQLException e) {
            System.err.println("Error al crear la tabla de la base de datos: " + e.getMessage());
        }
    }

    /**
     * Registra una entrada de compilación exitosa en la base de datos.
     */
    public void recordLog(String userId, String sourceCode, String assemblyOutput, String dotOutput) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        String sql = "INSERT INTO " + TABLE_NAME + " (user_id, timestamp, source_code, assembly_output, dot_output) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Usamos PreparedStatement para prevenir inyecciones SQL
            pstmt.setString(1, userId);
            pstmt.setString(2, timestamp);
            pstmt.setString(3, sourceCode);
            pstmt.setString(4, assemblyOutput);
            pstmt.setString(5, dotOutput);
            
            pstmt.executeUpdate();
            System.out.println("Log registrado exitosamente para usuario: " + userId);
            
        } catch (SQLException e) {
            System.err.println("Error al registrar el log: " + e.getMessage());
        }
    }
}
