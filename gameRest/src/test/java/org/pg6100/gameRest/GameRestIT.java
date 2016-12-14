package org.pg6100.gameRest;

import io.dropwizard.testing.junit.DropwizardAppRule;
import io.restassured.http.ContentType;
import org.junit.ClassRule;
import org.junit.Test;

import static io.restassured.RestAssured.given;

public class GameRestIT {

    @ClassRule
    public static final DropwizardAppRule<GameConfiguration> RULE =
            new DropwizardAppRule<>(GameApplication.class);

    @Test
    public void testSwagger(){
        given().baseUri("http://localhost")
                .port(8080)
                .accept(ContentType.JSON)
                .get("/index.html")
                .then()
                .statusCode(200);
    }
}
