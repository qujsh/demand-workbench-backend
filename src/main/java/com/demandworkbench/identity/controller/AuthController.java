package com.demandworkbench.identity.controller;

import com.demandworkbench.common.ApiResponse;
import com.demandworkbench.identity.application.AuthService;
import com.demandworkbench.identity.application.AuthenticatedUser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    public static final String SESSION_COOKIE = "DW_SESSION";
    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/sms/send")
    public ApiResponse<CodeSentResponse> sendCode(@Valid @RequestBody PhoneRequest request) {
        service.sendCode(request.phone());
        return ApiResponse.success(new CodeSentResponse("验证码已发送", "123456"));
    }

    @PostMapping("/sms/login")
    public ApiResponse<UserResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        var result = service.login(request.phone(), request.code());
        var cookie = ResponseCookie.from(SESSION_COOKIE, result.token())
                .httpOnly(true).sameSite("Lax").path("/")
                .maxAge(Duration.between(java.time.LocalDateTime.now(), result.expiresAt())).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ApiResponse.success(UserResponse.from(result.user()));
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(HttpServletRequest request, HttpServletResponse response) {
        service.logout(readToken(request));
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(SESSION_COOKIE, "")
                .httpOnly(true).sameSite("Lax").path("/").maxAge(Duration.ZERO).build().toString());
        return ApiResponse.success("logged_out");
    }

    private String readToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (SESSION_COOKIE.equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }

    public record PhoneRequest(@NotBlank @Pattern(regexp = "^1\\d{10}$", message = "请输入有效的手机号") String phone) { }
    public record LoginRequest(@NotBlank @Pattern(regexp = "^1\\d{10}$", message = "请输入有效的手机号") String phone,
                               @NotBlank String code) { }
    public record CodeSentResponse(String message, String developmentCode) { }
    public record UserResponse(String userNo, String nickname, String phone, String status) {
        static UserResponse from(com.demandworkbench.identity.application.CurrentUser user) {
            return new UserResponse(user.userNo(), user.nickname(), user.phone(), user.status());
        }
    }
}
