package stransky.redcare.interfaces.job;

import java.time.Duration;
import java.time.Instant;

public record JobTask(String language, Instant dateToFetchFrom, Duration durationAddedToDateToFetchFrom) {
}
