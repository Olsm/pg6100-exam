package org.pg6100.quizImp.dto;

import org.pg6100.quizApi.collection.ListDto;
import org.pg6100.quizApi.dto.QuizDTO;
import org.pg6100.quizApi.dto.SubCategoryDTO;
import org.pg6100.quizImp.datalayer.Quiz;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/*
    It might be tempting to use the @Entity objects directly as DTOs.
    That would be WRONG!!!
    It might work for small applications developed by one single person,
    but it is a huge problem (eg, maintainability and de-coupling) for
    larger projects.

    So here we need a converter from @Entity to DTO
 */
public class QuizConverter {

    private QuizConverter(){}

    public static QuizDTO transform(Quiz entity){
        Objects.requireNonNull(entity);
        QuizDTO dto = new QuizDTO();
        dto.id = String.valueOf(entity.getId());
        dto.category = new SubCategoryDTO(entity.getRootCategory().getId().toString(), entity.getSubCategory().getName());
        dto.question = entity.getQuestion();
        dto.answerList = entity.getAnswers();
        dto.correctAnswer = entity.getCorrectAnswer();

        return dto;
    }

    public static ListDto<QuizDTO> transform(List<Quiz> quizList){
        return  transform(quizList, 0, 100);
    }

    public static ListDto<QuizDTO> transform(List<Quiz> quizList, int offset, int limit){
        Objects.requireNonNull(quizList);

        List<QuizDTO> dtoList;
        dtoList = quizList.stream()
                .skip(offset)
                .limit(limit)
                .map(QuizConverter::transform)
                .collect(Collectors.toList());

        ListDto<QuizDTO> dto = new ListDto<>();
        dto.list = dtoList;
        dto._links = new ListDto.ListLinks();
        dto.rangeMin = offset;
        dto.rangeMax = dto.rangeMin + dtoList.size() - 1;
        dto.totalSize = quizList.size();

        return dto;
    }

}
