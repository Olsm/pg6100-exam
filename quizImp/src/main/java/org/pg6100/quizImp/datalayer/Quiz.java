package org.pg6100.quizImp.datalayer;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@NamedQueries({
        @NamedQuery(name = Quiz.SUM_QUIZES, query = "select count(q) from Quiz q"),
})

@Entity
public class Quiz {

    public static final String SUM_QUIZES = "SUM_QUIZES";

    @Id @GeneratedValue
    private Long id;
    @NotNull @ManyToOne
    private SubCategory subCategory;
    @NotEmpty
    private String question;
    @NotEmpty
    private String answer1;
    @NotEmpty
    private String answer2;
    @NotEmpty
    private String answer3;
    @NotEmpty
    private String answer4;
    @Range(max = 4, min = 1)
    private int correctAnswer;

    public Quiz() {}

    public Quiz(SubCategory category, String question, List<String> answerList, int correctAnswer) {
        this.subCategory = category;
        this.question = question;
        this.answer1 = answerList.get(0);
        this.answer2 = answerList.get(1);
        this.answer3 = answerList.get(2);
        this.answer4 = answerList.get(3);
        this.correctAnswer = correctAnswer;
    }

    public Long getId() {
        return id;
    }

    public Category getRootCategory() {
        return getSubCategory().getRootCategory();
    }

    public SubCategory getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(SubCategory subCategory) {
        this.subCategory = subCategory;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getAnswers() {
        ArrayList<String> answerList = new ArrayList<>();
        answerList.add(answer1);
        answerList.add(answer2);
        answerList.add(answer3);
        answerList.add(answer4);
        return answerList;
    }

    public void setAnswers(List<String> answerList) {
        this.answer1 = answerList.get(0);
        this.answer2 = answerList.get(1);
        this.answer3 = answerList.get(2);
        this.answer4 = answerList.get(3);
    }

    public String getCorrectAnswer() {
        return getAnswers().get(correctAnswer);
    }

    public void setCorrectAnswer(int correctAnswer) {
        this.correctAnswer = --correctAnswer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!Quiz.class.isAssignableFrom(obj.getClass()))
            return false;
        final Quiz other = (Quiz) obj;

        boolean sameId = (this.getId() == null) ? other.getId() == null : this.getId().equals(other.getId());
        boolean sameQAndA = (Objects.equals(this.getQuestion(), other.getQuestion()) &&
                this.getAnswers() == other.getAnswers() && Objects.equals(this.getCorrectAnswer(), other.getCorrectAnswer()));

        return sameId || sameQAndA;
    }
}
