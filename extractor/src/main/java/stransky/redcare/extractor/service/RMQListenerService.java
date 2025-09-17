package stransky.redcare.extractor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import stransky.redcare.interfaces.job.JobTask;
import stransky.redcare.interfaces.repository.ScorableGHR;

import java.util.Collection;

/**
 * Receives via RabbitMQ fetch jobs,
 * delegates them and
 * caches the infos about GitHub repositories .
 */
@Service
public class RMQListenerService {

    private static final Logger logger = LoggerFactory.getLogger(RMQListenerService.class);

    private final GitHubSearchService gitHubSearchService;

    public RMQListenerService(GitHubSearchService gitHubSearchService) {
        this.gitHubSearchService = gitHubSearchService;
    }

    @RabbitListener(queues = "${stransky.redcare.queue.name}")
    public void consume(JobTask jobTask) {
        logger.info("search {}", jobTask);
        Collection<ScorableGHR> scorableGitHubRepositories = gitHubSearchService.searchRepositories(jobTask.language(), jobTask.dateToFetchFrom(), jobTask.durationAddedToDateToFetchFrom());
        logger.info("found {} # {}", jobTask, scorableGitHubRepositories.size());
        gitHubSearchService.save(scorableGitHubRepositories);
        gitHubSearchService.logTimeRangeAvailable(jobTask.language());
    }

}
