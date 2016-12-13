package org.pg6100.quizApi.api;

import io.swagger.annotations.*;
import io.swagger.jaxrs.PATCH;
import org.pg6100.quizApi.dto.CategoryDTO;
import org.pg6100.quizApi.dto.SubCategoryDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

@Api(value = "/categories", description = "Handling of creating and retrieving categories")
@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
public interface RootCategoryRestApi {

    String ID_PARAM = "The numeric id of the category";

    @ApiOperation("Get all the categories")
    @GET
    Set<CategoryDTO> get(
            @ApiParam("Root categories with quizes")
            @QueryParam("expand")
                    boolean expand
    );

    @ApiOperation("Get category by id)")
    @GET
    @Path("/{id}")
    CategoryDTO getRootCategoryById(
            @ApiParam(ID_PARAM)
            @PathParam("id")
                    Long id,
            @QueryParam("expand")
                    boolean expand);

    @ApiOperation("Create a category")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiResponse(code = 200, message = "The id of the new category")
    Long createRootCategory(
            @ApiParam("Category name")
                    CategoryDTO dto);

    @ApiOperation("Update category by id")
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    void updateRootCategory(
            @ApiParam(ID_PARAM)
            @PathParam("id")
                    Long id,
            @ApiParam("The category that will replace the old one")
                    CategoryDTO dto);

    @Path("/{id}")
    @PATCH
    @Consumes("application/merge-patch+json")
    public void updateRootCategory(@PathParam("id") Long id, String jsonPatch);

    @ApiOperation("Delete a category with given id (name)")
    @DELETE
    @Path("/{id}")
    void deleteRootCategory(
            @ApiParam(ID_PARAM)
            @PathParam("id")
                    Long id);



    /* Deprecated methods */

    @ApiOperation("GET all subcategories of the category specified by id")
    @GET
    @ApiResponses({@ApiResponse(code = 301, message = "Deprecated URI. Moved permanently.")})
    @Path("/{parentId}/subcategories")
    @Deprecated
    Response deprecatedGetSubCategoriesByRootCategory(
            @ApiParam("The root category id")
            @PathParam("parentId")
                    Long id);

    @ApiOperation("Create a new subcategory whose parent is the category specified by id")
    @POST
    @ApiResponses({@ApiResponse(code = 301, message = "Deprecated URI. Moved permanently.")})
    @Path("/{parentId}/subcategories")
    @Deprecated
    Response deprecatedCreateSubCategoryByParent(
            @ApiParam("The root category id")
            @PathParam("parentId")
                    Long id,
            @ApiParam("Category name")
                    SubCategoryDTO dto);

}
