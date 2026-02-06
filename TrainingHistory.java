import java.util.ArrayList;
import java.util.List;

/**
 * Stores training history metrics
 */
public class TrainingHistory {
    private final List<Double> trainingLoss;
    private final List<Double> validationLoss;
    
    public TrainingHistory() {
        this.trainingLoss = new ArrayList<>();
        this.validationLoss = new ArrayList<>();
    }
    
    public void addEpoch(double trainLoss, double valLoss) {
        trainingLoss.add(trainLoss);
        validationLoss.add(valLoss);
    }
    
    public List<Double> getTrainingLoss() {
        return new ArrayList<>(trainingLoss);
    }
    
    public List<Double> getValidationLoss() {
        return new ArrayList<>(validationLoss);
    }
    
    public int getEpochCount() {
        return trainingLoss.size();
    }
    
    public double getFinalTrainingLoss() {
        return trainingLoss.isEmpty() ? 0 : trainingLoss.get(trainingLoss.size() - 1);
    }
    
    public double getFinalValidationLoss() {
        return validationLoss.isEmpty() ? 0 : validationLoss.get(validationLoss.size() - 1);
    }
    
    @Override
    public String toString() {
        return String.format("TrainingHistory[epochs=%d, finalTrainLoss=%.4f, finalValLoss=%.4f]",
                getEpochCount(), getFinalTrainingLoss(), getFinalValidationLoss());
    }
}
