package org.pg6100.quizApi.api;

import io.swagger.annotations.*;
import org.pg6100.quizApi.dto.QuizDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/*
    When defining a REST API, there is going to be a lot of annotations that
    are required. To make things more readable, it is a good practice to
    have the API definition (with all the annotations) in an interface,
    and the actual internal logic in a concrete class.
 */


@Api(value = "/quizzes" , description = "Handling of creating and retrieving quiz")
// when the url is "<base>/quiz", then this class will be used to handle it
@Path("/quizzes")
@Produces(MediaType.APPLICATION_JSON) // states that, when a method returns something, it is in Json
public interface QuizRestApi {

    /*
        Main HTTP verbs/methods:
        GET: get the resource specified in the URL
        POST: send data, creating a new resource
        PUT: update a resource
        DELETE: delete the resource
     */

    /*
        Note: here inputs (what is in the method parameters) and outputs will
        be automatically processed by Wildfly using its own Json library, eg Jackson.
        So, when we have
        "List<quizDto>"
        as return value, Wildfly will automatically marshall it into Json
     */

    //note: in interfaces, this is by default "public static final"
    String ID_PARAM ="The numeric id of the quiz";


    @ApiOperation("Get all the quiz")
    @GET
    List<QuizDTO> get();

    @ApiOperation("Get a single quiz specified by id")
    @GET
    @Path("/{id}")
    QuizDTO getById(
            @ApiParam(ID_PARAM)
            @PathParam("id")
                    Long id);


    /*
        NOTE: in the following, we use the URI path to
        identify the subsets that we want, like "country"
        and "author". This does work, but is NOT fully correct.
        Later, we will go back on this point once we discuss
        URI parameters.
     */

    @ApiOperation("Get all the quiz in the specified category")
    @GET
    @Path("/categories/{id}")
    List<QuizDTO> getByCategory(
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

        /*
            PUT is idempotent (ie, applying 1 or 1000 times should end up in same result on the server).
            However, it will replace the whole resource (quiz) in this case.

            In some cases, a PUT on an non-existing resource might create it.
            This depends on the application.
            Here, as the id is what automatically generate by Hibernate,
            we will not allow it
         */

    @ApiOperation("Update an existing quiz")
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    void update(
            @ApiParam(ID_PARAM)
            @PathParam("id")
                    Long id,
            //
            @ApiParam("The quiz that will replace the old one. Cannot change its id though.")
                    QuizDTO dto);


    /*
        If we only want to update the text, using the method above can be inefficient, as we
        have to send again the WHOLE quiz. Partial updates are wrong.
        But, we can have a new resource specifying the content of the quiz,
        which then would be allowed to update with a PUT.

        Another approach is to use PATCH, but likely on overkill here...
     */

    @ApiOperation("Update the question of an existing quiz")
    @PUT
    @Path("/{id}/question")
    @Consumes(MediaType.TEXT_PLAIN)
    void updateQuestion(
            @ApiParam(ID_PARAM)
            @PathParam("id")
                    Long id,
            //
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
