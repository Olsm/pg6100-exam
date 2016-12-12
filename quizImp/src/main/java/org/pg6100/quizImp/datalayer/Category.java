package org.pg6100.quizImp.datalayer;

import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Category {

    @Id @GeneratedValue
    private Long id;
    @Column(unique = true) @NotEmpty
    private String name;
    @OneToMany(fetch = FetchType.EAGER)
    private Set<SubCategory> subCategoryList;

    public Category() {}

    public Category(String name) {
        this.name = name;
        this.subCategoryList = new HashSet<>();
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

    public Set<SubCategory> getSubCategoryList() {
        return subCategoryList;
    }

    public void setSubCategoryList(Set<SubCategory> subCategoryList) {
        this.subCategoryList = subCategoryList;
    }

    public void addSubCategory(SubCategory category) {
        getSubCategoryList().add(category);
    }

    public void removeSubCategory(SubCategory category) {
        getSubCategoryList().remove(category);
    }
}
