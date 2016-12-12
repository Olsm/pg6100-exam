package org.pg6100.quizImp;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.Test;
import org.pg6100.quizApi.dto.CategoryDTO;
import org.pg6100.quizApi.dto.SubCategoryDTO;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.Is.is;

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

    /* TODO
    @Test
    public void testGetWithQuizes() throws Exception {

    }
    */


    @Test
    public void testGetSubCategories() throws Exception {
        CategoryDTO rootDTO1 = createCategory("root1");
        CategoryDTO rootDTO2 = createCategory("root2");
        createSubCategory(rootDTO1.id, "sub1");
        createSubCategory(rootDTO2.id, "sub2");
        createSubCategory(rootDTO2.id, "sub3");
        testGetSubCategories(3)
                .body("name", hasItems("sub1", "sub2", "sub3"));
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
    public void testGetSubWithGivenParentByCategory() throws Exception {
        CategoryDTO rootDTO = createCategory("root");
        createSubCategory(rootDTO.id, "sub1");
        createSubCategory(rootDTO.id, "sub2");
        CategoryDTO rootDTO2 = createCategory("root2");
        createSubCategory(rootDTO2.id, "sub3");

        given().pathParam("id", rootDTO.id)
                .get("/subcategories/parent/{id}")
                .then()
                .statusCode(200)
                .body("name", hasItems("sub1", "sub2"));
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
    
    private void testGetRootCategory(String id) {
        testGetCategory(CATEGORY_PATH, id);
    }

    private void testGetSubCategory(String id) {
        testGetCategory(SUBCATEGORY_PATH, id);
    }

    private void testGetCategory(String path, String id) {
        given().pathParam("id", id)
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