package org.pg6100.quizImp.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import org.pg6100.quizApi.api.QuizRestApi;
import org.pg6100.quizApi.collection.ListDto;
import org.pg6100.quizApi.dto.QuizDTO;
import org.pg6100.quizApi.hal.HalLink;
import org.pg6100.quizImp.businesslayer.CategoryEJB;
import org.pg6100.quizImp.businesslayer.QuizEJB;
import org.pg6100.quizImp.datalayer.Category;
import org.pg6100.quizImp.datalayer.Quiz;
import org.pg6100.quizImp.datalayer.SubCategory;
import org.pg6100.quizImp.dto.QuizConverter;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

/*
    The actual implementation could be a EJB, eg if we want to handle
    transactions and dependency injections with @EJB.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) //avoid creating new transactions
public class QuizRestImpl implements QuizRestApi {

    @EJB
    private QuizEJB QEJB;
    @EJB
    private CategoryEJB CEJB;
    @Context
    UriInfo uriInfo;

    @Override
    public ListDto<QuizDTO> get(Integer offset, Integer limit, Long filter) {
        if(offset < 0)
            throw new WebApplicationException("Negative offset: "+offset, 400);
        if(limit < 1)
            throw new WebApplicationException("Limit should be at least 1: "+limit, 400);

        int maxFromDb = 100;
        List<Quiz> quizList;

        if (filter != null) {
            if(!CEJB.subCatExists(filter)){
                throw new WebApplicationException("Cannot find subCategory with id " + filter, 404);
            }
            quizList = QEJB.getAllFromCategory(CEJB.getSubCategory(filter), maxFromDb);
        } else {
            quizList  = QEJB.getAll(maxFromDb);
        }

        if(offset != 0 && offset >=  quizList.size()){
            throw new WebApplicationException("Offset "+ offset + " out of bound "+quizList.size(), 400);
        }

        ListDto<QuizDTO> quizDTOList = QuizConverter.transform(quizList, offset, limit);

        UriBuilder builder = uriInfo.getBaseUriBuilder()
                .path("/quizzes")
                .queryParam("limit", limit);

        if(filter != null){
            builder = builder.queryParam("filter", filter);
        }

        quizDTOList._links.self = new HalLink(builder.clone()
                .queryParam("offset", offset)
                .build().toString()
        );

        if (!quizList.isEmpty() && offset > 0) {
            quizDTOList._links.previous = new HalLink(builder.clone()
                    .queryParam("offset", Math.max(offset - limit, 0))
                    .build().toString()
            );
        }
        if (offset + limit < quizList.size()) {
            quizDTOList._links.next = new HalLink(builder.clone()
                    .queryParam("offset", offset + limit)
                    .build().toString()
            );
        }

        return quizDTOList;
    }

    @Override
    public ListDto<QuizDTO> getByCategory(Long id) {
        if (! CEJB.subCatExists(id))
            throw new WebApplicationException("Cannot find category with name: " + id, 400);
        return QuizConverter.transform(QEJB.getAllFromCategory(CEJB.getSubCategory(id)));
    }

    @Override
    public Long createQuiz(QuizDTO dto) {
        if(dto.id != null){
            throw new WebApplicationException("Cannot specify id for a newly generated quiz", 400);
        }
        long categoryId = parseId(dto.category.id);
        if (!CEJB.subCatExists(categoryId)) {
            throw new WebApplicationException("sub sub category is invalid", 400);
        }

        Quiz quiz;
        try{
            SubCategory subCat = CEJB.getSubCategory(categoryId);
            quiz = QEJB.registerQuiz(subCat, dto.question, dto.answerList, dto.answerList.indexOf(dto.correctAnswer));
        }catch (Exception e){
            /*
                note: this work just because NOT_SUPPORTED,
                otherwise a rolledback transaction would propagate to the
                caller of this method
             */
            throw wrapException(e);
        }

        return quiz.getId();
    }

    @Override
    public QuizDTO getById(Long id) {
        return QuizConverter.transform(QEJB.getQuiz(id));
    }

    @Override
    public QuizDTO getRandom() {
        if (QEJB.getAll().size() == 0)
            throw  new  WebApplicationException("There must be at least 1 quiz to get a random quiz", 400);
        return QuizConverter.transform(QEJB.getRandomQuiz());
    }

    @Override
    public void update(Long pathId, QuizDTO dto) {
        if (dto.id == null || dto.question == null || dto.category == null || dto.answerList == null || dto.correctAnswer == null)
            throw new WebApplicationException("All parameters required, they cannot be null", 400);

        long id;
        try{
            id = Long.parseLong(dto.id);
        } catch (Exception e){
            throw new WebApplicationException("Invalid id: "+dto.id, 400);
        }

        // in this case, 409 (Conflict) sounds more appropriate than the generic 400
        if(id != pathId)
            throw new WebApplicationException("Not allowed to change the id of the resource", 409);

        requireQuiz(id);

        try {
            QEJB.update(id, dto.question, dto.answerList, dto.answerList.indexOf(dto.correctAnswer));
        } catch (Exception e){
            throw wrapException(e);
        }
    }

    @Override
    public void update(Long id, String jsonPatch) {
        if (! QEJB.isPresent(id))
            throw new WebApplicationException("Cannot find quiz with id " + id, 404);

        Quiz quiz = QEJB.getQuiz(id);
        String question = quiz.getQuestion();
        List<String> answers = quiz.getAnswers();
        String correctAnswer = Integer.toString(quiz.getAnswers().indexOf(quiz.getCorrectAnswer()));
        SubCategory subCategory = quiz.getSubCategory();

        ObjectMapper jackson = new ObjectMapper();
        JsonNode jsonNode;
        JsonNode arrNode;
        try {
            jsonNode = jackson.readValue(jsonPatch, JsonNode.class);
            arrNode = new ObjectMapper().readTree(jsonPatch);
        } catch (Exception e) {
            throw new WebApplicationException("Invalid JSON data as input: " + e.getMessage(), 400);
        }

        question = (String) jsonNodeExtract(jsonNode, "question", question);
        answers = (List<String>) jsonNodeExtract(arrNode, "answers", answers);
        correctAnswer = (String) jsonNodeExtract(jsonNode, "correctAnswer", correctAnswer);
        subCategory = (SubCategory) jsonNodeExtract(jsonNode, "subCategory", subCategory);

        if (jsonNode.has("answers")) {
            JsonNode node = arrNode.get("answers");
            if (node.isNull()) {
                answers = null;
            } else if (arrNode.isArray()) {
                answers = new ArrayList<>();
                for (final JsonNode objNode : arrNode) {
                    answers.add(objNode.asText());
                }
            } else {
                throw new WebApplicationException("Invalid JSON. Non-string question", 400);
            }
        }

        try {
            QEJB.update(quiz.getId(), question, answers, Integer.parseInt(correctAnswer));
        } catch (Exception e) {
            throw wrapException(e);
        }
    }

    private Object jsonNodeExtract(JsonNode jsonNode, String nodeKey, Object value) {
        if (jsonNode.has(nodeKey)) {
            JsonNode node = jsonNode.get(nodeKey);
            if (node.isNull()) {
                value = null;
            } else if (jsonNode.isArray()) {
                ArrayList<String> newValue = new ArrayList<>();
                for (final JsonNode objNode : jsonNode) {
                    newValue.add(objNode.asText());
                }
                value = newValue;
            } else if (node.isTextual()) {
                value = node.asText();
            } else {
                throw new WebApplicationException("Invalid JSON. Non-string question", 400);
            }
        }
        return value;
    }

    @Override
    public void updateQuestion(Long id, String question){
        requireQuiz(id);

        try {
            QEJB.updateQuizQuestion(id, question);
        } catch (Exception e){
            throw wrapException(e);
        }
    }

    @Override
    public void delete(Long id) {
        QEJB.deleteQuiz(id);
    }


    //----------------------------------------------------------

    private void requireQuiz(Long id) throws WebApplicationException {
        if (!QEJB.isPresent(id)) {
            throw new WebApplicationException("Cannot find quiz with id: " + id, 404);
        }
    }

    private long parseId(String id) {
        try{
            return Long.parseLong(id);
        } catch (Exception e){
            throw new WebApplicationException("Invalid id: " + id, 400);
        }
    }

    private WebApplicationException wrapException(Exception e) throws WebApplicationException{

        /*
            Errors:
            4xx: the user has done something wrong, eg asking for something that does not exist (404)
            5xx: internal server error (eg, could be a bug in the code)
         */

        Throwable cause = Throwables.getRootCause(e);
        if(cause instanceof ConstraintViolationException){
            return new WebApplicationException("Invalid constraints on input: "+cause.getMessage(), 400);
        } else {
            return new WebApplicationException("Internal error", 500);
        }
    }

    /* Deprecated methods */

    @Override
    public Response deprecatedGetById(Long id) {
        return Response.status(301)
                .location(UriBuilder.fromUri("quiz")
                        .queryParam("id", id).build())
                .build();
    }
}
