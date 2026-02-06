import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Battery information reader for macOS systems
 */
public class MacOSBatteryReader implements IBatteryInfoReader {
    
    @Override
    public BatteryState readBatteryInfo() {
        try {
            Map<String, Object> batteryInfo = getBatteryHealth();
            
            double chargeLevel = 0;
            double voltage = 0;
            double current = 0;
            double temperature = 25; // Default
            boolean isCharging = false;
            
            if (batteryInfo.containsKey("MaxCapacity") && batteryInfo.containsKey("CurrentCapacity")) {
                int maxCapacity = (Integer) batteryInfo.get("MaxCapacity");
                int currentCapacity = (Integer) batteryInfo.get("CurrentCapacity");
                
                if (maxCapacity > 0) {
                    chargeLevel = (currentCapacity * 100.0) / maxCapacity;
                }
            }
            
            if (batteryInfo.containsKey("Voltage")) {
                voltage = ((Integer) batteryInfo.get("Voltage")) / 1000.0;
                current = voltage * 218; // Approximate current
            }
            
            if (batteryInfo.containsKey("Temperature")) {
                temperature = ((Integer) batteryInfo.get("Temperature")) / 100.0;
            }
            
            if (batteryInfo.containsKey("IsCharging")) {
                isCharging = "Yes".equals(batteryInfo.get("IsCharging"));
            }
            
            return new BatteryState(chargeLevel, voltage, current, temperature, isCharging);
            
        } catch (Exception e) {
            System.err.println("Error reading battery info: " + e.getMessage());
            return new BatteryState(100, 3.7, 0, 25, false);
        }
    }
    
    @Override
    public Map<String, Object> getBatteryHealth() {
        Map<String, Object> batteryInfo = new HashMap<>();
        
        try {
            Process process = Runtime.getRuntime().exec(
                new String[]{"ioreg", "-r", "-c", "AppleSmartBattery"}
            );
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Capacity") || line.contains("Voltage") || 
                    line.contains("Current") || line.contains("Time") || 
                    line.contains("IsCharg") || line.contains("Temp") || 
                    line.contains("MaxCapacity") || line.contains("level") || 
                    line.contains("CycleCount")) {
                    
                    String[] parts = line.trim().split("=");
                    if (parts.length == 2) {
                        String key = parts[0].trim().replace("\"", "");
                        String value = parts[1].trim();
                        
                        if (value.equals("Yes") || value.equals("No")) {
                            batteryInfo.put(key, value);
                        } else {
                            try {
                                batteryInfo.put(key, Integer.parseInt(value));
                            } catch (NumberFormatException e) {
                                batteryInfo.put(key, value);
                            }
                        }
                    }
                }
            }
            
            reader.close();
            process.waitFor();
            
        } catch (Exception e) {
            System.err.println("Error reading battery health: " + e.getMessage());
        }
        
        return batteryInfo;
    }
    
    @Override
    public boolean isBatteryAvailable() {
        try {
            Process process = Runtime.getRuntime().exec(
                new String[]{"ioreg", "-r", "-c", "AppleSmartBattery"}
            );
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            boolean available = reader.readLine() != null;
            reader.close();
            process.waitFor();
            
            return available;
            
        } catch (Exception e) {
            return false;
        }
    }
}
