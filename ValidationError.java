/**
 * Stores validation error metrics for a prediction horizon
 */
public class ValidationError {
    private final double mae;  // Mean Absolute Error
    private final double rmse; // Root Mean Squared Error
    private final int samples; // Number of samples
    
    public ValidationError(double mae, double rmse, int samples) {
        this.mae = mae;
        this.rmse = rmse;
        this.samples = samples;
    }
    
    public double getMae() {
        return mae;
    }
    
    public double getRmse() {
        return rmse;
    }
    
    public int getSamples() {
        return samples;
    }
    
    @Override
    public String toString() {
        return String.format("ValidationError[MAE=%.2f%%, RMSE=%.2f%%, samples=%d]", 
                mae, rmse, samples);
    }
}
