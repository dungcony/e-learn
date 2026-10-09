package com.elearning.user.service;

import com.elearning.common.response.PageRequestParams;
import com.elearning.user.dto.request.PasswordChangeRequest;
import com.elearning.user.dto.request.ProfileUpdateRequest;
import com.elearning.user.dto.request.TeacherCreateRequest;
import com.elearning.user.dto.request.TeacherUpdateRequest;
import com.elearning.user.dto.request.UserSearchRequest;
import com.elearning.user.dto.response.ProfileResponse;
import com.elearning.user.dto.response.UserBriefResponse;
import com.elearning.user.dto.response.UserDetailResponse;
import com.elearning.user.dto.response.UserSummaryResponse;
import com.elearning.user.enums.Role;
import com.elearning.user.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Thông tin cá nhân (UC002, UC005), quản lý giảng viên, học viên của Quản trị viên (UC006, UC008, UC010) và API
 * công khai cho module khác.
 * <p>
 * Khóa hoặc xóa tài khoản ghi {@code BlacklistedUserModel} lên Redis sau khi commit, để token đang dùng của người đó
 * bị chặn ngay thay vì chờ hết hạn.
 */
public interface UserService {

    /**
     * @param userId người đăng nhập
     * @return thông tin cá nhân
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND} nếu tài khoản đã bị xóa
     */
    ProfileResponse getProfile(UUID userId);

    /**
     * Cập nhật thông tin cá nhân (UC005). Giảng viên, Quản trị viên được kèm mật khẩu mới (ghi chú UC002).
     *
     * @param userId  người đăng nhập
     * @param request form thông tin
     * @return thông tin sau khi cập nhật
     * @throws com.elearning.common.exception.BusinessException {@code VALIDATION_ERROR} nếu học viên gửi kèm mật khẩu;
     *                                                          {@code AUTH_PASSWORD_CONFIRM_MISMATCH}; {@code
     *                                                          AUTH_EMAIL_ALREADY_EXISTS} nếu đổi sang email của
     *                                                          tài khoản khác
     */
    ProfileResponse updateProfile(UUID userId, ProfileUpdateRequest request);

    /**
     * Lưu ảnh đại diện (ghi một file lên ổ đĩa trong transaction).
     *
     * @param userId người đăng nhập
     * @param file   ảnh png, gif, jpg hoặc jpeg
     * @return thông tin sau khi cập nhật
     * @throws com.elearning.common.exception.BusinessException {@code FILE_TYPE_NOT_SUPPORTED}
     */
    ProfileResponse updateAvatar(UUID userId, MultipartFile file);

    /**
     * Học viên đổi mật khẩu (UC002).
     *
     * @param userId  người đăng nhập
     * @param request mật khẩu cũ và mật khẩu mới
     * @throws com.elearning.common.exception.BusinessException {@code AUTH_OLD_PASSWORD_INCORRECT};
     *                                                          {@code AUTH_PASSWORD_CONFIRM_MISMATCH}
     */
    void changePassword(UUID userId, PasswordChangeRequest request);

    /**
     * Tìm kiếm giảng viên hoặc học viên (UC006). Cho phép sắp xếp theo {@code created_at}, {@code full_name},
     * {@code email}; mặc định mới nhất trước.
     *
     * @param role    {@code TEACHER} hoặc {@code STUDENT}
     * @param request tiêu chí tìm kiếm
     * @param params  phân trang, sắp xếp
     * @return một trang kết quả, có thể rỗng
     */
    Page<UserSummaryResponse> searchUsers(Role role, UserSearchRequest request, PageRequestParams params);

    /**
     * @param id   id tài khoản
     * @param role role mong đợi
     * @return chi tiết tài khoản
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND} nếu không tồn tại, đã xóa hoặc khác role
     */
    UserDetailResponse getUser(UUID id, Role role);

    /**
     * Thêm giảng viên (UC008); role luôn là {@code TEACHER}.
     *
     * @param request thông tin giảng viên
     * @return giảng viên vừa tạo
     * @throws com.elearning.common.exception.BusinessException {@code AUTH_EMAIL_ALREADY_EXISTS}
     */
    UserDetailResponse createTeacher(TeacherCreateRequest request);

    /**
     * Sửa giảng viên (UC008). Đổi {@code status} sang {@code LOCKED} chặn token đang dùng ngay; về {@code ACTIVE}
     * thì gỡ chặn.
     *
     * @param id      id giảng viên
     * @param request thông tin mới; {@code password} null thì giữ mật khẩu cũ
     * @return giảng viên sau khi sửa
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND}; {@code AUTH_EMAIL_ALREADY_EXISTS}
     */
    UserDetailResponse updateTeacher(UUID id, TeacherUpdateRequest request);

    /**
     * Lưu ảnh đại diện của giảng viên (ghi một file lên ổ đĩa trong transaction).
     *
     * @param id   id giảng viên
     * @param file ảnh png, gif, jpg hoặc jpeg
     * @return giảng viên sau khi cập nhật
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND}; {@code FILE_TYPE_NOT_SUPPORTED}
     */
    UserDetailResponse updateTeacherAvatar(UUID id, MultipartFile file);

    /**
     * Khóa hoặc mở khóa học viên (UC010).
     *
     * @param id     id học viên
     * @param status {@code LOCKED} hoặc {@code ACTIVE}
     * @return học viên sau khi cập nhật
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND}
     */
    UserDetailResponse changeStudentStatus(UUID id, UserStatus status);

    /**
     * Xóa mềm giảng viên hoặc học viên và chặn token đang dùng của họ.
     *
     * @param id   id tài khoản
     * @param role role mong đợi
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND}
     */
    void deleteUser(UUID id, Role role);

    /**
     * API công khai cho module khác lấy tên người dùng. Đọc cả tài khoản đã xóa mềm để vẫn hiển thị được tên người cũ.
     *
     * @param ids các id cần lấy; id không tồn tại bị bỏ qua
     * @return thông tin tối thiểu, thứ tự không đảm bảo
     */
    List<UserBriefResponse> getUserBriefs(Collection<UUID> ids);
}
