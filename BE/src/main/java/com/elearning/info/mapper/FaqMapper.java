package com.elearning.info.mapper;

import com.elearning.common.config.CommonMapperConfig;
import com.elearning.info.dto.request.FaqCreateRequest;
import com.elearning.info.dto.request.FaqUpdateRequest;
import com.elearning.info.dto.response.FaqResponse;
import com.elearning.info.entity.Faq;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CommonMapperConfig.class)
public interface FaqMapper {

    FaqResponse toResponse(Faq faq);

    // id, người tạo và mốc thời gian do service gán.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Faq toFaq(FaqCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateFaq(FaqUpdateRequest request, @MappingTarget Faq faq);
}
