package org.pg6100.quizApi.api;

import io.swagger.annotations.*;
import io.swagger.jaxrs.PATCH;
import org.pg6100.quizApi.collection.ListDto;
import org.pg6100.quizApi.dto.QuizDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Api(value = "/quizzes" , description = "Handling of creating and retrieving quiz")
// when the url is "<base>/quiz", then this class will be used to handle it
@Path("/quizzes")
@Produces(MediaType.APPLICATION_JSON) // states that, when a method returns something, it is in Json
public interface QuizRestApi {

    String ID_PARAM ="The numeric id of the quiz";

    @ApiOperation("Get all the quiz")
    @GET
    ListDto<QuizDTO> get(
            @ApiParam("Offset in the list of news")
            @QueryParam("offset")
            @DefaultValue("0")
                    Integer offset,
            @ApiParam("Limit of news in a single retrieved page")
            @QueryParam("limit")
            @DefaultValue("10")
                    Integer limit,
            @ApiParam("filter quizzes by subcategory")
            @QueryParam("filter")
                    Long filter);

    @ApiOperation("Get a single quiz specified by id")
    @GET
    @Path("/{id}")
    QuizDTO getById(
            @ApiParam(ID_PARAM)
            @PathParam("id")
                    Long id);

    @ApiOperation("GET a quiz at random")
    @GET
    @Path("/random")
    QuizDTO getRandom();

    @ApiOperation("Get all the quiz in the specified category")
    @GET
    @Path("/categories/{id}")
    ListDto<QuizDTO> getByCategory(
            @ApiParam("The category id")
            @PathParam("id")
                    Long id);

    @ApiOperation("Create a quiz")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiResponse(code = 200, message = "The id of newly created quiz")
    Long createQuiz(
            @ApiParam("quiz question, answers and correct answer. Should not specify id or creation time")
                    QuizDTO dto);

    @ApiOperation("Update an existing quiz")
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    void update(
            @ApiParam(ID_PARAM)
            @PathParam("id")
                    Long id,
            @ApiParam("The quiz that will replace the old one. Cannot change its id though.")
                    QuizDTO dto);

    @Path("/{id}")
    @PATCH
    @Consumes("application/merge-patch+json")
    void update(@PathParam("id") Long id, String jsonPatch);

    @ApiOperation("Update the question of an existing quiz")
    @PUT
    @Path("/{id}/question")
    @Consumes(MediaType.TEXT_PLAIN)
    void updateQuestion(
            @ApiParam(ID_PARAM)
            @PathParam("id")
                    Long id,
            @ApiParam("The new question which will replace the old one")
                    String question
    );

    @ApiOperation("Delete a quiz with the given id")
    @DELETE
    @Path("/{id}")
    void delete(
            @ApiParam(ID_PARAM)
            @PathParam("id")
                    Long id);

    /* Deprecated methods */
    @ApiOperation("Get a single quiz specified by id")
    @ApiResponses({@ApiResponse(code = 301, message = "Deprecated URI. Moved permanently.")})
    @GET
    @Path("/id/{id}")
    @Deprecated
    Response deprecatedGetById(
            @ApiParam(ID_PARAM)
            @PathParam("id")
                    Long id);
}
