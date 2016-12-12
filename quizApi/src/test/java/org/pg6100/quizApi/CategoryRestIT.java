package org.pg6100.quizApi;

import io.restassured.http.ContentType;
import org.junit.Test;
import org.pg6100.quizApi.dto.CategoryDTO;
import org.pg6100.quizApi.dto.SubCategoryDTO;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.Is.is;

public class CategoryRestIT extends CategoryRestTestBase {

    @Test
    public void testCleanDB() {

        get("/categories").then()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    public void testCreateAndGetRootCategory() {
        get("/categories").then().statusCode(200).body("size()", is(0));

        CategoryDTO dto = createCategoryDTO("name");
        get("/categories").then().statusCode(200).body("size()", is(1));

        testGetCategory("/categories", dto.id);
    }

    @Test
    public void testCreateAndGetSubCategory() {
        CategoryDTO rootDTO = createCategoryDTO("name");

        get("/subcategories").then().statusCode(200).body("size()", is(0));

        SubCategoryDTO dto = createSubCategoryDTO(rootDTO.id, "name");
        get("/subcategories").then().statusCode(200).body("size()", is(1));
        testGetCategory("/subcategories", dto.id);
    }

    @Test
    public void testGetRootCategories() throws Exception {
        CategoryDTO dto1 = createCategoryDTO("name1");
        CategoryDTO dto2 = createCategoryDTO("name2");
        get("/categories").then().statusCode(200).body("size()", is(2))
            .body("name", hasItems("name1", "name2"));
    }

    @Test
    public void testUpdateRootCategory() throws Exception {
        CategoryDTO dto = createCategoryDTO("name1");
        dto.name = "name2";
        updateCategory(dto, dto.id, "/categories");
        testGetCategory("/categories", dto.id);
    }

    @Test
    public void testDeleteRootCategory() throws Exception {
        CategoryDTO dto = createCategoryDTO("name");
        deleteCategory(dto.id, "/categories");
        get("/categories").then().statusCode(200).body("size()", is(0));
    }

    /* TODO
    @Test
    public void testGetWithQuizes() throws Exception {

    }
    */


    @Test
    public void testGetSubCategories() throws Exception {
        CategoryDTO dto = createCategoryDTO("name");
        SubCategoryDTO dto1 = createSubCategoryDTO(dto.id, "name1");
        SubCategoryDTO dto2 = createSubCategoryDTO(dto.id, "name2");
        get("/subcategories").then().statusCode(200).body("size()", is(2))
                .body("name", hasItems("name1", "name2"));
    }

    @Test
    public void testUpdateSubCategory() throws Exception {
        CategoryDTO rootDTO = createCategoryDTO("root");
        SubCategoryDTO subDTO = createSubCategoryDTO(rootDTO.id, "name1");
        subDTO.name = "name2";
        updateCategory(subDTO, subDTO.id, "/subcategories");
        testGetCategory("/subcategories", subDTO.id);
    }

    @Test
    public void testDeleteSubCategory() throws Exception {
        CategoryDTO dto = createCategoryDTO("name");
        SubCategoryDTO subDTO = createSubCategoryDTO(dto.id, "name");
        deleteCategory(subDTO.id, "/subcategories");
        get("/subcategories").then().statusCode(200).body("size()", is(0));
    }

    @Test
    public void testGetSubCategoriesByRootCategory() throws Exception {
        CategoryDTO rootDTO = createCategoryDTO("root");
        CategoryDTO rootDTO2 = createCategoryDTO("root2");
        SubCategoryDTO subDTO = createSubCategoryDTO(rootDTO.id, "sub1");
        SubCategoryDTO subDTO2 = createSubCategoryDTO(rootDTO.id, "sub2");
        SubCategoryDTO subDTO3 = createSubCategoryDTO(rootDTO2.id, "sub3");

        given().pathParam("id", rootDTO.id)
                .get("/categories/{id}/subcategories")
                .then()
                .statusCode(200)
                .body("name", hasItems("sub1", "sub2"));
    }

    @Test
    public void testGetSubWithGivenParentByCategory() throws Exception {
        CategoryDTO rootDTO = createCategoryDTO("root");
        createSubCategoryDTO(rootDTO.id, "sub1");
        createSubCategoryDTO(rootDTO.id, "sub2");
        CategoryDTO rootDTO2 = createCategoryDTO("root2");
        createSubCategoryDTO(rootDTO2.id, "sub3");

        given().pathParam("id", rootDTO.id)
                .get("/subcategories/parent/{id}")
                .then()
                .statusCode(200)
                .body("name", hasItems("sub1", "sub2"));
    }


    private CategoryDTO createCategoryDTO(String name) {
        CategoryDTO dto = new CategoryDTO(name);
        dto.id = registerCategory(dto, "/categories");
        return dto;
    }

    private SubCategoryDTO createSubCategoryDTO(String rootCategoryId, String name) {
        SubCategoryDTO dto = new SubCategoryDTO(rootCategoryId, name);
        dto.id = registerCategory(dto, "/subcategories");
        return dto;
    }

    private void testGetCategory(String path, String id) {
        given().pathParam("id", id)
                .get(path + "/{id}")
                .then()
                .statusCode(200)
                .body("id", is(id));
    }


    private String registerCategory(Object dto, String path) {
        return given().contentType(ContentType.JSON)
                .body(dto)
                .post(path)
                .then()
                .statusCode(200)
                .extract().asString();
    }

    private void updateCategory(Object dto, String id, String path) {
        given().contentType(ContentType.JSON)
                .pathParam("id", id)
                .body(dto)
                .put(path + "/{id}")
                .then()
                .statusCode(204);
    }

    private void deleteCategory(String id, String path) {
        given().contentType(ContentType.JSON)
                .pathParam("id", id)
                .delete(path + "/{id}")
                .then()
                .statusCode(204);
    }
}