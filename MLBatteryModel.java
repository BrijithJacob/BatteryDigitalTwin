import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Machine Learning Battery Prediction Model
 */
public class MLBatteryModel implements IBatteryPredictionModel {
    private INeuralNetwork neuralNetwork;
    private StandardScaler scaler;
    private final String dbPath;
    private boolean trainingComplete;
    private Map<Integer, ValidationError> predictionErrors;
    
    public MLBatteryModel(String dbPath) {
        this.dbPath = dbPath;
        this.trainingComplete = false;
        this.predictionErrors = new HashMap<>();
        this.scaler = new StandardScaler();
    }
    
    @Override
    public boolean trainModel(int timeWindowHours, int maxHours, int toleranceMinutes) {
        try {
            BatteryDatabase db = new BatteryDatabase(dbPath);
            List<BatteryDataRecord> data = db.getRecentRecords(10000);
            db.close();
            
            if (data.size() < 100) {
                System.out.println("Insufficient data for training");
                return false;
            }
            
            // Prepare training data
            List<double[]> xAll = new ArrayList<>();
            List<Double> yAll = new ArrayList<>();
            List<Integer> horizons = new ArrayList<>();
            
            for (int idx = 0; idx < data.size() - maxHours; idx++) {
                BatteryDataRecord entry = data.get(idx);
                double[] features = createFeatures(data, idx, timeWindowHours);
                
                if (features == null) continue;
                
                LocalDateTime currentTime = entry.getTimestamp();
                double currentCharge = entry.getChargePercentage();
                
                // For each prediction horizon
                for (int hours = 1; hours <= maxHours; hours++) {
                    LocalDateTime targetTime = currentTime.plusHours(hours);
                    
                    // Find actual future value
                    BatteryDataRecord futureEntry = findClosestRecord(
                        data, targetTime, toleranceMinutes);
                    
                    if (futureEntry != null && 
                        futureEntry.getChargePercentage() < currentCharge) {
                        
                        double[] extendedFeatures = Arrays.copyOf(features, features.length + 1);
                        extendedFeatures[features.length] = hours;
                        
                        xAll.add(extendedFeatures);
                        yAll.add(futureEntry.getChargePercentage());
                        horizons.add(hours);
                    }
                }
            }
            
            if (xAll.size() < 50) {
                System.out.println("Insufficient valid training samples");
                return false;
            }
            
            // Convert to arrays
            double[][] X = xAll.toArray(new double[0][]);
            double[] y = yAll.stream().mapToDouble(Double::doubleValue).toArray();
            
            // Split data chronologically
            int splitIdx = (int)(X.length * 0.8);
            double[][] xTrain = Arrays.copyOfRange(X, 0, splitIdx);
            double[][] xTest = Arrays.copyOfRange(X, splitIdx, X.length);
            double[] yTrain = Arrays.copyOfRange(y, 0, splitIdx);
            double[] yTest = Arrays.copyOfRange(y, splitIdx, y.length);
            List<Integer> horizonsTest = horizons.subList(splitIdx, horizons.size());
            
            // Scale features
            scaler = new StandardScaler();
            double[][] xTrainScaled = scaler.fitTransform(xTrain);
            double[][] xTestScaled = scaler.transform(xTest);
            
            // Split training data for validation
            DataSplit split = splitTrainValidation(xTrainScaled, yTrain, 0.2);
            
            // Initialize and train neural network
            System.out.println("Training Neural Network...");
            neuralNetwork = new SimpleNeuralNetwork(xTrainScaled[0].length);
            
            TrainingHistory history = neuralNetwork.train(
                split.xTrain, split.yTrain, 
                split.xVal, split.yVal, 
                100
            );
            
            // Make predictions
            double[] predictions = neuralNetwork.predict(xTestScaled);
            
            // Calculate errors by horizon
            calculatePredictionErrors(predictions, yTest, horizonsTest, maxHours);
            
            trainingComplete = true;
            System.out.println("Training complete using Simple Neural Network");
            printValidationResults();
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Training error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private void calculatePredictionErrors(double[] predictions, double[] yTest, 
                                          List<Integer> horizonsTest, int maxHours) {
        for (int hour = 1; hour <= maxHours; hour++) {
            List<Double> errors = new ArrayList<>();
            
            for (int i = 0; i < horizonsTest.size(); i++) {
                if (horizonsTest.get(i) == hour) {
                    errors.add(Math.abs(predictions[i] - yTest[i]));
                }
            }
            
            if (!errors.isEmpty()) {
                double mae = errors.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                double rmse = Math.sqrt(
                    errors.stream()
                        .mapToDouble(e -> e * e)
                        .average()
                        .orElse(0)
                );
                
                predictionErrors.put(hour, new ValidationError(mae, rmse, errors.size()));
            }
        }
    }
    
    private double[] createFeatures(List<BatteryDataRecord> data, int idx, int timeWindowHours) {
        try {
            BatteryDataRecord currentEntry = data.get(idx);
            LocalDateTime currentTime = currentEntry.getTimestamp();
            LocalDateTime windowStart = currentTime.minusHours(timeWindowHours);
            
            List<BatteryDataRecord> windowData = new ArrayList<>();
            for (int i = idx; i < data.size(); i++) {
                BatteryDataRecord record = data.get(i);
                LocalDateTime recordTime = record.getTimestamp();
                
                if (recordTime.isAfter(windowStart) && recordTime.isBefore(currentTime)) {
                    windowData.add(record);
                }
            }
            
            if (windowData.size() < 2) return null;
            
            // Calculate statistics
            double[] chargeLevels = windowData.stream()
                .mapToDouble(BatteryDataRecord::getChargePercentage)
                .toArray();
            double[] temps = windowData.stream()
                .mapToDouble(BatteryDataRecord::getTemperature)
                .toArray();
            double[] voltages = windowData.stream()
                .mapToDouble(BatteryDataRecord::getVoltage)
                .toArray();
            
            if (chargeLevels.length == 0 || temps.length == 0 || voltages.length == 0) {
                return null;
            }
            
            long chargingCount = windowData.stream().filter(BatteryDataRecord::isCharging).count();
            double chargingRatio = (double) chargingCount / windowData.size();
            
            return new double[] {
                currentEntry.getChargePercentage(),
                mean(chargeLevels),
                stdDev(chargeLevels),
                mean(temps),
                mean(voltages),
                chargingRatio,
                currentEntry.getTemperature(),
                currentEntry.getVoltage()
            };
            
        } catch (Exception e) {
            System.err.println("Error creating features: " + e.getMessage());
            return null;
        }
    }
    
    private BatteryDataRecord findClosestRecord(List<BatteryDataRecord> data, 
                                                LocalDateTime targetTime, 
                                                int toleranceMinutes) {
        return data.stream()
            .filter(record -> {
                long minutes = Math.abs(ChronoUnit.MINUTES.between(
                    record.getTimestamp(), targetTime));
                return minutes <= toleranceMinutes;
            })
            .findFirst()
            .orElse(null);
    }
    
    @Override
    public PredictionResult predictFutureState(BatteryState currentState, double hoursAhead) {
        if (!trainingComplete || neuralNetwork == null || !scaler.isFitted()) {
            return new PredictionResult(null, null);
        }
        
        try {
            // Create mock data for feature extraction
            // In a real implementation, this would use actual historical data
            double[] features = new double[] {
                currentState.getChargeLevel(),
                currentState.getChargeLevel(),
                0.0,
                currentState.getTemperature(),
                currentState.getVoltage(),
                0.0,
                currentState.getTemperature(),
                currentState.getVoltage(),
                hoursAhead
            };
            
            double[][] featuresScaled = scaler.transform(new double[][] {features});
            double[] prediction = neuralNetwork.predict(featuresScaled);
            
            double predictedCharge = Math.max(0, Math.min(100, prediction[0]));
            Double uncertainty = getUncertainty((int) hoursAhead);
            
            if (uncertainty == null) {
                uncertainty = 5.0; // Default uncertainty
            }
            
            return new PredictionResult(predictedCharge, uncertainty);
            
        } catch (Exception e) {
            System.err.println("Prediction error: " + e.getMessage());
            return new PredictionResult(null, null);
        }
    }
    
    @Override
    public Double getUncertainty(int hoursAhead) {
        ValidationError error = predictionErrors.get(hoursAhead);
        return error != null ? error.getMae() : null;
    }
    
    @Override
    public boolean isTrainingComplete() {
        return trainingComplete;
    }
    
    @Override
    public void printValidationResults() {
        if (predictionErrors.isEmpty()) {
            System.out.println("No validation results available");
            return;
        }
        
        System.out.println("\nNeural Network Validation Results:");
        System.out.println("-".repeat(50));
        System.out.printf("%-12s %-10s %-10s %-8s%n", 
            "Hours Ahead", "MAE (%)", "RMSE (%)", "Samples");
        System.out.println("-".repeat(50));
        
        List<Integer> sortedHours = new ArrayList<>(predictionErrors.keySet());
        Collections.sort(sortedHours);
        
        for (int hour : sortedHours) {
            ValidationError error = predictionErrors.get(hour);
            System.out.printf("%-12d %.2f%%      %.2f%%      %d%n",
                hour, error.getMae(), error.getRmse(), error.getSamples());
        }
        System.out.println("-".repeat(50));
    }
    
    // Utility methods
    private double mean(double[] array) {
        return Arrays.stream(array).average().orElse(0.0);
    }
    
    private double stdDev(double[] array) {
        if (array.length <= 1) return 0.0;
        double avg = mean(array);
        double variance = Arrays.stream(array)
            .map(x -> Math.pow(x - avg, 2))
            .average()
            .orElse(0.0);
        return Math.sqrt(variance);
    }
    
    private DataSplit splitTrainValidation(double[][] X, double[] y, double testRatio) {
        int splitIdx = (int)(X.length * (1 - testRatio));
        
        return new DataSplit(
            Arrays.copyOfRange(X, 0, splitIdx),
            Arrays.copyOfRange(y, 0, splitIdx),
            Arrays.copyOfRange(X, splitIdx, X.length),
            Arrays.copyOfRange(y, splitIdx, y.length)
        );
    }
    
    private static class DataSplit {
        final double[][] xTrain;
        final double[] yTrain;
        final double[][] xVal;
        final double[] yVal;
        
        DataSplit(double[][] xTrain, double[] yTrain, double[][] xVal, double[] yVal) {
            this.xTrain = xTrain;
            this.yTrain = yTrain;
            this.xVal = xVal;
            this.yVal = yVal;
        }
    }
}
