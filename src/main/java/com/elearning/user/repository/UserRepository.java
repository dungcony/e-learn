package com.elearning.user.repository;

import com.elearning.user.entity.User;
import com.elearning.user.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Truy vấn bảng {@code users}. Mọi truy vấn JPA tự bỏ qua tài khoản đã xóa mềm; chỉ
 * {@link #findBriefsByIds} đọc được chúng.
 */
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    Optional<User> findByIdAndRole(UUID id, Role role);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    boolean existsByRole(Role role);

    // Native để không bị @SQLRestriction loại tài khoản đã xóa.
    @Query(value = "SELECT id AS id, full_name AS \"fullName\", email AS email FROM users WHERE id IN (:ids)",
            nativeQuery = true)
    List<UserBriefView> findBriefsByIds(@Param("ids") Collection<UUID> ids);
}
