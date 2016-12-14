package org.pg6100.gameRest;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pg6100.gameCommands.dto.AnswerDTO;
import org.pg6100.utils.web.JBossUtil;

import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;

public class GameRestIT {

    private static WireMockServer wiremockServer;

    @ClassRule
    public static final DropwizardAppRule<GameConfiguration> RULE =
            new DropwizardAppRule<>(GameApplication.class);

    @BeforeClass
    public static void initClass() {
        JBossUtil.waitForJBoss(10);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/game/api";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        wiremockServer = new WireMockServer(
                wireMockConfig().port(8099).notifier(new ConsoleNotifier(true))
        );
        wiremockServer.start();
    }

    @AfterClass
    public static void tearDown() {
        wiremockServer.stop();
    }

    @Test
    public void testSwagger(){
        given().baseUri("http://localhost")
                .port(8080)
                .accept(ContentType.JSON)
                .get("http://localhost:8080/index.html")
                .then()
                .statusCode(200);
    }

    private String getMockedJsonQuiz() {
        String json = "{\n" +
                "  \"id\": \"1\",\n" +
                "  \"category\": {\n" +
                "    \"id\": \"0\",\n" +
                "    \"name\": \"string\",\n" +
                "    \"rootCategoryId\": \"0\"\n" +
                "  },\n" +
                "  \"question\": \"question\",\n" +
                "  \"answerList\": [\n" +
                "    \"answer1\",\n" +
                "    \"answer2\",\n" +
                "    \"answer3\",\n" +
                "    \"answer4\"\n" +
                "  ],\n" +
                "  \"correctAnswer\": \"1\"\n" +
                "}";
        return json;
    }

    private void stubJsonQuiz(String url, String json) throws Exception {
        wiremockServer.stubFor(
                WireMock.get(
                        urlMatching("/quiz/api/quizzes" + url))
                        // define the mocked response of the GET
                        .willReturn(WireMock.aResponse()
                                .withHeader("Content-Type", "application/json; charset=utf-8")
                                .withHeader("Content-Length", "" + json.getBytes("utf-8").length)
                                .withBody(json)));
    }

    @Test
    public void testGetRandom() throws Exception {
        String json = getMockedJsonQuiz();
        stubJsonQuiz("/random", json);

        given().accept(ContentType.JSON)
                .get("/random")
                .then()
                .statusCode(200)
                .body("id", equalTo("1"))
                .body("question", equalTo("question"));
    }

    @Test
    public void testGetRandomFail() {
        given().accept(ContentType.JSON)
                .get("/random")
                .then()
                .statusCode(500);
    }

    @Test
    public void testPlayGameCorrect() throws  Exception {
        String json = getMockedJsonQuiz();
        stubJsonQuiz("/1", json);
        AnswerDTO answerDTO = new AnswerDTO("1", "1");

        given().accept(ContentType.JSON)
                .queryParam("quizAnswer", answerDTO)
                .post("/games")
                .then()
                .statusCode(200)
                .body(equalTo(true));
    }

    @Test
    public void testPlayGameWrong() throws  Exception {
        String json = getMockedJsonQuiz();
        stubJsonQuiz("/1", json);
        AnswerDTO answerDTO = new AnswerDTO("1", "2");

        given().accept(ContentType.JSON)
                .queryParam("quizAnswer", answerDTO)
                .post("/games")
                .then()
                .statusCode(200)
                .body(equalTo(false));
    }


}
