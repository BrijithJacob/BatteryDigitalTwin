/**
 * Handles power management functions
 */
public class PowerManagement {
    private static boolean powerSavingMode = false;
    
    // Constants
    public static final double DISCHARGE_RATE_THRESHOLD = 8.0;
    public static final int ALERT_COOLDOWN_HOURS = 24;
    
    /**
     * Enable power saving mode
     */
    public static void enablePowerSavingMode() {
        if (powerSavingMode) {
            return;
        }
        
        String os = System.getProperty("os.name").toLowerCase();
        
        try {
            if (os.contains("mac")) {
                // macOS
                Runtime.getRuntime().exec("pmset -a lowpowermode 1");
                System.out.println("Power saving mode enabled (macOS)");
            } else if (os.contains("win")) {
                // Windows - requires admin privileges
                Runtime.getRuntime().exec("powercfg /setactive 8c5e7fda-e8bf-4a96-9a85-a6e23a8c635c");
                System.out.println("Power saving mode enabled (Windows)");
            } else if (os.contains("nix") || os.contains("nux")) {
                // Linux
                Runtime.getRuntime().exec("sudo cpufreq-set -g powersave");
                System.out.println("Power saving mode enabled (Linux)");
            }
            
            powerSavingMode = true;
            
        } catch (Exception e) {
            System.err.println("Error enabling power saving: " + e.getMessage());
        }
    }
    
    /**
     * Disable power saving mode
     */
    public static void disablePowerSavingMode() {
        if (!powerSavingMode) {
            return;
        }
        
        String os = System.getProperty("os.name").toLowerCase();
        
        try {
            if (os.contains("mac")) {
                Runtime.getRuntime().exec("pmset -a lowpowermode 0");
                System.out.println("Power saving mode disabled (macOS)");
            } else if (os.contains("win")) {
                Runtime.getRuntime().exec("powercfg /setactive 381b4222-f694-41f0-9685-ff5bb260df2e");
                System.out.println("Power saving mode disabled (Windows)");
            } else if (os.contains("nix") || os.contains("nux")) {
                Runtime.getRuntime().exec("sudo cpufreq-set -g ondemand");
                System.out.println("Power saving mode disabled (Linux)");
            }
            
            powerSavingMode = false;
            
        } catch (Exception e) {
            System.err.println("Error disabling power saving: " + e.getMessage());
        }
    }
    
    /**
     * Check if power saving mode is enabled
     */
    public static boolean isPowerSavingMode() {
        return powerSavingMode;
    }
}
