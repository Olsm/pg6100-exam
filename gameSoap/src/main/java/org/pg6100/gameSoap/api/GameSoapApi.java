package org.pg6100.gameSoap.api;

import org.pg6100.gameCommands.dto.AnswerDTO;
import org.pg6100.gameCommands.dto.GameDTO;

import javax.jws.WebService;
import java.util.List;

@WebService( name = "GameSoap")
public interface GameSoapApi {

    GameDTO get();

    boolean post(AnswerDTO quizAnswer);

}
