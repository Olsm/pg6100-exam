package org.pg6100.quizImp;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.pg6100.quizApi.collection.ListDto;
import org.pg6100.quizApi.dto.CategoryDTO;
import org.pg6100.quizApi.dto.SubCategoryDTO;
import org.pg6100.quizApi.dto.QuizDTO;

import java.util.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

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
        testGet().body("list.size()", is(0));
    }

    @Test
    public void testCreateAndGetQuiz() {
        QuizDTO dto = createQuizDTO();
        dto.id = testRegisterQuiz(dto);
        testGet().body("list.size()", is(1));
        testGet("/{id}", dto.id)
                .body("id", is(dto.id))
                .body("category.name", Is.is(subCategory.name))
                .body("category.rootCategoryId", Is.is(rootCategory.id))
                .body("question", is(dto.question))
                .body("answerList", is(dto.answerList))
                .body("correctAnswer", is(dto.correctAnswer));
    }

    @Test
    public void testGetRandom() {
        QuizDTO dto = createQuizDTO();
        dto.id = testRegisterQuiz(dto);
        testGet("/random").body("id", is(dto.id));
    }

    @Test
    public void testGetRandomNoQuizzes() {
        get("/quizzes/random").then().statusCode(400);
    }

    @Test
    public void testDelete() {
        String id = testRegisterQuiz(createQuizDTO());
        testGet().body("list.id", is(Collections.singletonList(id)));

        delete("/quizzes/" + id);
        testGet().body("list.id", not(Collections.singletonList(id)));
    }


    @Test
    public void testUpdate() throws Exception {
        QuizDTO quizDTO = createQuizDTO();
        String id = testRegisterQuiz(quizDTO);
        testGet().body("list.question", contains(quizDTO.question));

        String updatedQuestion = "new question";

        //now change text with PUT
        QuizDTO dto = createQuizDTO(id, quizDTO.category, updatedQuestion, quizDTO.answerList, quizDTO.correctAnswer);
        testUpdateQuiz(dto, dto.id);

        //was the PUT fine?
        testGet().body("list.question", contains(updatedQuestion));

        //now rechange, but just the text
        String anotherQuestion = "yet another question";

        given().contentType(ContentType.TEXT)
                .body(anotherQuestion)
                .pathParam("id", id)
                .put("/quizzes/{id}/question")
                .then()
                .statusCode(204);

        testGet().body("list.question", contains(anotherQuestion));
    }

    @Test
    public void testPatchChangeQuestion() throws Exception {
        QuizDTO quizDTO = createQuizDTO();
        quizDTO.id = testRegisterQuiz(quizDTO);
        testGet().body("list.question", contains(quizDTO.question));

        String updatedQuestion = "new question";

        //now change text with Patch
        given().contentType("application/merge-patch+json")
                .pathParam("id", quizDTO.id)
                .body("{\"question\":\""+updatedQuestion+"\"}")
                .patch("/quizzes/{id}")
                .then()
                .statusCode(204);

        //was the Patch fine?
        testGet().body("list.question", contains(updatedQuestion));
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
        testGet().body("list.size()", is(6));
    }

    @Test
    public void testGetAllByCategory() {
        createSomeQuizes();
        testGet("/categories/{id}", subCategory.id).body("list.size()", is(4));
        testGet("/categories/{id}", subCategory2.id).body("list.size()", is(2));
        testGet("/categories/{id}", subCategory3.id).body("list.size()", is(0));
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

    @Test
    public void testSelfLink() {
        int n = 6, limit = 3;
        createSomeQuizes();

        ListDto<?> listDto = given()
                .queryParam("limit", limit)
                .get("/quizzes")
                .then()
                .statusCode(200)
                .extract()
                .as(ListDto.class);

        assertEquals(n, (int) listDto.totalSize);
        assertEquals(0, (int) listDto.rangeMin);
        assertEquals(limit - 1, (int) listDto.rangeMax);

        assertNull(listDto._links.previous);
        assertNotNull(listDto._links.next);
        assertNotNull(listDto._links.self);

        //read again using self link
        ListDto<?> selfDto = given()
                .get(listDto._links.self.href)
                .then()
                .statusCode(200)
                .extract()
                .as(ListDto.class);

        Set<String> first = getQuestions(listDto);
        Set<String> self = getQuestions(selfDto);

        assertContainsTheSame(first, self);
    }

    @Test
    public void testNextLink() {
        int n = 6, limit = 2;
        createSomeQuizes();

        ListDto<?> listDto = given()
                .queryParam("limit", limit)
                .get("/quizzes")
                .then()
                .statusCode(200)
                .extract()
                .as(ListDto.class);

        assertEquals(n, (int) listDto.totalSize);
        assertNotNull(listDto._links.next.href);

        Set<String> values = getQuestions(listDto);
        String next = listDto._links.next.href;

        int counter = 0;

        //read pages until there is still a "next" link
        while (next != null) {

            counter++;

            int beforeNextSize = values.size();

            listDto = given()
                    .get(next)
                    .then()
                    .statusCode(200)
                    .extract()
                    .as(ListDto.class);

            values.addAll(getQuestions(listDto));

            assertEquals(beforeNextSize + limit, values.size());
            assertEquals(counter * limit, (int) listDto.rangeMin);
            assertEquals(listDto.rangeMin + limit - 1, (int) listDto.rangeMax);

            if (listDto._links.next != null) {
                next = listDto._links.next.href;
            } else {
                next = null;
            }
        }

        assertEquals(n, values.size());
    }

    @Test
    public void textPreviousLink() {
        int n = 6, limit = 2;
        createSomeQuizes();

        ListDto<?> listDto = given()
                .queryParam("limit", limit)
                .get("/quizzes")
                .then()
                .statusCode(200)
                .extract()
                .as(ListDto.class);

        Set<String> first = getQuestions(listDto);

        //read next page
        ListDto<?> nextDto = given()
                .get(listDto._links.next.href)
                .then()
                .statusCode(200)
                .extract()
                .as(ListDto.class);

        Set<String> next = getQuestions(nextDto);
        // check that an element of next page was not in the first page
        assertTrue(!first.contains(next.iterator().next()));

        /*
            The "previous" page of the "next" page should be the
            first "self" page, ie

            self.next.previous == self
         */
        ListDto<?> previousDto = given()
                .get(nextDto._links.previous.href)
                .then()
                .statusCode(200)
                .extract()
                .as(ListDto.class);

        Set<String> previous = getQuestions(previousDto);
        assertContainsTheSame(first, previous);
    }

    @Test
    public void testFilter(){
        QuizDTO dto = createQuizDTO();
        dto.id = testRegisterQuiz(dto);
        createSomeQuizes();

        // Verify get with filter contains quiz
        given().queryParam("filter", dto.category.id)
                .get("/quizzes")
                .then()
                .statusCode(200)
                .body("list.size()", is(5))
                .body("list.id", hasItem(dto.id));

        // Verify get with filter of category without quiz
        given().queryParam("filter", subCategory2.id)
                .get("/quizzes")
                .then()
                .statusCode(200)
                .body("list.size()", is(2))
                .body("list.id", not(hasItem(dto.id)));
    }

    /**
     * Extract the "question" fields from all the quizzes
     */
    private Set<String> getQuestions(ListDto<?> selfDto) {

        Set<String> values = new HashSet<>();
        selfDto.list.stream()
                .map(m -> (String) ((Map) m).get("question"))
                .forEach(t -> values.add(t));

        return values;
    }

    private void assertContainsTheSame(Collection<?> a, Collection<?> b) {
        assertEquals(a.size(), b.size());
        a.stream().forEach(v -> assertTrue(b.contains(v)));
        b.stream().forEach(v -> assertTrue(a.contains(v)));
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