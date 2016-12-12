package org.pg6100.quizApi;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.pg6100.quizApi.dto.QuizDTO;
import org.pg6100.quizApi.dto.CategoryDTO;
import org.pg6100.quizApi.dto.SubCategoryDTO;
import org.pg6100.utils.web.JBossUtil;

import java.util.Arrays;
import java.util.List;

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
        List<QuizDTO> list1 = Arrays.asList(given().accept(ContentType.JSON).get("/quizzes")
                .then()
                .statusCode(200)
                .extract().as(QuizDTO[].class));

        list1.forEach(dto ->
                given().pathParam("id", dto.id).delete("/quizzes/{id}").then().statusCode(204));

        get("/quizzes").then().statusCode(200).body("size()", is(0));

        List<SubCategoryDTO> list2 = Arrays.asList(given().accept(ContentType.JSON).get("/subcategories")
                .then()
                .statusCode(200)
                .extract().as(SubCategoryDTO[].class));

        list2.forEach(dto ->
                given().pathParam("id", dto.id).delete("/subcategories/{id}").then().statusCode(204));

        get("/subcategories").then().statusCode(200).body("size()", is(0));

        List<CategoryDTO> list3 = Arrays.asList(given().accept(ContentType.JSON).get("/categories")
                .then()
                .statusCode(200)
                .extract().as(CategoryDTO[].class));

        list3.forEach(dto ->
                given().pathParam("id", dto.id).delete("/categories/{id}").then().statusCode(204));

        get("/categories").then().statusCode(200).body("size()", is(0));
    }
}
