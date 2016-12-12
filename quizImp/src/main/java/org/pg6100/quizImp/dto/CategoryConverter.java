package org.pg6100.quizImp.dto;

import org.pg6100.quizApi.dto.CategoryDTO;
import org.pg6100.quizApi.dto.SubCategoryDTO;
import org.pg6100.quizImp.datalayer.Category;
import org.pg6100.quizImp.datalayer.SubCategory;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class CategoryConverter {

    private CategoryConverter() {}

    public static CategoryDTO transform(Category category) {
        Objects.requireNonNull(category);
        return new CategoryDTO(category.getId().toString(), category.getName());
    }

    public static SubCategoryDTO transform(SubCategory category) {
        return new SubCategoryDTO(category.getRootCategory().getId().toString(), category.getId().toString(), category.getName());
    }

    public static Set<CategoryDTO> transformCategories(Set<Category> categories) {
        Objects.requireNonNull(categories);
        return categories.stream()
                .map(CategoryConverter::transform)
                .collect(Collectors.toSet());
    }

    public static Set<SubCategoryDTO> transformSubCategories(Set<SubCategory> categories) {
        Objects.requireNonNull(categories);
        return categories.stream()
                .map(CategoryConverter::transform)
                .collect(Collectors.toSet());
    }
}
