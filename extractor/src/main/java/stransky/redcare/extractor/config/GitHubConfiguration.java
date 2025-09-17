package stransky.redcare.extractor.config;


import org.kohsuke.github.GitHub;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@ConfigurationProperties(prefix = "stransky.redcare.github")
public class GitHubConfiguration {
    private String login;
    private String oauthAccessToken;

    public GitHub createGitHubConnection() throws IOException {
        if (login != null && oauthAccessToken != null) {
            return GitHub.connect(login, oauthAccessToken);
        } else {
            return GitHub.connectAnonymously();
        }
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setOauthAccessToken(String oauthAccessToken) {
        this.oauthAccessToken = oauthAccessToken;
    }
}
