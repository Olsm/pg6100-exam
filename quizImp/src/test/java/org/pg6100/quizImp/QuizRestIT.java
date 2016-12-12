package org.pg6100.quizImp;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.pg6100.quizApi.dto.CategoryDTO;
import org.pg6100.quizApi.dto.SubCategoryDTO;
import org.pg6100.quizApi.dto.QuizDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.*;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;

public class QuizRestIT extends QuizRestTestBase {

    private CategoryDTO rootCategory;
    private SubCategoryDTO subCategory;
    private SubCategoryDTO subCategory2;
    private SubCategoryDTO subCategory3;

    @Before
    public void setup() {
        rootCategory = new CategoryDTO("rootCategory");
        rootCategory.id = registerCategory(rootCategory, "/categories");
        subCategory = new SubCategoryDTO(rootCategory.id, "subCategory");
        subCategory.id = registerCategory(subCategory, "/subcategories");
    }

    @Test
    public void testCleanDB() {
        testGet().body("size()", is(0));
    }

    @Test
    public void testCreateAndGet() {
        String question = "Such Question";
        List<String> answerList = new ArrayList<>();
        answerList.add("ans1");
        answerList.add("ans2");
        answerList.add("ans3");
        answerList.add("ans4");
        String correctAnswer = answerList.get(3);
        testGet().body("size()", is(0));

        QuizDTO dto = createQuizDTO(null, subCategory, question, answerList, correctAnswer);
        String id = testRegisterQuiz(dto);
        testGet().body("size()", is(1));
        testGet("/{id}", id)
                .body("id", is(id))
                .body("category.name", Is.is(subCategory.name))
                .body("category.rootCategoryId", Is.is(rootCategory.id))
                .body("question", is(question))
                .body("answerList", is(answerList))
                .body("correctAnswer", is(correctAnswer));
    }

    @Test
    public void testDelete() {
        String id = testRegisterQuiz(createQuizDTO());
        testGet().body("id", is(Collections.singletonList(id)));

        delete("/quizzes/" + id);
        testGet().body("id", not(Collections.singletonList(id)));
    }


    @Test
    public void testUpdate() throws Exception {
        QuizDTO quizDTO = createQuizDTO();
        String id = testRegisterQuiz(quizDTO);
        testGet().body("question", contains(quizDTO.question));

        String updatedQuestion = "new question";

        //now change text with PUT
        QuizDTO dto = createQuizDTO(id, quizDTO.category, updatedQuestion, quizDTO.answerList, quizDTO.correctAnswer);
        testUpdateQuiz(dto, dto.id);

        //was the PUT fine?
        testGet().body("question", contains(updatedQuestion));

        //now rechange, but just the text
        String anotherQuestion = "yet another question";

        given().contentType(ContentType.TEXT)
                .body(anotherQuestion)
                .pathParam("id", id)
                .put("/quizzes/{id}/question")
                .then()
                .statusCode(204);

        testGet().body("question", contains(anotherQuestion));
    }

    @Test
    public void testMissingForUpdate() {
        QuizDTO quizDTO = createQuizDTO();
        quizDTO.id = "-333";
        testUpdateQuiz(quizDTO, "-333", 404);
    }

    @Test
    public void testUpdateNonMatchingId() {
        QuizDTO quizDTO = createQuizDTO();
        quizDTO.id = "1";
        testUpdateQuiz(quizDTO, "-333", 409);
    }


    @Test
    public void testInvalidUpdate() {
        QuizDTO dto = createQuizDTO();
        dto.id = testRegisterQuiz(dto);
        QuizDTO quizDTO = createQuizDTO(dto.id, null, "", null, null);
        testUpdateQuiz(quizDTO, dto.id, 400);
    }

