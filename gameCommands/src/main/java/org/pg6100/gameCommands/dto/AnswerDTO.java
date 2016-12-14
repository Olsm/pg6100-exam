package org.pg6100.gameCommands.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.pg6100.quizApi.dto.QuizDTO;

import java.util.List;

@ApiModel("A quiz game")
public class AnswerDTO extends BaseDTO {

    @ApiModelProperty("The chosen answer for the quiz")
    public String answerIndex;

    public AnswerDTO(){}

    public AnswerDTO(String id, String answerIndex) {
        this.id = id;
        this.answerIndex = answerIndex;
    }
}
