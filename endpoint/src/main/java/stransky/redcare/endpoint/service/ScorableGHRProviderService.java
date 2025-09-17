package stransky.redcare.endpoint.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stransky.redcare.endpoint.config.GitHubSearchConfiguration;
import stransky.redcare.interfaces.job.JobTask;
import stransky.redcare.interfaces.repository.ScorableGHR;
import stransky.redcare.interfaces.repository.ScorableGHRRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Provides the requested ScorableGHRs or commissions their calculation via RabbitMQ.
 */
@Service
public class ScorableGHRProviderService {

    private static final Logger logger = LoggerFactory.getLogger(ScorableGHRProviderService.class);

    private final ScorableGHRRepository scorableGHRRepository;
    private final RabbitTemplate rabbitTemplate;

    private final ConcurrentMap<String, Instant> oldestRequestedDateForLanguageMap;
    private final Duration durationAddedToDateToFetchFrom;
    private final Duration durationInWhichForSureRepositoriesAreCreated;

    public ScorableGHRProviderService(ScorableGHRRepository scorableGHRRepository, GitHubSearchConfiguration gitHubSearchServiceConfiguration, RabbitTemplate rabbitTemplate) {
        this.scorableGHRRepository = scorableGHRRepository;
        this.rabbitTemplate = rabbitTemplate;

        this.oldestRequestedDateForLanguageMap = new ConcurrentHashMap<>();
        this.durationAddedToDateToFetchFrom = gitHubSearchServiceConfiguration.getChunkSize();
        this.durationInWhichForSureRepositoriesAreCreated = gitHubSearchServiceConfiguration.getDurationInWhichForSureRepositoriesAreCreated();
    }

    public Instant getOldestAvailableDate(String language) {
        Instant oldest = scorableGHRRepository.oldest(language);
        return oldest == null ? Instant.now() : oldest;
    }

    /**
     * Provides the requested ScorableGHRs from the database
     *
     * @param language             of requested repositories
     * @param earliestCreationDate earliest creation date of requested repositories
     * @return scored GitHub Repositories for a given language after the given earliest creation date
     */
    @Transactional
    public List<ScorableGHR> getScorableGHR(String language, Instant earliestCreationDate) {
        return Arrays.stream(scorableGHRRepository.find(language, earliestCreationDate)).map(r -> (ScorableGHR) r).toList();
    }

    /**
     * Commissions via RabbitMQ fetching the information needed to score a GitHub repository.
     *
     * @param language             of requested repositories, not case-sensitive, as GitHub's api is not case-sensitive
     * @param earliestCreationDate earliest creation date of requested repositories
     */
    public void commission(String language, Instant earliestCreationDate) {
        logger.info("Commission for {} from {}", language, earliestCreationDate);
        if (isAlreadyCommissioned(language, earliestCreationDate)) {
            return;
        }
        final Instant oldestAvailableDate = getOldestAvailableDate(language);
        Instant dateToFetch = max(earliestCreationDate, oldestAvailableDate).minus(durationAddedToDateToFetchFrom);
        for (; dateToFetch.isAfter(earliestCreationDate); dateToFetch = dateToFetch.minus(durationAddedToDateToFetchFrom)) {
            commission(language, dateToFetch, durationAddedToDateToFetchFrom);
        }
        commission(language, earliestCreationDate.minus(durationInWhichForSureRepositoriesAreCreated), Duration.between(earliestCreationDate.minus(durationInWhichForSureRepositoriesAreCreated), dateToFetch.plus(durationAddedToDateToFetchFrom)));
    }

    /**
     * Has the information, needed to score a GitHub repository, already been requested, but not necessarily available.
     *
     * @param language             of requested repositories
     * @param earliestCreationDate earliest creation date of requested repositories
     * @return is already in requested time range
     */
    boolean isAlreadyCommissioned(String language, Instant earliestCreationDate) {
        final String normalizedLanguage = language.toLowerCase().trim();
        oldestRequestedDateForLanguageMap.putIfAbsent(normalizedLanguage, Instant.MAX);
        if (!oldestRequestedDateForLanguageMap.get(normalizedLanguage).isAfter(earliestCreationDate)) {
            return true;
        }
        oldestRequestedDateForLanguageMap.compute(normalizedLanguage, (k, v) -> min(earliestCreationDate, v));
        return false;
    }

    Instant min(Instant a, Instant b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isBefore(b) ? a : b;
    }

    Instant max(Instant a, Instant b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isAfter(b) ? a : b;
    }

    /**
     * Commission the information for scoring a GitHub repository via GitHub in a portion which is suitable for a single api call
     *
     * @param language                    of the GitHub repositories
     * @param earliestCreationDateToFetch the oldest GitHub repository to fetch
     * @param chunkSize                   timespan for which data to fetch
     */
    void commission(String language, Instant earliestCreationDateToFetch, Duration chunkSize) {
        final JobTask pleaseDo = new JobTask(language, earliestCreationDateToFetch, chunkSize);
        rabbitTemplate.convertAndSend(pleaseDo);
        logger.info("Commission for {}", pleaseDo);
    }

}
