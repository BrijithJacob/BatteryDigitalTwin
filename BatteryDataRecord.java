import java.time.LocalDateTime;

/**
 * Represents a single battery data record from the database
 */
public class BatteryDataRecord {
    private LocalDateTime timestamp;
    private double chargePercentage;
    private double temperature;
    private double voltage;
    private boolean isCharging;
    private double dailyMaxSoc;
    
    public BatteryDataRecord(LocalDateTime timestamp, double chargePercentage, 
                            double temperature, double voltage, 
                            boolean isCharging, double dailyMaxSoc) {
        this.timestamp = timestamp;
        this.chargePercentage = chargePercentage;
        this.temperature = temperature;
        this.voltage = voltage;
        this.isCharging = isCharging;
        this.dailyMaxSoc = dailyMaxSoc;
    }
    
    // Getters
    public LocalDateTime getTimestamp() { return timestamp; }
    public double getChargePercentage() { return chargePercentage; }
    public double getTemperature() { return temperature; }
    public double getVoltage() { return voltage; }
    public boolean isCharging() { return isCharging; }
    public double getDailyMaxSoc() { return dailyMaxSoc; }
    
    // Setters
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public void setChargePercentage(double chargePercentage) { this.chargePercentage = chargePercentage; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public void setVoltage(double voltage) { this.voltage = voltage; }
    public void setCharging(boolean charging) { isCharging = charging; }
    public void setDailyMaxSoc(double dailyMaxSoc) { this.dailyMaxSoc = dailyMaxSoc; }
    
    @Override
    public String toString() {
        return String.format("BatteryDataRecord[time=%s, charge=%.1f%%, temp=%.1f°C, voltage=%.2fV, charging=%b]",
                timestamp, chargePercentage, temperature, voltage, isCharging);
    }
}
