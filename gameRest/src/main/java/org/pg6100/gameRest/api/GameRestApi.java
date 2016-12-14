package org.pg6100.gameRest.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.pg6100.gameCommands.dto.GameDTO;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Api(description = "Handling of quiz game")
@Path("")
public interface GameRestApi {
    @ApiOperation("Get a random game")
    @GET
    @Path("/random")
    GameDTO get();

    /*
    @ApiOperation("Post an answer")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/games")
    boolean post(
            @ApiParam("Quiz Id")
                    Long quizId,
            @ApiParam("Index of chosen answer")
                    int chosenAnswer);
    */
}
