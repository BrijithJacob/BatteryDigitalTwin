import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Custom panel for displaying battery state with visualization
 */
public class BatteryPanel extends JPanel {
    
    private String title;
    private Color fillColor;
    private BatteryCanvas canvas;
    private JLabel chargeLabel;
    private JLabel voltageLabel;
    private JLabel uncertaintyLabel;
    
    private double chargeLevel = 0;
    private double voltage = 0;
    private double uncertainty = 0;
    
    public BatteryPanel(String title, Color fillColor) {
        this.title = title;
        this.fillColor = fillColor;
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(titleLabel);
        
        add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Canvas
        canvas = new BatteryCanvas(fillColor);
        canvas.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(canvas);
        
        add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Labels
        JPanel labelsPanel = new JPanel();
        labelsPanel.setLayout(new BoxLayout(labelsPanel, BoxLayout.Y_AXIS));
        
        chargeLabel = new JLabel("Charge: --");
        chargeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        voltageLabel = new JLabel("Voltage: --");
        voltageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        uncertaintyLabel = new JLabel("Uncertainty: ±--");
        uncertaintyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        labelsPanel.add(chargeLabel);
        labelsPanel.add(voltageLabel);
        
        if (title.contains("Prediction")) {
            labelsPanel.add(uncertaintyLabel);
        }
        
        add(labelsPanel);
    }
    
    public void updateDisplay(double chargeLevel, double voltage, double uncertainty) {
        this.chargeLevel = chargeLevel;
        this.voltage = voltage;
        this.uncertainty = uncertainty;
        
        canvas.setChargeLevel(chargeLevel);
        canvas.setUncertainty(uncertainty);
        canvas.repaint();
        
        chargeLabel.setText(String.format("Charge: %.1f%%", chargeLevel));
        voltageLabel.setText(String.format("Voltage: %.2fV", voltage));
        
        if (title.contains("Prediction")) {
            uncertaintyLabel.setText(String.format("Uncertainty: ±%.1f%%", uncertainty));
        }
    }
    
    /**
     * Custom canvas for drawing battery visualization
     */
    private class BatteryCanvas extends JPanel {
        private Color fillColor;
        private double chargeLevel = 100;
        private double uncertainty = 0;
        
        public BatteryCanvas(Color fillColor) {
            this.fillColor = fillColor;
            setPreferredSize(new Dimension(200, 120));
            setBackground(Color.WHITE);
        }
        
        public void setChargeLevel(double chargeLevel) {
            this.chargeLevel = chargeLevel;
        }
        
        public void setUncertainty(double uncertainty) {
            this.uncertainty = uncertainty;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                RenderingHints.VALUE_ANTIALIAS_ON);
            
            int x = 10;
            int y = 20;
            int width = 180;
            int height = 60;
            
            // Draw uncertainty region (for prediction panel)
            if (uncertainty > 0) {
                double lowerBound = Math.max(0, chargeLevel - uncertainty);
                double upperBound = Math.min(100, chargeLevel + uncertainty);
                
                int lowerWidth = (int)(width * lowerBound / 100);
                int upperWidth = (int)(width * upperBound / 100);
                
                g2d.setColor(new Color(173, 216, 230, 100)); // Light blue with transparency
                g2d.fillRect(x + lowerWidth, y, upperWidth - lowerWidth, height);
            }
            
            // Draw battery fill
            int fillWidth = (int)(width * chargeLevel / 100);
            g2d.setColor(fillColor);
            g2d.fillRect(x, y, fillWidth, height);
            
            // Draw battery outline
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(x, y, width, height);
            
            // Draw battery terminal
            g2d.fillRect(x + width, y + 15, 5, 30);
        }
    }
}
