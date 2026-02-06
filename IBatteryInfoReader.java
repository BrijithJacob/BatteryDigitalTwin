/**
 * Interface for reading battery information from the operating system
 */
public interface IBatteryInfoReader {
    
    /**
     * Read current battery information
     * @return Current battery state
     */
    BatteryState readBatteryInfo();
    
    /**
     * Get battery health information
     * @return Map of battery health metrics
     */
    java.util.Map<String, Object> getBatteryHealth();
    
    /**
     * Check if battery information is available
     * @return true if battery info can be read
     */
    boolean isBatteryAvailable();
}
