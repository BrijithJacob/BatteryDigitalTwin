/**
 * Interface for battery prediction models
 */
public interface IBatteryPredictionModel {
    
    /**
     * Train the model with historical data
     * @param timeWindowHours Time window for feature creation
     * @param maxHours Maximum prediction horizon
     * @param toleranceMinutes Tolerance for matching predictions
     * @return true if training successful
     */
    boolean trainModel(int timeWindowHours, int maxHours, int toleranceMinutes);
    
    /**
     * Predict future battery state
     * @param currentState Current battery state
     * @param hoursAhead Hours to predict ahead
     * @return Prediction result with value and uncertainty
     */
    PredictionResult predictFutureState(BatteryState currentState, double hoursAhead);
    
    /**
     * Get prediction uncertainty for a given horizon
     * @param hoursAhead Prediction horizon in hours
     * @return Uncertainty value (MAE)
     */
    Double getUncertainty(int hoursAhead);
    
    /**
     * Check if model training is complete
     * @return true if training complete
     */
    boolean isTrainingComplete();
    
    /**
     * Print validation results
     */
    void printValidationResults();
}
