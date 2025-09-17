package stransky.redcare.endpoint.service;

import org.springframework.stereotype.Service;
import stransky.redcare.endpoint.config.RepositoryScorerWeightsConfiguration;
import stransky.redcare.endpoint.model.ScoredGHR;
import stransky.redcare.interfaces.repository.ScorableGHR;

import java.time.Instant;
import java.time.temporal.ChronoUnit;


/**
 * Calculates a linear score based on stars forks and age.
 */
@Service
public class RepositoryScorerService {
    private final double starFactor;
    private final double forkFactor;
    private final double ageFactor;

    public RepositoryScorerService(RepositoryScorerWeightsConfiguration repositoryScorerWeightsConfiguration) {
        this.starFactor = repositoryScorerWeightsConfiguration.getStarFactor();
        this.forkFactor = repositoryScorerWeightsConfiguration.getForkFactor();
        this.ageFactor = repositoryScorerWeightsConfiguration.getAgeFactor();
    }

    /**
     * Calculates a ScoredGHR for a given ScorableGHR.
     *
     * @param scorableGHR contains all the information needed for scoring
     * @return contains the name of a GitHub Repository and its assigned score
     */
    public ScoredGHR score(ScorableGHR scorableGHR) {
        return new ScoredGHR(scorableGHR.getFullName(), score(scorableGHR.getStargazers(), scorableGHR.getForks(), scorableGHR.getUpdatedAt()));
    }

    private double score(int stars, int forks, Instant lastUpdated) {
        return starFactor * stars + forkFactor + forks + ageFactor * ChronoUnit.DAYS.between(lastUpdated, Instant.now());
    }

}
