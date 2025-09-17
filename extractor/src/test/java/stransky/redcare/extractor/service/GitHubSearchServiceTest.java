package stransky.redcare.extractor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GitHub;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import stransky.redcare.extractor.config.GitHubConfiguration;
import stransky.redcare.interfaces.repository.ScorableGHR;
import stransky.redcare.interfaces.repository.ScorableGHRRepository;

import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GitHubSearchServiceTest {

    @Mock
    ScorableGHRRepository scorableGHRRepository;

    @Spy
    GitHubConfiguration gitHubConfiguration = new GitHubConfiguration() {
        @Override
        public GitHub createGitHubConnection() {
            return gitHub;
        }
    };

    @Mock
    private GitHub gitHub;


    @InjectMocks
    GitHubSearchService gitHubSearchService;

    @Test
    void save() {
        Collection<ScorableGHR> scorableGitHubRepositories = Collections.emptyList();
        gitHubSearchService.save(scorableGitHubRepositories);
        verify(scorableGHRRepository).saveAllAndFlush(scorableGitHubRepositories);
    }

}