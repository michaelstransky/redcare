package stransky.redcare.endpoint.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A GitHub Repository with its score.
 * GitHub Repository is abbreviated to GHR to avoid confusion with Repository pattern for serialisation.
 *
 * @param fullName name of GitHub Repository
 * @param score    the score assigned to it, representing a quality.
 */
@Schema(description = "GitHub repository with its assigned score.")
public record ScoredGHR(@Schema(
        description = "GitHub repository",
        name = "fullName",
        type = "string",
        example = "owner/repo") String fullName, @Schema(
        description = "Score of GitHub repository",
        name = "score",
        type = "double",
        example = "2.7") double score) {
}
