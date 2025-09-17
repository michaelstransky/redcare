package stransky.redcare.endpoint.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "stransky.redcare.weights")
public class RepositoryScorerWeightsConfiguration {
    private double starFactor;
    private double forkFactor;
    private double ageFactor;

    public double getStarFactor() {
        return starFactor;
    }

    public void setStarFactor(double starFactor) {
        this.starFactor = starFactor;
    }

    public double getForkFactor() {
        return forkFactor;
    }

    public void setForkFactor(double forkFactor) {
        this.forkFactor = forkFactor;
    }

    public double getAgeFactor() {
        return ageFactor;
    }

    public void setAgeFactor(double ageFactor) {
        this.ageFactor = ageFactor;
    }
}
