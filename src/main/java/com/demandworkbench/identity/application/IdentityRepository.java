package com.demandworkbench.identity.application;

import java.time.LocalDateTime;

public interface IdentityRepository {
    CurrentUser findByPhone(String phone);
    CurrentUser findBySessionTokenHash(String tokenHash, LocalDateTime now);
    CurrentUser createPhoneUser(String phone, String nickname);
    void saveSession(String sessionNo, long userId, String tokenHash, LocalDateTime expiresAt);
    void revokeSession(String tokenHash, LocalDateTime revokedAt);
    void updateLastLogin(long userId, LocalDateTime loginAt);
}
