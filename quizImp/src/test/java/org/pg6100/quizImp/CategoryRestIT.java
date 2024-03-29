package org.pg6100.quizImp;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.pg6100.quizApi.dto.CategoryDTO;
import org.pg6100.quizApi.dto.SubCategoryDTO;
import org.pg6100.utils.web.HttpUtil;

import java.util.HashSet;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

public class CategoryRestIT extends CategoryRestTestBase {
    
    private static String CATEGORY_PATH = "/categories";
    private static String SUBCATEGORY_PATH = "/subcategories";

    @Test
    public void testCleanDB() {
        testGetRootCategories(0);
        testGetSubCategories(0);
    }

    @Test
    public void testCreateCategory() {
        CategoryDTO dto = createCategory("name");
        testGetRootCategories(1);
        testGetRootCategory(dto.id);
    }

    @Test
    public void testCreateSubCategory() {
        CategoryDTO rootDTO = createCategory("name");
        SubCategoryDTO dto = createSubCategory(rootDTO.id, "name");
        testGetSubCategories(1);
        testGetSubCategory(dto.id);
    }

    @Test
    public void testCreateTwoCategories() throws Exception {
        createCategory("name1");
        createCategory("name2");
        testGetRootCategories(2)
                .body("name", hasItems("name1", "name2"));
    }

    @Test
    public void testUpdateCategory() throws Exception {
        CategoryDTO dto = createCategory("name1");
        dto.name = "name2";
        given().contentType("application/merge-patch+json")
                .pathParam("id", dto.id)
                .body("{\"name\":\""+dto.name+"\"}")
                .patch("/categories/{id}")
                .then()
                .statusCode(204);
        testGetRootCategory(dto.id);
    }

    @Test
    public void testUpdateRootCategory() throws Exception {
        CategoryDTO dto = createCategory("name1");
        dto.name = "name2";
        updateRootCategory(dto, dto.id);
        testGetRootCategory(dto.id);
    }

    @Test
    public void testDeleteRootCategory() throws Exception {
        CategoryDTO dto = createCategory("name");
        deleteRootCategory(dto.id);
        get(CATEGORY_PATH).then().statusCode(200).body("size()", is(0));
    }

    @Test
    public void testExpand() {
        CategoryDTO rootDTO = createCategory("name");
        SubCategoryDTO subDTO = createSubCategory(rootDTO.id, "name");
        rootDTO.subCategories = new HashSet<>();
        rootDTO.subCategories.add(subDTO);

        // Categories with expand false should not be present
        testGetRootCategories(1).body("subCategories", hasItem(nullValue()));

        // Categories with expand true should be present
        given().queryParam("expand", true).get("/categories").then()
                .statusCode(200).body("subCategories[0].id", hasItems(subDTO.id));

        // Category with expand false should not be present
        testGetRootCategory(rootDTO.id).body("subCategories", nullValue());

        // Category with expand true should be present
        given().pathParam("id", rootDTO.id).queryParam("expand", true).get("/categories/{id}").then()
                .statusCode(200).body("subCategories.id", hasItems(subDTO.id));
    }


    @Test
    public void testGetSubCategories() throws Exception {
        CategoryDTO rootDTO1 = createCategory("root1");
        CategoryDTO rootDTO2 = createCategory("root2");
        createSubCategory(rootDTO1.id, "sub1");
        createSubCategory(rootDTO2.id, "sub2");
        createSubCategory(rootDTO2.id, "sub3");

        testGetSubCategories(3)
                .body("name", hasItems("sub1", "sub2", "sub3"));
        given().contentType(ContentType.JSON).queryParam("parentId", rootDTO1.id)
                .get("/subcategories").then().statusCode(200)
                .body("size()", is(1));
        given().contentType(ContentType.JSON).pathParam("parentId", rootDTO2.id)
                .get("/categories/{parentId}/subcategories").then().statusCode(200)
                .body("size()", is(2));
    }

    @Test
    public void testUpdateSubCategory() throws Exception {
        CategoryDTO rootDTO = createCategory("root");
        SubCategoryDTO subDTO = createSubCategory(rootDTO.id, "name1");
        subDTO.name = "name2";
        updateSubCategory(subDTO, subDTO.id);
        testGetSubCategory(subDTO.id);
    }

