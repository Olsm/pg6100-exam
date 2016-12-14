package org.pg6100.gameRest.api;

import org.pg6100.gameCommands.dto.AnswerDTO;
import org.pg6100.gameCommands.dto.GameDTO;
import org.pg6100.gameCommands.hystrix.CallQuiz;
import org.pg6100.quizApi.dto.QuizDTO;

import javax.ws.rs.WebApplicationException;

public class GameRestImpl implements GameRestApi {

    @Override
    public GameDTO get() {
        GameDTO dto = (GameDTO) new CallQuiz(null).execute();
        if (dto == null)
            throwException("Could not get game", 500);
        return dto;
    }

    @Override
    public boolean post(AnswerDTO quizAnswer) {
        int chosenAnswer = 0;
        long quizId = 0;
        try{
            quizId = Long.parseLong(quizAnswer.id);
            chosenAnswer = Integer.parseInt(quizAnswer.answerIndex);
        } catch (Exception e){
            throwException("Invalid id: " + quizAnswer.id, 400);
        }

        if (quizId < 0)
            throwException("Invalid quiz id: " + quizId, 400);
        if (chosenAnswer < 1 || chosenAnswer > 4)
            throwException("Invalid answer, must be between 1-4 " + chosenAnswer, 400);

        QuizDTO dto = (QuizDTO) new CallQuiz(quizId).execute();
        if (dto == null)
            throwException("Could not get answer for quiz with id " + quizId, 500);

        return dto.question.indexOf(dto.correctAnswer) == chosenAnswer;
    }

    private void throwException(String message, int code) {
        throw new WebApplicationException(message, code);
    }
}
