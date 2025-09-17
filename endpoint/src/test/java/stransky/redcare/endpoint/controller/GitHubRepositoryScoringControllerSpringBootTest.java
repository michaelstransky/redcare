package stransky.redcare.endpoint.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import stransky.redcare.endpoint.service.RepositoryScorerService;
import stransky.redcare.endpoint.service.ScorableGHRProviderService;

import java.time.Instant;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GitHubRepositoryScoringControllerSpringBootTest {

    @MockitoBean
    RepositoryScorerService repositoryScorer;

    @MockitoBean
    ScorableGHRProviderService scorableGHRProviderService;

    @LocalServerPort
    private int port;


    @BeforeAll
    static void beforeAll() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.basePath = "/repository_score/v1/";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }


    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        reset(scorableGHRProviderService);
    }

    @Test
    void getScore_nothingCached() {
        Mockito.when(scorableGHRProviderService.getOldestAvailableDate(any())).thenReturn(Instant.MAX);
        final String language = "java";
        final Instant date = Instant.MIN;
        given()//
                .contentType(MediaType.APPLICATION_JSON.toString())//
                .when()//
                .get("language/" + language + "/creationdate/" + date)
                .then()//
                .statusCode(HttpStatus.ACCEPTED.value())//
                .log().all();
        verify(scorableGHRProviderService).getOldestAvailableDate(language);
        verify(scorableGHRProviderService).commission(language, date);
    }

    @Test
    void getScore_alreadyCached() {
        final String language = "java";
        final Instant date = Instant.now();
        Mockito.when(scorableGHRProviderService.getOldestAvailableDate(any())).thenReturn(date);
        given()//
                .contentType(MediaType.APPLICATION_JSON.toString())//
                .when()//
                .get("language/" + language + "/creationdate/" + date)
                .then()//
                .statusCode(HttpStatus.OK.value())//
                .log().all();
        verify(scorableGHRProviderService).getOldestAvailableDate(language);
        verify(scorableGHRProviderService).getScorableGHR(language, date);
    }
}