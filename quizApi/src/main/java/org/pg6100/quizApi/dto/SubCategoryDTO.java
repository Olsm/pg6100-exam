package org.pg6100.quizApi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("A sub category")
public class SubCategoryDTO {

    @ApiModelProperty("The sub category id")
    public String id;

    @ApiModelProperty("The category name (id)")
    public String name;

    @ApiModelProperty("The root category")
    public String rootCategoryId;

    public SubCategoryDTO(){}

    public SubCategoryDTO(String rootCategoryId, String name) {
        this.rootCategoryId = rootCategoryId;
        this.name = name;
    }

    public SubCategoryDTO(String rootCategoryId, String id, String name) {
        this(rootCategoryId, name);
        this.id = id;
    }
}