    private void createSomeQuizes() {
        CategoryDTO rootCategory2 = new CategoryDTO("root2");
        rootCategory2.id = registerCategory(rootCategory2, "/categories");
        subCategory2 = new SubCategoryDTO(rootCategory2.id, "sub2");
        subCategory2.id = registerCategory(subCategory2, "/subcategories");
        subCategory3 = new SubCategoryDTO(rootCategory2.id, "sub3");
        subCategory3.id = registerCategory(subCategory3, "/subcategories");
        List<String> answerList = new ArrayList<>();
        answerList.add("ans1");
        answerList.add("ans2");
        answerList.add("ans3");
        answerList.add("ans4");
        testRegisterQuiz(createQuizDTO(null, subCategory, "q1", answerList, answerList.get(1)));
        testRegisterQuiz(createQuizDTO(null, subCategory, "q2", answerList, answerList.get(1)));
        testRegisterQuiz(createQuizDTO(null, subCategory, "q3", answerList, answerList.get(1)));
        testRegisterQuiz(createQuizDTO(null, subCategory, "q4", answerList, answerList.get(1)));
        testRegisterQuiz(createQuizDTO(null, subCategory2, "q5", answerList, answerList.get(1)));
        testRegisterQuiz(createQuizDTO(null, subCategory2, "q6", answerList, answerList.get(1)));
    }

    private QuizDTO createQuizDTO(String id, SubCategoryDTO category, String question, List<String> answerList, String correctAnswer) {
        return new QuizDTO(id, category, question, answerList, correctAnswer);
    }

    private QuizDTO createQuizDTO() {
        String question = "Such Question";
        List<String> answerList = new ArrayList<>();
        answerList.add("ans1");
        answerList.add("ans2");
        answerList.add("ans3");
        answerList.add("ans4");
        String correctAnswer = answerList.get(3);
        return createQuizDTO(null, subCategory, question, answerList, correctAnswer);
    }

    @Test
    public void testGetAll() {
        createSomeQuizes();
        testGet().body("size()", is(6));
    }

    @Test
    public void testGetAllByCategory() {
        createSomeQuizes();
        testGet("/categories/{id}", subCategory.id).body("size()", is(4));
        testGet("/categories/{id}", subCategory2.id).body("size()", is(2));
        testGet("/categories/{id}", subCategory3.id).body("size()", is(0));
    }

    @Test
    public void testInvalidGetByCategory() {
        testGet("/categories/{id}", "foo", 404);
    }

    @Test
    public void testInvalidCategory() {
        QuizDTO quizDTO = createQuizDTO();
        quizDTO.category = new SubCategoryDTO("null", "null");
        testRegisterQuiz(quizDTO, 400);
    }

    @Test
    public void testPostWithId() {
        QuizDTO quizDTO = createQuizDTO();
        testRegisterQuiz(createQuizDTO("1", quizDTO.category, quizDTO.question, quizDTO.answerList, quizDTO.correctAnswer), 400);
    }

    @Test
    public void testPostWithWrongType() {
        given().contentType(ContentType.XML)
                .body("<foo></foo>")
                .post("/quizzes")
                .then()
                .statusCode(415);
    }


    @Test
    public void testGetByInvalidId() {
        testGet("/{id}", "foo", 404);
    }

    private String registerCategory(Object dto, String path) {
        return given().contentType(ContentType.JSON)
                .body(dto)
                .post(path)
                .then()
                .statusCode(200)
                .extract().asString();
    }

    private ValidatableResponse testGet() {
        return testGet("");
    }

    private ValidatableResponse testGet(String path) {
        return get("/quizzes" + path).then()
                .statusCode(200);
    }

    private ValidatableResponse testGet(String path, String id) {
        return  testGet(path, id, 200);
    }

    private ValidatableResponse testGet(String path, String id, int statusCode) {
        return given().pathParam("id", id)
                .get("/quizzes" + path).then()
                .statusCode(statusCode);
    }

    private String testRegisterQuiz(Object dto) {
        return  testRegisterQuiz(dto, 200);
    }

    private String testRegisterQuiz(Object dto, int statusCode) {
        return given().contentType(ContentType.JSON)
                .body(dto)
                .post("/quizzes")
                .then()
                .statusCode(statusCode)
                .extract().asString();
    }

    private ValidatableResponse testUpdateQuiz(Object dto, String id) {
        return testUpdateQuiz(dto, id, 204);
    }

    private ValidatableResponse testUpdateQuiz(Object dto, String id, int statusCode) {
        return given().contentType(ContentType.JSON)
                .pathParam("id", id)
                .body(dto)
                .put("/quizzes/{id}")
                .then()
                .statusCode(statusCode);
    }
}