package com.elearning.course.mapper;

import com.elearning.common.config.CommonMapperConfig;
import com.elearning.course.dto.response.CategoryRefResponse;
import com.elearning.course.dto.response.CategoryResponse;
import com.elearning.course.entity.Category;
import org.mapstruct.Mapper;

@Mapper(config = CommonMapperConfig.class)
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);

    CategoryRefResponse toRefResponse(Category category);
}
