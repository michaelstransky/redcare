package stransky.redcare.endpoint.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stransky.redcare.endpoint.model.ScoredGHR;
import stransky.redcare.endpoint.service.RepositoryScorerService;
import stransky.redcare.endpoint.service.ScorableGHRProviderService;
import stransky.redcare.interfaces.repository.ScorableGHR;

import java.time.Instant;
import java.util.Collection;
import java.util.List;


@Tag(name = "GitHub Repository Scoring", description = "Returns GitHub repository scorings immediately, when available or commissions to get the required info - then come back again later.")
@RestController
@RequestMapping(path = "/repository_score/v1/")
public class GitHubRepositoryScoringController {
    private static final Logger logger = LoggerFactory.getLogger(GitHubRepositoryScoringController.class);

    private final ScorableGHRProviderService resultProviderService;
    private final RepositoryScorerService repositoryScorerService;

    public GitHubRepositoryScoringController(ScorableGHRProviderService resultProviderService, RepositoryScorerService repositoryScorerService) {
        this.resultProviderService = resultProviderService;
        this.repositoryScorerService = repositoryScorerService;
    }

    @Operation(summary = "GitHub Repository Scoring", description = "Returns scores of repositories of the given language and newer than the given creation date")
    @ApiResponse(responseCode = "200", description = "OK: returns Scored Repositories")
    @ApiResponse(responseCode = "202", description = "Accepted: in progress, come here again later for results", content = @Content(schema = @Schema(hidden = true)))
    @GetMapping(path = "language/{language}/creationdate/{creationDate}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Collection<ScoredGHR>> score(
            @Parameter(
                    name = "language",
                    description = "Programming language exactly case sensitive as written on GitHub",
                    example = "Java",
                    required = true)
            @PathVariable String language,
            @Parameter(
                    name = "creationDate",
                    description = "Oldest creation date",
                    example = "2025-09-15T13:37:00Z",
                    required = true)
            @PathVariable Instant creationDate) {
        if (resultProviderService.getOldestAvailableDate(language).isAfter(creationDate)) {
            logger.info("{} {} commission", language, creationDate);
            resultProviderService.commission(language, creationDate);
            return ResponseEntity.accepted().build();
        }
        logger.info("{} {} available", language, creationDate);
        Collection<ScorableGHR> scorableGHR = resultProviderService.getScorableGHR(language, creationDate);
        List<ScoredGHR> scores = scorableGHR.stream().map(repositoryScorerService::score).toList();
        return ResponseEntity.ok(scores);
    }
}
