package com.demandworkbench.identity.infrastructure.persistence;

import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface IdentityMapper {
    @Select("""
        SELECT u.id, u.user_no, u.nickname, u.status, i.identifier AS phone
        FROM sys_user u JOIN user_auth_identity i ON i.user_id = u.id AND i.deleted = 0
        WHERE i.identity_type = 'phone' AND i.identifier = #{phone} AND u.deleted = 0
        """)
    IdentityRecord findByPhone(String phone);

    @Select("""
        SELECT u.id, u.user_no, u.nickname, u.status, i.identifier AS phone
        FROM user_session s
        JOIN sys_user u ON u.id = s.user_id AND u.deleted = 0
        LEFT JOIN user_auth_identity i ON i.user_id = u.id AND i.identity_type = 'phone' AND i.deleted = 0
        WHERE s.token_hash = #{tokenHash} AND s.revoked_at IS NULL AND s.expires_at > #{now} AND s.deleted = 0
        """)
    IdentityRecord findBySession(@Param("tokenHash") String tokenHash, @Param("now") LocalDateTime now);

    @Insert("INSERT INTO sys_user (nickname, status) VALUES (#{nickname}, 'active')")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertUser(IdentityRecord record);

    @Update("UPDATE sys_user SET user_no = #{userNo} WHERE id = #{id} AND deleted = 0")
    int updateUserNo(IdentityRecord record);

    @Insert("INSERT INTO user_auth_identity (user_id, identity_type, identifier, verified_at) VALUES (#{id}, 'phone', #{phone}, CURRENT_TIMESTAMP(3))")
    void insertPhoneIdentity(IdentityRecord record);

    @Insert("INSERT INTO user_business_identity (user_id, identity_type, status, activated_at) VALUES (#{id}, 'publisher', 'active', CURRENT_TIMESTAMP(3))")
    void insertPublisherIdentity(IdentityRecord record);

    @Insert("INSERT INTO publisher_profile (user_id, level_code, growth_points, status) VALUES (#{id}, 'L0', 0, 'active')")
    void insertPublisherProfile(IdentityRecord record);

    @Insert("INSERT INTO user_session (session_no, user_id, token_hash, expires_at) VALUES (#{sessionNo}, #{userId}, #{tokenHash}, #{expiresAt})")
    void insertSession(@Param("sessionNo") String sessionNo, @Param("userId") long userId,
                       @Param("tokenHash") String tokenHash, @Param("expiresAt") LocalDateTime expiresAt);

    @Update("UPDATE user_session SET revoked_at = #{revokedAt} WHERE token_hash = #{tokenHash} AND revoked_at IS NULL AND deleted = 0")
    int revokeSession(@Param("tokenHash") String tokenHash, @Param("revokedAt") LocalDateTime revokedAt);

    @Update("UPDATE sys_user SET last_login_at = #{loginAt} WHERE id = #{userId} AND deleted = 0")
    int updateLastLogin(@Param("userId") long userId, @Param("loginAt") LocalDateTime loginAt);
}
