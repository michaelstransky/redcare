package stransky.redcare.endpoint.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Component
@ConfigurationProperties(prefix = "stransky.redcare.search")
public class GitHubSearchConfiguration {
    private int chunkSize = 1;
    private ChronoUnit chunkSizeUnit = ChronoUnit.DAYS;

    private int durationInWhichForSureRepositoriesAreCreated = 1;
    private ChronoUnit durationInWhichForSureRepositoriesAreCreatedUnit = ChronoUnit.HOURS;

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public void setChunkSizeUnit(ChronoUnit chunkSizeUnit) {
        this.chunkSizeUnit = chunkSizeUnit;
    }

    public void setDurationInWhichForSureRepositoriesAreCreated(int durationInWhichForSureRepositoriesAreCreated) {
        this.durationInWhichForSureRepositoriesAreCreated = durationInWhichForSureRepositoriesAreCreated;
    }

    public void setDurationInWhichForSureRepositoriesAreCreatedUnit(ChronoUnit durationInWhichForSureRepositoriesAreCreatedUnit) {
        this.durationInWhichForSureRepositoriesAreCreatedUnit = durationInWhichForSureRepositoriesAreCreatedUnit;
    }

    public Duration getChunkSize() {
        return Duration.of(chunkSize, chunkSizeUnit);
    }

    public Duration getDurationInWhichForSureRepositoriesAreCreated() {
        return Duration.of(durationInWhichForSureRepositoriesAreCreated, durationInWhichForSureRepositoriesAreCreatedUnit);
    }
}
