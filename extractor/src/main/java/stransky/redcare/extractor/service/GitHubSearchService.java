package stransky.redcare.extractor.service;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedSearchIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stransky.redcare.extractor.config.GitHubConfiguration;
import stransky.redcare.interfaces.repository.ScorableGHR;
import stransky.redcare.interfaces.repository.ScorableGHRRepository;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Service for searching for GitHub Repositories for a given programming language in a given timeframe
 * and storing the information needed to score them.
 */
@Service
public class GitHubSearchService {

    private static final Logger logger = LoggerFactory.getLogger(GitHubSearchService.class);

    private final ScorableGHRRepository scorableGHRRepository;
    private final GitHub gitHub;

    public GitHubSearchService(GitHubConfiguration gitHubConfiguration, ScorableGHRRepository scorableGHRRepository) throws IOException {
        this.gitHub = gitHubConfiguration.createGitHubConnection();
        this.scorableGHRRepository = scorableGHRRepository;
    }

    /**
     * Persists the ScorableGHRs
     *
     * @param scorableGitHubRepositories to persist
     */
    @Transactional
    public void save(Collection<ScorableGHR> scorableGitHubRepositories) {
        scorableGHRRepository.saveAllAndFlush(scorableGitHubRepositories);
    }

    @Transactional
    public void logTimeRangeAvailable(String language) {
        logger.info("{} -> {} - {}", language, scorableGHRRepository.oldest(language), scorableGHRRepository.newest(language));
    }

    /**
     * Fetch information needed for scoring GitHub repositories.
     * If items cannot be
     *
     * @param language             of requested repositories
     * @param earliestCreationDate earliest creation date of requested repositories
     * @param chunkSize            timespan for which data to fetch
     * @return ScorableGHR the GitHub repositories with the information needed for scoring them.
     */
    public Collection<ScorableGHR> searchRepositories(String language, Instant earliestCreationDate, Duration chunkSize) {
        PagedSearchIterable<GHRepository> ghRepositories = gitHub.searchRepositories()
                .language(language)
                .created(String.format("%s..%s", earliestCreationDate, earliestCreationDate.plus(chunkSize)))
                .list();

        logger.info("iterate {} {} - {} # {}", language, earliestCreationDate, earliestCreationDate.plus(chunkSize), ghRepositories.getTotalCount());

        List<ScorableGHR> scorableRepositories = new ArrayList<>(ghRepositories.getTotalCount());
        for (GHRepository r : ghRepositories) {
            try {
                scorableRepositories.add(new ScorableGHR(r.getFullName(), r.getLanguage(), r.getCreatedAt().toInstant(), r.getStargazersCount(), r.getForksCount(), r.getUpdatedAt().toInstant()));
            } catch (IOException e) {
                logger.warn("{} contains unparsable date", r, e);
            }
        }
        return scorableRepositories;
    }

}
