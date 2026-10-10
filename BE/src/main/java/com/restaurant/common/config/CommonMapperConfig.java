package com.restaurant.common.config;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * Cấu hình chung cho mọi mapper MapStruct. {@code ERROR} để thêm field vào entity hoặc DTO mà quên cập nhật
 * mapper thì build lỗi ngay, không âm thầm trả {@code null} (rule 2.7).
 */
@MapperConfig(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CommonMapperConfig {
}
