package com.elearning.course.dto.response;

import java.util.UUID;

// Thể loại rút gọn nhúng trong chi tiết khóa học.
public record CategoryRefResponse(UUID id, String name) {
}
