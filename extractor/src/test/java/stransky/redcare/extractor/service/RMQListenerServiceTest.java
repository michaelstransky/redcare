package stransky.redcare.extractor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stransky.redcare.interfaces.job.JobTask;
import stransky.redcare.interfaces.repository.ScorableGHR;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RMQListenerServiceTest {

    @Mock
    GitHubSearchService gitHubSearchService;

    @InjectMocks
    RMQListenerService rmqListenerService;

    @Test
    void consume() {
        String language = "Java";
        Instant dateToFetchFrom = Instant.now();
        Duration durationAddedToDateToFetchFrom = Duration.ofDays(1);
        JobTask java = new JobTask(language, dateToFetchFrom, durationAddedToDateToFetchFrom);
        Collection<ScorableGHR> scorableGHRs = Collections.emptyList();
        when(gitHubSearchService.searchRepositories(language, dateToFetchFrom, durationAddedToDateToFetchFrom)).thenReturn(scorableGHRs);

        rmqListenerService.consume(java);

        verify(gitHubSearchService).searchRepositories(language, dateToFetchFrom, durationAddedToDateToFetchFrom);
        verify(gitHubSearchService).save(scorableGHRs);
    }
}