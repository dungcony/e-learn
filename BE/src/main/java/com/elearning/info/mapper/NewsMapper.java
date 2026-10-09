package com.elearning.info.mapper;

import com.elearning.common.config.CommonMapperConfig;
import com.elearning.info.dto.request.NewsCreateRequest;
import com.elearning.info.dto.request.NewsUpdateRequest;
import com.elearning.info.dto.response.NewsDetailResponse;
import com.elearning.info.dto.response.NewsSummaryResponse;
import com.elearning.info.entity.News;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CommonMapperConfig.class)
public interface NewsMapper {

    NewsSummaryResponse toSummaryResponse(News news);

    NewsDetailResponse toDetailResponse(News news);

    // id, người đăng và mốc thời gian do service gán.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    News toNews(NewsCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateNews(NewsUpdateRequest request, @MappingTarget News news);
}
