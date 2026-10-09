package com.elearning.user.repository;

import com.elearning.user.entity.PasswordResetToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    /**
     * Tìm token theo bản băm và khóa bi quan ({@code SELECT ... FOR UPDATE}) đúng một dòng, để hai request
     * dùng cùng một token cùng lúc không cùng thành công. Giao dịch gọi hàm này phải ngắn.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    // Vô hiệu các token chưa dùng của người dùng, để chỉ link mới nhất có hiệu lực.
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.usedAt = :now WHERE t.userId = :userId AND t.usedAt IS NULL")
    int invalidateUnusedByUserId(@Param("userId") UUID userId, @Param("now") Instant now);
}
