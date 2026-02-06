/**
 * Interface for Neural Network implementations
 */
public interface INeuralNetwork {
    
    /**
     * Train the neural network
     * @param xTrain Training features
     * @param yTrain Training labels
     * @param xVal Validation features
     * @param yVal Validation labels
     * @param epochs Number of training epochs
     * @return Training history
     */
    TrainingHistory train(double[][] xTrain, double[] yTrain, 
                         double[][] xVal, double[] yVal, int epochs);
    
    /**
     * Make predictions
     * @param X Input features
     * @return Predictions
     */
    double[] predict(double[][] X);
    
    /**
     * Get the input size of the network
     * @return Input size
     */
    int getInputSize();
}
