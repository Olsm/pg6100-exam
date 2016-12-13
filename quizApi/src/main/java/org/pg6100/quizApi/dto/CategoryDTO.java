package org.pg6100.quizApi.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Set;

@ApiModel("A category")
public class CategoryDTO {

    @ApiModelProperty("The root category id")
    public String id;

    @ApiModelProperty("The category name")
    public String name;

    @ApiModelProperty("The sub categories (available if expand true)")
    public Set<SubCategoryDTO> subCategories;

    public CategoryDTO(){}

    public CategoryDTO(String name) {
        this.name = name;
    }

    public CategoryDTO(String id, String name) {
        this(name);
        this.id = id;
        this.subCategories = null;
    }
}
