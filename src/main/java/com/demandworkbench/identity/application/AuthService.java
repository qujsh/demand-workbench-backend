package com.demandworkbench.identity.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final String DEVELOPMENT_CODE = "123456";
    private final IdentityRepository repository;
    private final Map<String, LocalDateTime> verificationCodes = new ConcurrentHashMap<>();
    private final Duration sessionDuration;

    public AuthService(IdentityRepository repository,
                       @Value("${identity.session-duration-days:30}") long sessionDurationDays) {
        this.repository = repository;
        this.sessionDuration = Duration.ofDays(sessionDurationDays);
    }

    public void sendCode(String phone) {
        verificationCodes.put(normalizePhone(phone), LocalDateTime.now().plusMinutes(5));
    }

    public LoginResult login(String phone, String code) {
        var normalizedPhone = normalizePhone(phone);
        var expiresAt = verificationCodes.get(normalizedPhone);
        if (!DEVELOPMENT_CODE.equals(code) || expiresAt == null || expiresAt.isBefore(LocalDateTime.now())) {
            throw new AuthException("S048100002", "验证码无效或已过期");
        }
        verificationCodes.remove(normalizedPhone);
        var user = repository.findByPhone(normalizedPhone);
        if (user == null) {
            user = repository.createPhoneUser(normalizedPhone, "用户" + normalizedPhone.substring(7));
        }
        if (!"active".equals(user.status())) {
            throw new AuthException("S048100003", "账号当前不可登录");
        }
        var token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        var sessionExpiresAt = LocalDateTime.now().plus(sessionDuration);
        repository.saveSession("SESSION-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase(),
                user.id(), hash(token), sessionExpiresAt);
        repository.updateLastLogin(user.id(), LocalDateTime.now());
        return new LoginResult(user, token, sessionExpiresAt);
    }

    public CurrentUser authenticate(String token) {
        return token == null || token.isBlank()
                ? null : repository.findBySessionTokenHash(hash(token), LocalDateTime.now());
    }

    public void logout(String token) {
        if (token != null && !token.isBlank()) repository.revokeSession(hash(token), LocalDateTime.now());
    }

    private String normalizePhone(String phone) {
        return phone == null ? "" : phone.replaceAll("\\s+", "");
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前运行环境不支持 SHA-256", exception);
        }
    }

    public record LoginResult(CurrentUser user, String token, LocalDateTime expiresAt) {
    }
}
