import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Mock battery reader for testing and non-macOS systems
 */
public class MockBatteryReader implements IBatteryInfoReader {
    
    private Random random = new Random();
    private double simulatedCharge = 80.0;
    private long startTime = System.currentTimeMillis();
    
    @Override
    public BatteryState readBatteryInfo() {
        // Simulate battery discharge over time
        long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
        double dischargeRate = 0.5; // 0.5% per minute
        simulatedCharge = Math.max(5, 80.0 - (elapsedSeconds / 60.0 * dischargeRate));
        
        // Add some random variation
        double variation = (random.nextDouble() - 0.5) * 2; // ±1%
        double actualCharge = Math.max(0, Math.min(100, simulatedCharge + variation));
        
        double voltage = 3.7 * (actualCharge / 100.0) + 0.3; // Voltage drops with charge
        double current = 500 + random.nextDouble() * 100; // 500-600 mA
        double temperature = 25 + random.nextDouble() * 10; // 25-35°C
        boolean isCharging = actualCharge < 20; // Simulate charging when low
        
        return new BatteryState(actualCharge, voltage, current, temperature, isCharging);
    }
    
    @Override
    public Map<String, Object> getBatteryHealth() {
        Map<String, Object> health = new HashMap<>();
        
        health.put("MaxCapacity", 5000);
        health.put("CurrentCapacity", (int)(5000 * simulatedCharge / 100));
        health.put("Voltage", (int)(3700 * (simulatedCharge / 100.0)));
        health.put("Temperature", 2500 + random.nextInt(1000));
        health.put("IsCharging", simulatedCharge < 20 ? "Yes" : "No");
        health.put("CycleCount", 100 + random.nextInt(50));
        
        return health;
    }
    
    @Override
    public boolean isBatteryAvailable() {
        return true; // Mock is always available
    }
    
    /**
     * Reset simulation
     */
    public void resetSimulation() {
        simulatedCharge = 80.0;
        startTime = System.currentTimeMillis();
    }
}
