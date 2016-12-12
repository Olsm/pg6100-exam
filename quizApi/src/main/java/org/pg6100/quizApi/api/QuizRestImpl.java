package org.pg6100.quizApi.api;

import com.google.common.base.Throwables;
import org.pg6100.quizApi.dto.QuizConverter;
import org.pg6100.quizApi.dto.QuizDTO;
import org.pg6100.quizImp.businesslayer.CategoryEJB;
import org.pg6100.quizImp.businesslayer.QuizEJB;
import org.pg6100.quizImp.datalayer.Quiz;
import org.pg6100.quizImp.datalayer.SubCategory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
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

    @Override
    public List<QuizDTO> get() {
        return QuizConverter.transform(QEJB.getAll());
    }

    @Override
    public List<QuizDTO> getByCategory(Long id) {
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
