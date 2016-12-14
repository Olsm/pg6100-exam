package org.pg6100.gameRest.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.mockito.stubbing.Answer;
import org.pg6100.gameCommands.dto.AnswerDTO;
import org.pg6100.gameCommands.dto.GameDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Api(description = "Handling of quiz game")
@Path("")
public interface GameRestApi {
    @ApiOperation("Get a random game")
    @GET
    @Path("/random")
    GameDTO get();

    @ApiOperation("Post an answer")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/games")
    boolean post(
            @ApiParam("Quiz Answer")
            @QueryParam("quizAnswer")
                    AnswerDTO quizAnswer);
}
