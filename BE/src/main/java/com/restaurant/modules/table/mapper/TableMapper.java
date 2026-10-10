package com.restaurant.modules.table.mapper;

import com.restaurant.common.config.CommonMapperConfig;
import com.restaurant.modules.table.dto.request.TableCreateRequest;
import com.restaurant.modules.table.dto.request.TableUpdateRequest;
import com.restaurant.modules.table.dto.response.TableBriefResponse;
import com.restaurant.modules.table.dto.response.TableResponse;
import com.restaurant.modules.table.entity.DiningTable;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CommonMapperConfig.class)
public interface TableMapper {

    TableResponse toResponse(DiningTable table);

    TableBriefResponse toBriefResponse(DiningTable table);

    // id và trạng thái ban đầu do service gán
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "zone", source = "zone")
    @Mapping(target = "capacity", source = "capacity")
    @Mapping(target = "note", source = "note")
    DiningTable fromCreateRequest(TableCreateRequest request);

    // Trạng thái cần kiểm tra quy tắc trước nên do service gán
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "zone", source = "zone")
    @Mapping(target = "capacity", source = "capacity")
    @Mapping(target = "note", source = "note")
    void updateFromRequest(TableUpdateRequest request, @MappingTarget DiningTable table);
}
