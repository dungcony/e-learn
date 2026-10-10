package com.restaurant.modules.reservation.mapper;

import com.restaurant.common.config.CommonMapperConfig;
import com.restaurant.modules.reservation.dto.request.ReservationCreateRequest;
import com.restaurant.modules.reservation.dto.response.ReservationResponse;
import com.restaurant.modules.reservation.dto.response.ReservationSummaryResponse;
import com.restaurant.modules.reservation.entity.Reservation;
import com.restaurant.modules.table.dto.response.TableBriefResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public interface ReservationMapper {

    // Bàn lấy từ module bàn nên truyền vào riêng; id và status có ở cả hai nguồn nên chỉ rõ lấy từ đặt bàn
    @Mapping(target = "id", source = "reservation.id")
    @Mapping(target = "status", source = "reservation.status")
    @Mapping(target = "table", source = "table")
    ReservationResponse toResponse(Reservation reservation, TableBriefResponse table);

    @Mapping(target = "tableName", source = "tableName")
    ReservationSummaryResponse toSummaryResponse(Reservation reservation, String tableName);

    // Mã, khung giờ kết thúc, trạng thái, khách hàng do service gán
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "guestName", source = "guestName")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "reservedAt", source = "reservedAt")
    @Mapping(target = "guestCount", source = "guestCount")
    @Mapping(target = "preferredZone", source = "preferredZone")
    @Mapping(target = "note", source = "note")
    Reservation fromCreateRequest(ReservationCreateRequest request);
}
