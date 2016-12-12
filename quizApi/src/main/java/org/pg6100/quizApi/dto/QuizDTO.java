package org.pg6100.quizApi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/*
    A data transfer object (DTO) is what we will use to represent and (un)marshal
     JSon objects.
     Note: it is perfectly fine that fields here are all "public".
     This is just a POJO (plain old Java object), with no logic, just data.
     Also note how Swagger is used here to provide documentation.
 */


@ApiModel("A quiz")
public class QuizDTO {

    @ApiModelProperty("The id of the quiz")
    public String id;

    @ApiModelProperty("The category of the quiz")
    public SubCategoryDTO category;

    @ApiModelProperty("The question of the quiz")
    public String question;

    @ApiModelProperty("The answers of the quiz")
    public List<String> answerList;

    @ApiModelProperty("The correct answer of the quiz")
    public String correctAnswer;

    public QuizDTO(){}

    public QuizDTO(String id, SubCategoryDTO category, String question, List<String> answerList, String correctAnswer) {
        this.id = id;
        this.category = category;
        this.question = question;
        this.answerList = answerList;
        this.correctAnswer = correctAnswer;
    }
}
