import java.awt.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles system alerts and notifications
 */
public class AlertSystem {
    private static Map<String, LocalDateTime> lastAlertTime = new HashMap<>();
    
    /**
     * Send a system alert/notification
     */
    public static void sendAlert(String message) {
        sendAlert("Battery Alert", message);
    }
    
    /**
     * Send a system alert with custom title
     */
    public static void sendAlert(String title, String message) {
        System.out.println("\n⚠️ ALERT: " + message);
        
        // Check if system tray is supported
        if (SystemTray.isSupported()) {
            try {
                SystemTray tray = SystemTray.getSystemTray();
                
                // Create an icon (you would need an actual icon file)
                Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
                
                TrayIcon trayIcon = new TrayIcon(image, "Battery Monitor");
                trayIcon.setImageAutoSize(true);
                trayIcon.setToolTip("Battery Monitor");
                
                tray.add(trayIcon);
                
                // Display notification
                trayIcon.displayMessage(title, message, TrayIcon.MessageType.WARNING);
                
                // Remove icon after notification
                Thread.sleep(3000);
                tray.remove(trayIcon);
                
            } catch (Exception e) {
                System.err.println("Error showing system notification: " + e.getMessage());
                // Fallback to console output (already done above)
            }
        }
    }
    
    /**
     * Send alert with cooldown check
     */
    public static void sendAlertWithCooldown(String alertKey, String message, int cooldownHours) {
        LocalDateTime now = LocalDateTime.now();
        
        if (lastAlertTime.containsKey(alertKey)) {
            LocalDateTime lastAlert = lastAlertTime.get(alertKey);
            long hoursSince = java.time.Duration.between(lastAlert, now).toHours();
            
            if (hoursSince < cooldownHours) {
                System.out.println("Alert suppressed (cooldown): " + message);
                return;
            }
        }
        
        sendAlert(message);
        lastAlertTime.put(alertKey, now);
    }
    
    /**
     * Clear alert history for a specific key
     */
    public static void clearAlertHistory(String alertKey) {
        lastAlertTime.remove(alertKey);
    }
    
    /**
     * Clear all alert history
     */
    public static void clearAllAlertHistory() {
        lastAlertTime.clear();
    }
}
