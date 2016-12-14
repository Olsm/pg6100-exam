package org.pg6100.gameSoap.api;

import org.pg6100.gameCommands.dto.AnswerDTO;
import org.pg6100.gameCommands.dto.GameDTO;
import org.pg6100.gameCommands.hystrix.CallQuiz;
import org.pg6100.quizApi.dto.QuizDTO;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.WebService;

@WebService(
        endpointInterface = "org.pg6100.gameSoap.api.GameSoapApi"
)
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class GameSoapImpl implements GameSoapApi {

    @Override
    public GameDTO get() {
        GameDTO dto = (GameDTO) new CallQuiz(null).execute();
        if (dto != null)
            return dto;
        else
            return null;
    }

    @Override
    public boolean post(AnswerDTO quizAnswer) {
        int chosenAnswer = 0;
        long quizId = 0;
        try{
            quizId = Long.parseLong(quizAnswer.id);
            chosenAnswer = Integer.parseInt(quizAnswer.answerIndex);
            QuizDTO dto = (QuizDTO) new CallQuiz(quizId).execute();
            return dto.question.indexOf(dto.correctAnswer) == chosenAnswer;
        } catch (Exception e){
            // Do error handling
        }
        return false;
    }


}
