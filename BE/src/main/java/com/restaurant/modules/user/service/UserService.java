package com.restaurant.modules.user.service;

import com.restaurant.common.response.PageRequestParams;
import com.restaurant.modules.user.dto.request.PasswordChangeRequest;
import com.restaurant.modules.user.dto.request.ProfileUpdateRequest;
import com.restaurant.modules.user.dto.request.StaffCreateRequest;
import com.restaurant.modules.user.dto.request.StaffUpdateRequest;
import com.restaurant.modules.user.dto.request.UserSearchRequest;
import com.restaurant.modules.user.dto.response.ProfileResponse;
import com.restaurant.modules.user.dto.response.UserBriefResponse;
import com.restaurant.modules.user.dto.response.UserDetailResponse;
import com.restaurant.modules.user.dto.response.UserSummaryResponse;
import com.restaurant.modules.user.enums.UserGroup;
import com.restaurant.modules.user.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Tài khoản người dùng: hồ sơ và mật khẩu của chính mình (UC003, UC005) và quản trị nhân viên, khách hàng của Quản lý
 * (UC006, UC008, UC009). Mọi hàm nhận id người dùng thuần, không nhận token.
 */
public interface UserService {

    // ---- của chính người đăng nhập ----

    /**
     * @throws com.restaurant.common.exception.BusinessException {@code NOT_FOUND} nếu tài khoản không còn tồn tại
     */
    ProfileResponse getProfile(UUID userId);

    /**
     * Cập nhật họ tên, email, ngày sinh, điện thoại, giới tính. Không đổi được vai trò, trạng thái hay mật khẩu.
     *
     * @throws com.restaurant.common.exception.BusinessException {@code AUTH_EMAIL_ALREADY_EXISTS} nếu email thuộc tài khoản khác
     */
    ProfileResponse updateProfile(UUID userId, ProfileUpdateRequest request);

    /**
     * Lưu ảnh đại diện (png, gif, jpg, jpeg) và cập nhật đường dẫn.
     *
     * @throws com.restaurant.common.exception.BusinessException {@code FILE_TYPE_NOT_SUPPORTED} nếu sai định dạng
     */
    ProfileResponse updateAvatar(UUID userId, MultipartFile file);

    /**
     * Đổi mật khẩu và hạ cờ đổi mật khẩu lần đầu.
     *
     * @throws com.restaurant.common.exception.BusinessException {@code AUTH_PASSWORD_CONFIRM_MISMATCH},
     *                                                          {@code AUTH_OLD_PASSWORD_INCORRECT},
     *                                                          {@code AUTH_PASSWORD_SAME_AS_OLD}
     */
    void changePassword(UUID userId, PasswordChangeRequest request);

    // ---- quản trị của Quản lý ----

    /**
     * Tìm tài khoản của một nhóm (UC006); chỉ trả tài khoản chưa xóa, mới nhất trước theo mặc định.
     *
     * @throws com.restaurant.common.exception.BusinessException {@code VALIDATION_ERROR} nếu tìm nhân viên với vai trò {@code CUSTOMER}
     */
    Page<UserSummaryResponse> searchUsers(UserGroup group, UserSearchRequest request, PageRequestParams params);

    /**
     * @throws com.restaurant.common.exception.BusinessException {@code NOT_FOUND} nếu không có, đã xóa, hoặc thuộc nhóm khác
     */
    UserDetailResponse getUser(UserGroup group, UUID id);

    /**
     * Tạo nhân viên: đã xác thực email và phải đổi mật khẩu ở lần đăng nhập đầu.
     *
     * @throws com.restaurant.common.exception.BusinessException {@code VALIDATION_ERROR} nếu vai trò là {@code CUSTOMER},
     *                                                          {@code AUTH_EMAIL_ALREADY_EXISTS} nếu email đã dùng
     */
    UserDetailResponse createStaff(StaffCreateRequest request);

    /**
     * @param currentUserId Quản lý đang thao tác
     * @throws com.restaurant.common.exception.BusinessException {@code USER_CANNOT_MODIFY_SELF} nếu tự đổi vai trò của mình,
     *                                                          {@code AUTH_EMAIL_ALREADY_EXISTS}, {@code NOT_FOUND}
     */
    UserDetailResponse updateStaff(UUID currentUserId, UUID id, StaffUpdateRequest request);

    /**
     * @throws com.restaurant.common.exception.BusinessException {@code NOT_FOUND}, {@code FILE_TYPE_NOT_SUPPORTED}
     */
    UserDetailResponse updateStaffAvatar(UUID id, MultipartFile file);

    /**
     * Khóa hoặc mở khóa. Khóa ghi vào danh sách chặn token (Redis) nên token đang dùng bị từ chối ngay; mở khóa gỡ khỏi danh sách.
     * Redis nằm ngoài transaction JPA nên được ghi sau khi lưu thành công.
     *
     * @throws com.restaurant.common.exception.BusinessException {@code USER_CANNOT_MODIFY_SELF}, {@code NOT_FOUND}
     */
    UserDetailResponse changeStatus(UserGroup group, UUID currentUserId, UUID id, UserStatus status);

    /**
     * Xóa mềm và chặn token đang dùng. Mỗi {@link UserDeletionGuard} được hỏi trước.
     *
     * @throws com.restaurant.common.exception.BusinessException {@code USER_CANNOT_MODIFY_SELF}, {@code USER_HAS_ACTIVITY}, {@code NOT_FOUND}
     */
    void deleteUser(UserGroup group, UUID currentUserId, UUID id);

    // ---- API công khai cho module khác ----

    /**
     * Tên và liên hệ của các tài khoản, kể cả đã xóa mềm. Id không tồn tại bị bỏ qua.
     */
    List<UserBriefResponse> getUserBriefs(Collection<UUID> ids);
}
