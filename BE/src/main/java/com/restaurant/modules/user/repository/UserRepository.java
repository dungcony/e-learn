package com.restaurant.modules.user.repository;

import com.restaurant.modules.user.entity.User;
import com.restaurant.modules.user.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Truy vấn bảng {@code users}. Email truyền vào phải đã chuẩn hóa chữ thường; so sánh theo {@code lower(email)} để khớp
 * unique index {@code uq_users_email}.
 */
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    @Query("select u from User u where lower(u.email) = :email")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("select count(u) > 0 from User u where lower(u.email) = :email")
    boolean existsByEmail(@Param("email") String email);

    @Query("select count(u) > 0 from User u where lower(u.email) = :email and u.id <> :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") UUID id);

    boolean existsByRole(Role role);

    Optional<User> findByIdAndRoleIn(UUID id, Collection<Role> roles);

    /**
     * Tên và liên hệ của người dùng kể cả tài khoản đã xóa mềm: native query không chịu {@code @SQLRestriction}, nên hóa đơn
     * và đơn hàng cũ vẫn hiện được tên nhân viên đã nghỉ.
     */
    @Query(value = """
            select id as "id", full_name as "fullName", email as "email", phone as "phone", role as "role"
            from users where id in (:ids)
            """, nativeQuery = true)
    List<UserBriefProjection> findBriefsIncludingDeleted(@Param("ids") Collection<UUID> ids);
}
