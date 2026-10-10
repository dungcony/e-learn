package com.restaurant.modules.user.repository;

import com.restaurant.modules.user.entity.UserToken;
import com.restaurant.modules.user.enums.UserTokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserTokenRepository extends JpaRepository<UserToken, UUID> {

    Optional<UserToken> findByTokenHash(String tokenHash);

    /** Vô hiệu các token cùng loại còn hiệu lực của người dùng khi phát token mới; trả số token bị vô hiệu. */
    @Modifying
    @Query("update UserToken t set t.usedAt = :now where t.userId = :userId and t.type = :type and t.usedAt is null")
    int invalidateActiveTokens(@Param("userId") UUID userId, @Param("type") UserTokenType type, @Param("now") Instant now);

    /** Dọn token đã quá hạn của người dùng; gọi khi phát token mới nên không cần job riêng. */
    @Modifying
    @Query("delete from UserToken t where t.userId = :userId and t.type = :type and t.expiresAt < :now")
    int deleteExpired(@Param("userId") UUID userId, @Param("type") UserTokenType type, @Param("now") Instant now);
}
