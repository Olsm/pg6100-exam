package org.pg6100.quizImp.businesslayer;

import org.pg6100.quizImp.datalayer.Quiz;
import org.pg6100.quizImp.datalayer.SubCategory;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Random;

@Stateless
public class QuizEJB {

    @PersistenceContext
    protected EntityManager em;

    public QuizEJB(){}

    public Quiz registerQuiz(SubCategory subCategory, String question, List<String> answerList, int correctAnswer){
        Quiz quiz = new Quiz(subCategory, question, answerList, correctAnswer);
        em.persist(quiz);
        subCategory.addQuiz(quiz);
        return quiz;
    }

    public boolean isPresent(Long quizId) {
        return em.find(Quiz.class, quizId) != null;
    }

    public Quiz getQuiz(Long id) {
        return em.find(Quiz.class, id);
    }

    public Quiz getRandomQuiz() {
        List<Quiz> quizzes = getAll();
        int randomIndex = new Random().nextInt(quizzes.size());
        return quizzes.get(randomIndex);
    }

    public int getNumberOfQuizes(){
        Query query = em.createNamedQuery(Quiz.SUM_QUIZES);
        return ((Number)query.getSingleResult()).intValue();
    }

    public List<Quiz> getAll() {
        return  getAll(1000);
    }

    public List<Quiz> getAll(int maxFromDb) {
        Query query = em.createQuery("SELECT q FROM Quiz q")
                .setMaxResults(maxFromDb);
        return (List<Quiz>) query.getResultList();
    }

    public List<Quiz> getAllFromCategory(SubCategory subCategory) {
        return getAllFromCategory(subCategory, 1000);
    }

    public List<Quiz> getAllFromCategory(SubCategory subCategory, int maxFromDb) {
        Query query = em.createQuery("SELECT q FROM Quiz q where q.subCategory = :category")
                .setMaxResults(maxFromDb);
        query.setParameter("category", subCategory);
        return (List<Quiz>) query.getResultList();
    }

    public void updateQuizCategory(Long quizId, SubCategory subCategory) {
        getQuiz(quizId).setSubCategory(subCategory);
    }

    public void updateQuizQuestion(Long quizId, String question) {
        getQuiz(quizId).setQuestion(question);
    }

    public void updateQuizAnswer(Long quizId, List<String> answerList) {
        getQuiz(quizId).setAnswers(answerList);
    }

    public void updateQuizCorrectAnswer(Long quizId, int correctAnswer) {
        getQuiz(quizId).setCorrectAnswer(correctAnswer);
    }

    public void deleteQuiz(Long quizId) {
        em.remove(getQuiz(quizId));
    }

    public boolean update(@NotNull Long quizId,
                          @NotNull String question,
                          @NotNull List<String> answerList,
                          @NotNull int correctAnswer) {
        Quiz quiz = getQuiz(quizId);
        if (quiz == null) {
            return false;
        }
        quiz.setQuestion(question);
        quiz.setAnswers(answerList);
        quiz.setCorrectAnswer(correctAnswer);
        return true;
    }
}
