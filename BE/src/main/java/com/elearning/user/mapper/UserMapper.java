package com.elearning.user.mapper;

import com.elearning.common.config.CommonMapperConfig;
import com.elearning.user.dto.request.ProfileUpdateRequest;
import com.elearning.user.dto.request.TeacherCreateRequest;
import com.elearning.user.dto.request.TeacherUpdateRequest;
import com.elearning.user.dto.response.ProfileResponse;
import com.elearning.user.dto.response.UserBriefResponse;
import com.elearning.user.dto.response.UserDetailResponse;
import com.elearning.user.dto.response.UserSummaryResponse;
import com.elearning.user.entity.User;
import com.elearning.user.repository.UserBriefView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CommonMapperConfig.class)
public interface UserMapper {

    ProfileResponse toProfileResponse(User user);

    UserSummaryResponse toSummaryResponse(User user);

    UserDetailResponse toDetailResponse(User user);

    UserBriefResponse toBriefResponse(User user);

    UserBriefResponse toBriefResponse(UserBriefView view);

    // id, mật khẩu băm và role do service gán; email do service chuẩn hóa chữ thường.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    User toNewTeacher(TeacherCreateRequest request);

    // Trạng thái và mật khẩu do service xử lý riêng vì kéo theo khóa token và băm mật khẩu.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateTeacher(TeacherUpdateRequest request, @MappingTarget User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateProfile(ProfileUpdateRequest request, @MappingTarget User user);
}
