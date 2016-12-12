package org.pg6100.quizImp.datalayer;

import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class SubCategory {

    @Id @GeneratedValue
    private Long id;
    @Column(unique = true) @NotEmpty
    private String name;
    @ManyToOne
    private Category rootCategory;
    @OneToMany(fetch = FetchType.EAGER)
    private Set<Quiz> quizList;

    public SubCategory() {}

    public SubCategory(Category rootCategory, String name) {
        this.rootCategory = rootCategory;
        this.name = name;
        this.quizList = new HashSet<>();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String category) {
        this.name = category;
    }

    public Category getRootCategory() {
        return rootCategory;
    }

    public void setRootCategory(Category rootCategory) {
        this.rootCategory = rootCategory;
    }

    public Set<Quiz> getQuizList() {
        return quizList;
    }

    public void setQuizList(Set<Quiz> quizList) {
        this.quizList = quizList;
    }

    public void addQuiz(Quiz quiz) {
        getQuizList().add(quiz);
    }
}
