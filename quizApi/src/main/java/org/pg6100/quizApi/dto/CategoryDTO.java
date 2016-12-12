package org.pg6100.quizApi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("A category")
public class CategoryDTO {

    @ApiModelProperty("The root category id")
    public String id;

    @ApiModelProperty("The category name)")
    public String name;

    public CategoryDTO(){}

    public CategoryDTO(String name) {
        this.name = name;
    }

    public CategoryDTO(String id, String name) {
        this(name);
        this.id = id;
    }
}
