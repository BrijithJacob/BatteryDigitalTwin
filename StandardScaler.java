/**
 * Standard scaler for normalizing features
 * Implements z-score normalization: (x - mean) / std
 */
public class StandardScaler {
    private double[] mean;
    private double[] std;
    private boolean fitted = false;
    
    /**
     * Fit the scaler to training data
     */
    public void fit(double[][] X) {
        int numFeatures = X[0].length;
        int numSamples = X.length;
        
        mean = new double[numFeatures];
        std = new double[numFeatures];
        
        // Calculate means
        for (int j = 0; j < numFeatures; j++) {
            double sum = 0;
            for (int i = 0; i < numSamples; i++) {
                sum += X[i][j];
            }
            mean[j] = sum / numSamples;
        }
        
        // Calculate standard deviations
        for (int j = 0; j < numFeatures; j++) {
            double sumSquaredDiff = 0;
            for (int i = 0; i < numSamples; i++) {
                double diff = X[i][j] - mean[j];
                sumSquaredDiff += diff * diff;
            }
            std[j] = Math.sqrt(sumSquaredDiff / numSamples);
            if (std[j] == 0) std[j] = 1; // Avoid division by zero
        }
        
        fitted = true;
    }
    
    /**
     * Fit and transform in one step
     */
    public double[][] fitTransform(double[][] X) {
        fit(X);
        return transform(X);
    }
    
    /**
     * Transform data using fitted parameters
     */
    public double[][] transform(double[][] X) {
        if (!fitted) {
            throw new IllegalStateException("Scaler must be fitted before transform");
        }
        
        int numSamples = X.length;
        int numFeatures = X[0].length;
        double[][] transformed = new double[numSamples][numFeatures];
        
        for (int i = 0; i < numSamples; i++) {
            for (int j = 0; j < numFeatures; j++) {
                transformed[i][j] = (X[i][j] - mean[j]) / std[j];
            }
        }
        
        return transformed;
    }
    
    public boolean isFitted() {
        return fitted;
    }
}
