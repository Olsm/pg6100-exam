package org.pg6100.gameCommands.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import org.pg6100.gameCommands.dto.BaseDTO;
import org.pg6100.gameCommands.dto.GameDTO;
import org.pg6100.quizApi.dto.QuizDTO;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class CallQuiz extends HystrixCommand<Object> {
    private final UriBuilder base;
    private final Client client;
    private final Long id;

    public CallQuiz(Long id) {
        super(HystrixCommandGroupKey.Factory.asKey("Interactions with X"));
        this.id = id;
        base = UriBuilder.fromUri("http://localhost:8080/quiz/api/quizzes");
        client = ClientBuilder.newClient();
    }

    @Override
    protected Object run() throws Exception {

        /*
            Note: this synchronous call could fail (and so throw an exception),
            or even just taking a long while (if server is under heavy load)
         */
        URI uri;
        Object result;
        if (id == null) {
            uri = base.path("/random").build();
            result = client.target(uri).request()
                    .get()
                    .readEntity(GameDTO.class);
        } else {
            uri = base.path("/{id}").build();
            result = client.target(uri).request()
                    .get()
                    .readEntity(QuizDTO.class);
        }

        return result;
    }

    @Override
    protected Object getFallback() {
        //this is what is returned in case of exceptions or timeouts
        return null;
    }
}
