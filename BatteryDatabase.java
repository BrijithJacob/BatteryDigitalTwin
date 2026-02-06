import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Database handler for battery health data
 */
public class BatteryDatabase {
    private final String dbPath;
    private Connection connection;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    public BatteryDatabase(String dbPath) {
        this.dbPath = dbPath;
        initializeDatabase();
    }
    
    private void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            createTableIfNotExists();
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createTableIfNotExists() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS battery_health (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                timestamp TEXT NOT NULL,
                charge_percentage REAL NOT NULL,
                temperature REAL,
                voltage REAL,
                is_charging TEXT,
                daily_max_soc REAL,
                current REAL
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
    }
    
    /**
     * Insert a battery data record
     */
    public void insertRecord(BatteryDataRecord record) {
        String insertSQL = """
            INSERT INTO battery_health 
            (timestamp, charge_percentage, temperature, voltage, is_charging, daily_max_soc)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            pstmt.setString(1, record.getTimestamp().format(formatter));
            pstmt.setDouble(2, record.getChargePercentage());
            pstmt.setDouble(3, record.getTemperature());
            pstmt.setDouble(4, record.getVoltage());
            pstmt.setString(5, record.isCharging() ? "Yes" : "No");
            pstmt.setDouble(6, record.getDailyMaxSoc());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting record: " + e.getMessage());
        }
    }
    
    /**
     * Get recent battery data records
     */
    public List<BatteryDataRecord> getRecentRecords(int limit) {
        List<BatteryDataRecord> records = new ArrayList<>();
        String query = """
            SELECT timestamp, charge_percentage, temperature, voltage, 
                   is_charging, daily_max_soc
            FROM battery_health
            WHERE is_charging = 'No'
            ORDER BY timestamp DESC
            LIMIT ?
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                LocalDateTime timestamp = LocalDateTime.parse(rs.getString("timestamp"), formatter);
                double chargePercentage = rs.getDouble("charge_percentage");
                double temperature = rs.getDouble("temperature");
                double voltage = rs.getDouble("voltage");
                boolean isCharging = "Yes".equals(rs.getString("is_charging"));
                double dailyMaxSoc = rs.getDouble("daily_max_soc");
                
                records.add(new BatteryDataRecord(
                    timestamp, chargePercentage, temperature, 
                    voltage, isCharging, dailyMaxSoc
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error querying records: " + e.getMessage());
        }
        
        return records;
    }
    
    /**
     * Close database connection
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database: " + e.getMessage());
        }
    }
}
