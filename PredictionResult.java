/**
 * Represents the result of a battery prediction
 */
public class PredictionResult {
    private final Double predictedCharge;
    private final Double uncertainty;
    
    public PredictionResult(Double predictedCharge, Double uncertainty) {
        this.predictedCharge = predictedCharge;
        this.uncertainty = uncertainty;
    }
    
    public Double getPredictedCharge() {
        return predictedCharge;
    }
    
    public Double getUncertainty() {
        return uncertainty;
    }
    
    public boolean isValid() {
        return predictedCharge != null && uncertainty != null;
    }
    
    public double getLowerBound() {
        if (!isValid()) return 0;
        return Math.max(0, predictedCharge - uncertainty);
    }
    
    public double getUpperBound() {
        if (!isValid()) return 100;
        return Math.min(100, predictedCharge + uncertainty);
    }
    
    public double getConfidence() {
        if (!isValid()) return 0;
        return Math.max(0, 1 - (uncertainty / 100));
    }
    
    @Override
    public String toString() {
        if (!isValid()) {
            return "PredictionResult[invalid]";
        }
        return String.format("PredictionResult[predicted=%.1f%%, uncertainty=±%.1f%%, confidence=%.1f%%]",
                predictedCharge, uncertainty, getConfidence() * 100);
    }
}
