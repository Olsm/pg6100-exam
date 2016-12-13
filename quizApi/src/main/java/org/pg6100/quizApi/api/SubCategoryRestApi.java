package org.pg6100.quizApi.api;

import io.swagger.annotations.*;
import org.pg6100.quizApi.dto.SubCategoryDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

@Api(value = "/subcategories", description = "Handling of creating and retrieving sub categories")
@Path("/subcategories")
@Produces(MediaType.APPLICATION_JSON)
public interface SubCategoryRestApi {

    String ID_PARAM = "The numeric id of the category";

    @ApiOperation("Get all the sub categories")
    @GET
    Set<SubCategoryDTO> get(
            @ApiParam(value = "Only the subcategories in the given category specified by parentId")
            @QueryParam("parentId")
                    Long parentId);

    @ApiOperation("Get subcategory by id")
    @GET
    @Path("/{id}")
    SubCategoryDTO getSubCategoryById(
            @ApiParam(ID_PARAM)
            @PathParam("id")
                    Long id);

    @ApiOperation("Create a subcategory")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiResponse(code = 200, message = "The id of the new subcategory")
    Long createSubCategory(
            @ApiParam("Category id and root category")
                    SubCategoryDTO dto,
            @ApiParam("Root category id")
                    Long parentId);

    @ApiOperation("Update sub category by id")
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    void updateSubCategory(
            @ApiParam(ID_PARAM)
            @PathParam("id")
                    Long id,
            @ApiParam("The sub category that will replace the old one")
                    SubCategoryDTO dto);

    /* TODO: Patch
    @ApiOperation("")
    @PATCH
    @Path("/categories/id/{id}");
    */

    @ApiOperation("Delete a sub category with given id")
    @DELETE
    @Path("/{id}")
    void deleteSubCategory(
            @ApiParam(ID_PARAM)
            @PathParam("id")
                    Long id);
}
