package stransky.redcare.interfaces.repository;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.Instant;

/**
 * A GitHub Repository, which contains all the information for assigning a score to it.
 * GitHub Repository is abbreviated to GHR to avoid confusion with the Repository for this Entity.
 */
@Entity
public class ScorableGHR {

    @Id
    String fullName;
    String language;
    Instant createdAt;
    int stargazers;
    int forks;
    Instant updatedAt;

    public ScorableGHR() {
    }

    public ScorableGHR(String fullName, String language, Instant createdAt, int stargazers, int forks, Instant updatedAt) {
        this.fullName = fullName;
        this.language = language;
        this.createdAt = createdAt;
        this.stargazers = stargazers;
        this.forks = forks;
        this.updatedAt = updatedAt;
    }

    public String getFullName() {
        return fullName;
    }

    public String getLanguage() {
        return language;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public int getStargazers() {
        return stargazers;
    }

    public int getForks() {
        return forks;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
