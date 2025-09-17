package stransky.redcare.endpoint.service;

import org.junit.jupiter.api.Test;
import stransky.redcare.endpoint.config.RepositoryScorerWeightsConfiguration;
import stransky.redcare.interfaces.repository.ScorableGHR;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RepositoryScorerServiceTest {

    @Test
    void score() {
        double starFactor = 3;
        double forkFactor = 5;
        double ageFactor = -0.8;
        RepositoryScorerWeightsConfiguration repositoryScorerWeightsConfiguration = mock(RepositoryScorerWeightsConfiguration.class);
        when(repositoryScorerWeightsConfiguration.getStarFactor()).thenReturn(starFactor);
        when(repositoryScorerWeightsConfiguration.getAgeFactor()).thenReturn(forkFactor);
        when(repositoryScorerWeightsConfiguration.getAgeFactor()).thenReturn(ageFactor);
        RepositoryScorerService repositoryScorerService = new RepositoryScorerService(repositoryScorerWeightsConfiguration);
        Instant i = Instant.now();

        double score = repositoryScorerService.score(new ScorableGHR("name", "Java", i, 1, 1, i.minus(5, ChronoUnit.DAYS))).score();
        assertEquals(0, score, 1e-5);
    }
}