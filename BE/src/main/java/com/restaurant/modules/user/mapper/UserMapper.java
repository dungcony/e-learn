package com.restaurant.modules.user.mapper;

import com.restaurant.common.config.CommonMapperConfig;
import com.restaurant.modules.user.dto.request.ProfileUpdateRequest;
import com.restaurant.modules.user.dto.request.StaffCreateRequest;
import com.restaurant.modules.user.dto.request.StaffUpdateRequest;
import com.restaurant.modules.user.dto.request.UserRegisterRequest;
import com.restaurant.modules.user.dto.response.AuthUserResponse;
import com.restaurant.modules.user.dto.response.ProfileResponse;
import com.restaurant.modules.user.dto.response.UserDetailResponse;
import com.restaurant.modules.user.dto.response.UserSummaryResponse;
import com.restaurant.modules.user.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CommonMapperConfig.class)
public interface UserMapper {

    ProfileResponse toProfileResponse(User user);

    AuthUserResponse toAuthUserResponse(User user);

    UserSummaryResponse toSummaryResponse(User user);

    UserDetailResponse toDetailResponse(User user);

    // Chỉ các trường người dùng tự nhập; id, email, mật khẩu, vai trò, trạng thái do service gán (email cần chuẩn hóa)
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "phone", source = "phone")
    User fromRegisterRequest(UserRegisterRequest request);

    // Không đụng tới vai trò, trạng thái, mật khẩu, ảnh đại diện; email do service chuẩn hóa rồi gán
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "gender", source = "gender")
    void updateFromRequest(ProfileUpdateRequest request, @MappingTarget User user);

    // Tài khoản nhân viên mới: mật khẩu, email chuẩn hóa và cờ xác thực do service gán
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "gender", source = "gender")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "status", source = "status")
    User fromStaffCreateRequest(StaffCreateRequest request);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "gender", source = "gender")
    @Mapping(target = "role", source = "role")
    void updateStaffFromRequest(StaffUpdateRequest request, @MappingTarget User user);
}
