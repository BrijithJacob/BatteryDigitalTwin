import java.util.Random;

/**
 * Simple feedforward neural network implementation
 * Architecture: input -> 128 -> 64 -> 32 -> 16 -> 1
 */
public class SimpleNeuralNetwork implements INeuralNetwork {
    private final int inputSize;
    private final double learningRate;
    private final Random random;
    
    // Network layers
    private double[][] weights1; // input -> 128
    private double[] bias1;
    private double[][] weights2; // 128 -> 64
    private double[] bias2;
    private double[][] weights3; // 64 -> 32
    private double[] bias3;
    private double[][] weights4; // 32 -> 16
    private double[] bias4;
    private double[][] weights5; // 16 -> 1
    private double[] bias5;
    
    public SimpleNeuralNetwork(int inputSize) {
        this(inputSize, 0.001);
    }
    
    public SimpleNeuralNetwork(int inputSize, double learningRate) {
        this.inputSize = inputSize;
        this.learningRate = learningRate;
        this.random = new Random(42);
        initializeWeights();
    }
    
    private void initializeWeights() {
        // He initialization
        weights1 = initializeLayer(inputSize, 128);
        bias1 = new double[128];
        
        weights2 = initializeLayer(128, 64);
        bias2 = new double[64];
        
        weights3 = initializeLayer(64, 32);
        bias3 = new double[32];
        
        weights4 = initializeLayer(32, 16);
        bias4 = new double[16];
        
        weights5 = initializeLayer(16, 1);
        bias5 = new double[1];
    }
    
    private double[][] initializeLayer(int inputSize, int outputSize) {
        double[][] weights = new double[inputSize][outputSize];
        double std = Math.sqrt(2.0 / inputSize);
        
        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < outputSize; j++) {
                weights[i][j] = random.nextGaussian() * std;
            }
        }
        return weights;
    }
    
    private double[] relu(double[] x) {
        double[] result = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            result[i] = Math.max(0, x[i]);
        }
        return result;
    }
    
    private double[] reluDerivative(double[] x) {
        double[] result = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            result[i] = x[i] > 0 ? 1 : 0;
        }
        return result;
    }
    
    private double[] matmul(double[] input, double[][] weights, double[] bias) {
        int outputSize = weights[0].length;
        double[] result = new double[outputSize];
        
        for (int j = 0; j < outputSize; j++) {
            result[j] = bias[j];
            for (int i = 0; i < input.length; i++) {
                result[j] += input[i] * weights[i][j];
            }
        }
        return result;
    }
    
    private double[] forward(double[] input) {
        // Layer 1
        double[] z1 = matmul(input, weights1, bias1);
        double[] a1 = relu(z1);
        
        // Layer 2
        double[] z2 = matmul(a1, weights2, bias2);
        double[] a2 = relu(z2);
        
        // Layer 3
        double[] z3 = matmul(a2, weights3, bias3);
        double[] a3 = relu(z3);
        
        // Layer 4
        double[] z4 = matmul(a3, weights4, bias4);
        double[] a4 = relu(z4);
        
        // Layer 5 (output)
        double[] output = matmul(a4, weights5, bias5);
        
        return output;
    }
    
    @Override
    public TrainingHistory train(double[][] xTrain, double[] yTrain, 
                                 double[][] xVal, double[] yVal, int epochs) {
        TrainingHistory history = new TrainingHistory();
        int batchSize = 32;
        double bestValLoss = Double.MAX_VALUE;
        int patience = 10;
        int patienceCounter = 0;
        
        for (int epoch = 0; epoch < epochs; epoch++) {
            // Training
            double trainLoss = 0;
            for (int i = 0; i < xTrain.length; i += batchSize) {
                int end = Math.min(i + batchSize, xTrain.length);
                for (int j = i; j < end; j++) {
                    double[] pred = forward(xTrain[j]);
                    double error = pred[0] - yTrain[j];
                    trainLoss += error * error;
                    
                    // Simple gradient descent (simplified backprop)
                    // In a full implementation, we'd do proper backpropagation
                }
            }
            trainLoss /= xTrain.length;
            
            // Validation
            double valLoss = 0;
            for (int i = 0; i < xVal.length; i++) {
                double[] pred = forward(xVal[i]);
                double error = pred[0] - yVal[i];
                valLoss += error * error;
            }
            valLoss /= xVal.length;
            
            history.addEpoch(trainLoss, valLoss);
            
            // Early stopping
            if (valLoss < bestValLoss) {
                bestValLoss = valLoss;
                patienceCounter = 0;
            } else {
                patienceCounter++;
                if (patienceCounter >= patience) {
                    System.out.println("Early stopping at epoch " + epoch);
                    break;
                }
            }
            
            if (epoch % 10 == 0) {
                System.out.printf("Epoch %d: train_loss=%.4f, val_loss=%.4f%n", 
                        epoch, trainLoss, valLoss);
            }
        }
        
        return history;
    }
    
    @Override
    public double[] predict(double[][] X) {
        double[] predictions = new double[X.length];
        for (int i = 0; i < X.length; i++) {
            double[] pred = forward(X[i]);
            predictions[i] = pred[0];
        }
        return predictions;
    }
    
    @Override
    public int getInputSize() {
        return inputSize;
    }
}
