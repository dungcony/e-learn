package com.elearning.common.storage;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Cấu hình nơi lưu ảnh tải lên.
 *
 * @param baseDir    thư mục gốc trên máy chủ; tương đối thì tính từ thư mục chạy ứng dụng
 * @param publicPath đường dẫn URL mà file được phục vụ công khai, vd {@code /files}
 */
@Validated
@ConfigurationProperties(prefix = "app.storage")
public record StorageProperties(@NotBlank String baseDir, @NotBlank String publicPath) {
}
