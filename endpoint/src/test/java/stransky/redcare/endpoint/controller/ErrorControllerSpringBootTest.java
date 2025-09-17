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
import stransky.redcare.endpoint.service.ScorableGHRProviderService;
import stransky.redcare.interfaces.repository.ScorableGHRRepository;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ErrorControllerSpringBootTest {

    @MockitoBean
    private ScorableGHRRepository scorableGHRRepository;

    @MockitoBean
    ScorableGHRProviderService resultProviderService;

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
        reset(resultProviderService);
    }

    @Test
    void handleBadRequest() {
        Mockito.when(resultProviderService.getScorableGHR(any(), any())).thenThrow(new IllegalArgumentException());
        given()//
                .contentType(MediaType.APPLICATION_JSON.toString())//
                .when()//
                .get("language/Java/creationdate/invalidata")//
                .then()//
                .statusCode(HttpStatus.BAD_REQUEST.value())//
                .log().all();
    }

    @Test
    void handleRunTimeException() {
        given()//
                .contentType(MediaType.APPLICATION_JSON.toString())//
                .when()//
                .get("language/java/creationdate/2025-09-15T13:27:00Z")//
                .then()//
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())//
                .log().all();
    }

    @Test
    void getScore_missingParameter() {
        given().contentType(MediaType.APPLICATION_JSON.toString())//
                .when()//
                .get("")//
                .then()//
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .log().all();
    }
}