package org.pg6100.quizImp.businesslayer;

import org.pg6100.quizImp.datalayer.Category;
import org.pg6100.quizImp.datalayer.SubCategory;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Stateless
public class CategoryEJB {

    @PersistenceContext
    protected EntityManager em;

    public CategoryEJB(){}

    public Category registerRootCategory(String name) {
        Category rootCategory = new Category(name);
        em.persist(rootCategory);
        return rootCategory;
    }

    public SubCategory registerSubCategory(Category rootCategory, String name) {
        rootCategory = getRootCategory(rootCategory.getId());
        SubCategory subCategory = new SubCategory(rootCategory, name);
        em.persist(subCategory);
        rootCategory.addSubCategory(subCategory);
        return subCategory;
    }

    public Category getRootCategory(Long id) {
        return em.find(Category.class, id);
    }

    public SubCategory getSubCategory(Long id) {
        return em.find(SubCategory.class, id);
    }

    public boolean rootCatExists(Long id) {
        return getRootCategory(id) != null;
    }
    public boolean subCatExists(Long id) {
        return getSubCategory(id) != null;
    }

    public boolean updateRootCategory(@NotNull Long id, @NotNull String newCategory) {
            Category rootCategory = getRootCategory(id);
            if (rootCategory == null) {
                return false;
            }
            rootCategory.setName(newCategory);
            return true;
    }

    public boolean updateSubCategory(@NotNull Long id, @NotNull String newCategory, @NotNull Long rootCategoryId) {
        Category rootCat = getRootCategory(rootCategoryId);
        SubCategory subCat = getSubCategory(id);
        if (rootCat == null || subCat == null) {
            return false;
        }
        subCat.setName(newCategory);
        subCat.setRootCategory(rootCat);
        return true;
    }

    public void deleteRootCategory(@NotNull Long id) {
        em.remove(getRootCategory(id));
    }

    public void deleteSubCategory(@NotNull Long id) {
        SubCategory subCategory = getSubCategory(id);
        getRootCategory(subCategory.getRootCategory().getId()).removeSubCategory(subCategory);
        em.remove(subCategory);
    }

    public Set<Category> getAllRootCategories() {
        Query query = em.createQuery("Select c FROM Category c");
        return new HashSet<>(query.getResultList());
    }

    public Set<SubCategory> getAllSubCategories() {
        Query query = em.createQuery("Select c FROM SubCategory c");
        return new HashSet<>(query.getResultList());
    }

    public Set<SubCategory> getSubCategoriesWithQuizes() {
        Set<SubCategory> subCategories = getAllSubCategories();
        Set<SubCategory> subCategoriesWithQuizes = new HashSet<>();

        for (SubCategory subCategory : subCategories) {
            if (subCategory.getQuizList().size() > 0) {
                subCategoriesWithQuizes.add(subCategory);
            }
        }

        return subCategoriesWithQuizes;
    }
}
