import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Main GUI for Battery Digital Twin Visualizer
 */
public class BatteryVisualizer extends JFrame {
    
    // UI Components
    private JLabel titleLabel;
    private JLabel mlStatusLabel;
    private JLabel confidenceLabel;
    private BatteryPanel realBatteryPanel;
    private BatteryPanel predictedBatteryPanel;
    private JSlider timeSlider;
    private JLabel timeLabel;
    
    // Data
    private IBatteryInfoReader batteryReader;
    private MLBatteryModel mlModel;
    private BatteryState currentBatteryState;
    private Timer updateTimer;
    private boolean alertShown = false;
    
    public BatteryVisualizer() {
        super("Battery Digital Twin - Neural Network");
        
        // Initialize battery reader based on OS
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            batteryReader = new MacOSBatteryReader();
        } else {
            // Default/mock reader for other systems
            batteryReader = new MockBatteryReader();
        }
        
        // Initialize ML model
        mlModel = new MLBatteryModel("battery_health.db");
        
        // Train model in background thread
        new Thread(() -> {
            mlModel.trainModel(48, 10, 15);
        }).start();
        
        // Setup UI
        setupUI();
        
        // Start update timer
        startUpdateTimer();
        
        // Window settings
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
    }
    
    private void setupUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Header
        mainPanel.add(createHeaderPanel());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Battery displays
        mainPanel.add(createBatteryDisplaysPanel());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Controls
        mainPanel.add(createControlsPanel());
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        
        titleLabel = new JLabel("Battery Digital Twin - Neural Network (Java)");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        mlStatusLabel = new JLabel("Neural Network: Training...");
        confidenceLabel = new JLabel("");
        
        statusPanel.add(mlStatusLabel);
        statusPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        statusPanel.add(confidenceLabel);
        
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(statusPanel, BorderLayout.CENTER);
        
        return headerPanel;
    }
    
    private JPanel createBatteryDisplaysPanel() {
        JPanel batteriesPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        
        realBatteryPanel = new BatteryPanel("Current Battery State", Color.GREEN);
        predictedBatteryPanel = new BatteryPanel("Neural Network Prediction", Color.BLUE);
        
        batteriesPanel.add(realBatteryPanel);
        batteriesPanel.add(predictedBatteryPanel);
        
        return batteriesPanel;
    }
    
    private JPanel createControlsPanel() {
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        
        timeSlider = new JSlider(0, 100, 0); // 0-10 hours (scaled by 10)
        timeSlider.setMajorTickSpacing(20);
        timeSlider.setMinorTickSpacing(10);
        timeSlider.setPaintTicks(true);
        timeSlider.setPaintLabels(true);
        
        // Custom labels
        java.util.Hashtable<Integer, JLabel> labelTable = new java.util.Hashtable<>();
        labelTable.put(0, new JLabel("0h"));
        labelTable.put(20, new JLabel("2h"));
        labelTable.put(40, new JLabel("4h"));
        labelTable.put(60, new JLabel("6h"));
        labelTable.put(80, new JLabel("8h"));
        labelTable.put(100, new JLabel("10h"));
        timeSlider.setLabelTable(labelTable);
        
        timeSlider.addChangeListener(e -> updateFutureState());
        
        timeLabel = new JLabel("Prediction Time: 0.0 hours");
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        controlsPanel.add(timeSlider);
        controlsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        controlsPanel.add(timeLabel);
        
        return controlsPanel;
    }
    
    private void startUpdateTimer() {
        updateTimer = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateBatteryInfo();
            }
        });
        updateTimer.start();
        
        // Initial update
        updateBatteryInfo();
    }
    
    private void updateBatteryInfo() {
        try {
            currentBatteryState = batteryReader.readBatteryInfo();
            realBatteryPanel.updateDisplay(
                currentBatteryState.getChargeLevel(),
                currentBatteryState.getVoltage(),
                0
            );
            
            // Update ML status
            if (mlModel.isTrainingComplete()) {
                mlStatusLabel.setText("Neural Network: Ready");
            }
            
            // Update prediction
            updateFutureState();
            
        } catch (Exception e) {
            System.err.println("Error updating battery info: " + e.getMessage());
        }
    }
    
    private void updateFutureState() {
        if (currentBatteryState == null) return;
        
        double timeElapsed = timeSlider.getValue() / 10.0; // Convert to hours
        timeLabel.setText(String.format("Prediction Time: %.1f hours", timeElapsed));
        
        try {
            PredictionResult mlPrediction = null;
            double uncertainty = 0;
            
            if (mlModel.isTrainingComplete()) {
                mlPrediction = mlModel.predictFutureState(currentBatteryState, timeElapsed);
                
                if (mlPrediction.isValid()) {
                    uncertainty = mlPrediction.getUncertainty();
                    double confidence = mlPrediction.getConfidence();
                    confidenceLabel.setText(
                        String.format("Prediction Confidence: %.1f%%", confidence * 100)
                    );
                }
            }
            
            // Physics-based fallback
            double currentLevel = currentBatteryState.getChargeLevel();
            double dischargeRate = 5.0; // 5% per hour
            double physicsPrediction = Math.max(0, currentLevel - (dischargeRate * timeElapsed));
            
            // Combine predictions
            double finalPrediction;
            if (mlPrediction != null && mlPrediction.isValid()) {
                double confidence = mlPrediction.getConfidence();
                double mlWeight = 0.8 * confidence;
                double physicsWeight = 1 - mlWeight;
                finalPrediction = (mlPrediction.getPredictedCharge() * mlWeight + 
                                 physicsPrediction * physicsWeight);
            } else {
                finalPrediction = physicsPrediction;
                uncertainty = dischargeRate * timeElapsed * 0.2;
            }
            
            double predictedVoltage = currentBatteryState.getVoltage() * 
                                     (finalPrediction / currentLevel);
            
            predictedBatteryPanel.updateDisplay(finalPrediction, predictedVoltage, uncertainty);
            
            // Check for alerts
            if (!alertShown && timeElapsed >= 2) {
                double twoHourPrediction = currentLevel - (dischargeRate * 2);
                if (twoHourPrediction < 20) {
                    String message = String.format(
                        "Battery will reach 20%% in 2 hours (predicted: %.1f%%)",
                        twoHourPrediction
                    );
                    AlertSystem.sendAlert(message);
                    PowerManagement.enablePowerSavingMode();
                    alertShown = true;
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error in future state update: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BatteryVisualizer visualizer = new BatteryVisualizer();
            visualizer.setVisible(true);
        });
    }
}
