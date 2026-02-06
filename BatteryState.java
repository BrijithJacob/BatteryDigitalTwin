/**
 * Represents the current state of a battery
 */
public class BatteryState {
    private double chargeLevel;
    private double voltage;
    private double current;
    private double temperature;
    private boolean isCharging;
    
    public BatteryState(double chargeLevel, double voltage, double current, double temperature) {
        this.chargeLevel = chargeLevel;
        this.voltage = voltage;
        this.current = current;
        this.temperature = temperature;
        this.isCharging = false;
    }
    
    public BatteryState(double chargeLevel, double voltage, double current, 
                       double temperature, boolean isCharging) {
        this.chargeLevel = chargeLevel;
        this.voltage = voltage;
        this.current = current;
        this.temperature = temperature;
        this.isCharging = isCharging;
    }
    
    // Getters
    public double getChargeLevel() { return chargeLevel; }
    public double getVoltage() { return voltage; }
    public double getCurrent() { return current; }
    public double getTemperature() { return temperature; }
    public boolean isCharging() { return isCharging; }
    
    // Setters
    public void setChargeLevel(double chargeLevel) { this.chargeLevel = chargeLevel; }
    public void setVoltage(double voltage) { this.voltage = voltage; }
    public void setCurrent(double current) { this.current = current; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public void setCharging(boolean charging) { isCharging = charging; }
    
    @Override
    public String toString() {
        return String.format("BatteryState[charge=%.1f%%, voltage=%.2fV, current=%.2fmA, temp=%.1f°C, charging=%b]",
                chargeLevel, voltage, current, temperature, isCharging);
    }
}