    @Test
    public void testDeleteSubCategory() throws Exception {
        CategoryDTO dto = createCategory("name");
        SubCategoryDTO subDTO = createSubCategory(dto.id, "name");
        deleteSubCategory(subDTO.id);
        testGetSubCategories(0);
    }

    @Test
    public void testGetSubCategoriesByRootCategory() throws Exception {
        CategoryDTO rootDTO = createCategory("root");
        CategoryDTO rootDTO2 = createCategory("root2");
        SubCategoryDTO subDTO = createSubCategory(rootDTO.id, "sub1");
        SubCategoryDTO subDTO2 = createSubCategory(rootDTO.id, "sub2");
        SubCategoryDTO subDTO3 = createSubCategory(rootDTO2.id, "sub3");

        given().pathParam("id", rootDTO.id)
                .get("/categories/{id}/subcategories")
                .then()
                .statusCode(200)
                .body("name", hasItems("sub1", "sub2"));
    }

    @Test
    public void testCreateCategoryRawHttp() throws Exception {
        String body = "{\"name\": \"økologi\"}";

        String header = "POST /quiz/api/categories HTTP/1.1\n";
        header += "HOST: localhost:8080\n";
        header += "content-type: application/json\n";
        header += "accept: application/json\n";
        header += "Content-Length: " + body.getBytes("UTF-8").length + "\n";
        header += "\n";

        String message = header + body;
        String result = HttpUtil.executeHttpCommand("localhost", 8080, message);

        String headers = HttpUtil.getHeaderBlock(result);
        assertTrue(headers.contains("200 OK"));

        String contentType = HttpUtil.getHeaderValue("Content-Type", result);
        assertTrue(contentType.contains("application/json"));

        body = HttpUtil.getBodyBlock(result);
        String id = body.replace("\n", "");
        testGetRootCategory(id);
    }


    private CategoryDTO createCategory(String name) {
        CategoryDTO dto = new CategoryDTO(name);
        dto.id = registerCategory(dto, CATEGORY_PATH);
        return dto;
    }

    private SubCategoryDTO createSubCategory(String rootCategoryId, String name) {
        SubCategoryDTO dto = new SubCategoryDTO(rootCategoryId, name);
        dto.id = registerCategory(dto, SUBCATEGORY_PATH);
        return dto;
    }
    
    private ValidatableResponse testGetRootCategory(String id) {
        return testGetCategory(CATEGORY_PATH, id);
    }

    private void testGetSubCategory(String id) {
        testGetCategory(SUBCATEGORY_PATH, id);
    }

    private ValidatableResponse testGetCategory(String path, String id) {
        return given().pathParam("id", id)
                .get(path + "/{id}")
                .then()
                .statusCode(200)
                .body("id", is(id));
    }

    private ValidatableResponse testGetRootCategories(int size) {
        return testGetCategories(CATEGORY_PATH, size);
    }

    private ValidatableResponse testGetSubCategories(int size) {
        return testGetCategories(SUBCATEGORY_PATH, size);
    }

    private ValidatableResponse testGetCategories(String path, int size) {
        return get(path).then().statusCode(200).body("size()", is(size));
    }

    private String registerCategory(Object dto, String path) {
        return given().contentType(ContentType.JSON)
                .body(dto)
                .post(path)
                .then()
                .statusCode(200)
                .extract().asString();
    }
    
    private void updateRootCategory(Object dto, String id) {
        updateCategory(dto, id, CATEGORY_PATH);
    }

    private void updateSubCategory(Object dto, String id) {
        updateCategory(dto, id, SUBCATEGORY_PATH);
    }

    private void updateCategory(Object dto, String id, String path) {
        given().contentType(ContentType.JSON)
                .pathParam("id", id)
                .body(dto)
                .put(path + "/{id}")
                .then()
                .statusCode(204);
    }
    
    private void deleteRootCategory(String id) {
        deleteCategory(id, CATEGORY_PATH);
    }

    private void deleteSubCategory(String id) {
        deleteCategory(id, SUBCATEGORY_PATH);
    }
    
    private void deleteCategory(String id, String path) {
        given().contentType(ContentType.JSON)
                .pathParam("id", id)
                .delete(path + "/{id}")
                .then()
                .statusCode(204);
    }
}