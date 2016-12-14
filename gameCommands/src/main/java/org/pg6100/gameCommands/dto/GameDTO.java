package org.pg6100.gameCommands.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.pg6100.quizApi.dto.QuizDTO;

import java.util.List;

@ApiModel("A quiz game")
public class GameDTO extends BaseDTO {

    @ApiModelProperty("The question of the quiz")
    public String question;

    @ApiModelProperty("The answers of the quiz")
    public List<String> answerList;

    public GameDTO(){}

    public GameDTO(String id, String question, List<String> answerList, String correctAnswer) {
        this.id = id;
        this.question = question;
        this.answerList = answerList;
    }
}
