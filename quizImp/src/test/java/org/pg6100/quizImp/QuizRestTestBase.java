package org.pg6100.quizImp;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.pg6100.quizApi.collection.ListDto;
import org.pg6100.quizApi.dto.QuizDTO;
import org.pg6100.quizApi.dto.CategoryDTO;
import org.pg6100.quizApi.dto.SubCategoryDTO;
import org.pg6100.utils.web.JBossUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

public class QuizRestTestBase {

    @BeforeClass
    public static void initClass() {
        JBossUtil.waitForJBoss(10);

        // RestAssured configs shared by all the tests
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/quiz/api";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }


    @Before
    @After
    public void clean() {
        cleanElements("/quizzes");

        List<SubCategoryDTO> list2 = Arrays.asList(RestAssured.given().accept(ContentType.JSON).get("/subcategories")
                .then()
                .statusCode(200)
                .extract().as(SubCategoryDTO[].class));

        list2.forEach(dto ->
                RestAssured.given().pathParam("id", dto.id).delete("/subcategories/{id}").then().statusCode(204));

        RestAssured.get("/subcategories").then().statusCode(200).body("size()", is(0));

        List<CategoryDTO> list3 = Arrays.asList(RestAssured.given().accept(ContentType.JSON).get("/categories")
                .then()
                .statusCode(200)
                .extract().as(CategoryDTO[].class));

        list3.forEach(dto ->
                RestAssured.given().pathParam("id", dto.id).delete("/categories/{id}").then().statusCode(204));

        RestAssured.get("/categories").then().statusCode(200).body("size()", is(0));
    }

    private void cleanElements(String path) {
        int total = Integer.MAX_VALUE;

        /*
            as the REST API does not return the whole state of the database (even,
            if I use an infinite "limit") I need to keep doing queries until the totalSize is 0
         */

        while (total > 0) {

            //seems there are some limitations when handling generics
            ListDto<?> listDto = given()
                    .queryParam("limit", Integer.MAX_VALUE)
                    .get(path)
                    .then()
                    .statusCode(200)
                    .extract()
                    .as(ListDto.class);

            listDto.list.stream()
                    //the "NewsDto" get unmarshalled into a map of fields
                    .map(n -> ((Map) n).get("id"))
                    .forEach(id ->
                            given().delete(path + "/" + id)
                                    .then()
                                    .statusCode(204)
                    );

            total = listDto.totalSize - listDto.list.size();
        }
    }
}
