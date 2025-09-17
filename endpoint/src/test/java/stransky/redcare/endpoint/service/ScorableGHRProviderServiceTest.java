package stransky.redcare.endpoint.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import stransky.redcare.endpoint.config.GitHubSearchConfiguration;
import stransky.redcare.interfaces.job.JobTask;
import stransky.redcare.interfaces.repository.ScorableGHR;
import stransky.redcare.interfaces.repository.ScorableGHRRepository;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScorableGHRProviderServiceTest {

    @Mock
    ScorableGHRRepository scorableGHRRepository;

    @Mock
    RabbitTemplate rabbitTemplate;

    @Spy
    GitHubSearchConfiguration gitHubSearchServiceConfiguration;

    @InjectMocks
    @Spy
    ScorableGHRProviderService scorableGHRProviderService;


    @Captor
    ArgumentCaptor<String> languageCaptor;
    @Captor
    ArgumentCaptor<Instant> instantCaptor;
    @Captor
    ArgumentCaptor<Duration> durationCaptor;

    @Test
    void getOldestAvailableDate() {
        Instant i = Instant.now();
        when(scorableGHRRepository.oldest(any())).thenReturn(i);
        String language = "Java";
        Instant oldestAvailableDate = scorableGHRProviderService.getOldestAvailableDate(language);
        verify(scorableGHRRepository).oldest(language);
        assertEquals(i, oldestAvailableDate);
    }

    @Test
    void getOldestAvailableDate_null() {
        String language = "Java";
        Instant oldestAvailableDate = scorableGHRProviderService.getOldestAvailableDate(language);
        verify(scorableGHRRepository).oldest(language);
        assertEquals(oldestAvailableDate.isBefore(Instant.now()), oldestAvailableDate.isAfter(Instant.now().minus(1, ChronoUnit.HOURS)));
    }

    @Test
    void getScorableGHR() {
        Instant i = Instant.now();
        String language = "Java";
        ScorableGHR scorableGHR = new ScorableGHR("name", language, i, 1, 2, Instant.now());
        when(scorableGHRRepository.find(any(), any())).thenReturn(new Object[]{scorableGHR});

        List<ScorableGHR> scorableGitHubRepositories = scorableGHRProviderService.getScorableGHR(language, i);
        verify(scorableGHRRepository).find(language, i);
        assertEquals(scorableGHR, scorableGitHubRepositories.getFirst());
    }

    @Test
    void commission() {
        Instant oldest = Instant.now();
        Duration d = Duration.ofHours(50);
        Instant earliestRequested = oldest.minus(d);
        String language = "Java";
        Duration duration = gitHubSearchServiceConfiguration.getChunkSize();
        when(scorableGHRRepository.oldest(any())).thenReturn(oldest);

        scorableGHRProviderService.commission(language, earliestRequested);

        verify(scorableGHRProviderService, times(3)).commission(languageCaptor.capture(), instantCaptor.capture(), durationCaptor.capture());
        assertTrue(languageCaptor.getAllValues().stream().allMatch(language::equals));
        List<Duration> durations = durationCaptor.getAllValues().stream().toList();
        List<Duration> durationsExceptLastWhichCanBeShorter = durations.subList(0, durations.size() - 1);
        assertTrue(durationsExceptLastWhichCanBeShorter.stream().allMatch(duration::equals));
        assertTrue(d.toMillis() < durations.stream().reduce(Duration.ZERO, Duration::plus).toMillis());
        List<Instant> instants = instantCaptor.getAllValues();
        assertEquals(oldest, instants.getFirst().plus(duration));
        assertTrue(earliestRequested.isAfter(instants.getLast()));
    }


    @Test
    void commission_shortQuery() {
        Instant oldest = Instant.now();
        Duration d = Duration.ofMinutes(1);
        Instant earliestRequested = oldest.minus(d);
        String language = "Java";
        when(scorableGHRRepository.oldest(any())).thenReturn(oldest);

        scorableGHRProviderService.commission(language, earliestRequested);

        verify(scorableGHRProviderService).commission(languageCaptor.capture(), instantCaptor.capture(), durationCaptor.capture());
        assertTrue(languageCaptor.getAllValues().stream().allMatch(language::equals));
        List<Duration> durations = durationCaptor.getAllValues().stream().toList();
        List<Instant> instants = instantCaptor.getAllValues();
        assertEquals(d.plus(gitHubSearchServiceConfiguration.getDurationInWhichForSureRepositoriesAreCreated()), durations.getFirst());
        assertTrue(earliestRequested.isAfter(instants.getLast()));
    }

    @Test
    void isAlreadyCommissioned() {
        String language = "Java";
        Instant i = Instant.now();
        Instant before = i.minus(50, ChronoUnit.HOURS);

        assertFalse(scorableGHRProviderService.isAlreadyCommissioned(language, before));
        assertTrue(scorableGHRProviderService.isAlreadyCommissioned(language, i));
    }

    @Test
    void isAlreadyCommissioned_dos() {
        String language = "Java";
        Instant i = Instant.now();

        assertFalse(scorableGHRProviderService.isAlreadyCommissioned(language, i));
        assertTrue(scorableGHRProviderService.isAlreadyCommissioned(language, i));
        assertTrue(scorableGHRProviderService.isAlreadyCommissioned(language.toLowerCase(), i));
        assertTrue(scorableGHRProviderService.isAlreadyCommissioned(language.toUpperCase(), i));
    }

    @Test
    void min() {
        Instant earlier = Instant.now();
        Instant later = earlier.plusSeconds(5);
        Instant min = scorableGHRProviderService.min(earlier, later);
        assertEquals(earlier, min);
    }

    @Test
    void max() {
        Instant earlier = Instant.now();
        Instant later = earlier.plusSeconds(5);
        Instant max = scorableGHRProviderService.max(earlier, later);
        assertEquals(later, max);
    }

    @Test
    void max_null() {
        Instant e = Instant.now();
        Instant a = scorableGHRProviderService.max(e, null);
        assertEquals(e, a);
    }

    @Test
    void commission_Part() {
        String language = "Java";
        Instant dateToFetch = Instant.now();
        Duration duration = Duration.ofDays(1);
        scorableGHRProviderService.commission(language, dateToFetch, duration);
        verify(rabbitTemplate).convertAndSend(new JobTask(language, dateToFetch, duration));
    }
}