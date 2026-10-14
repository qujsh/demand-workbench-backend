package com.demandworkbench.config;

import com.demandworkbench.identity.application.AuthService;
import com.demandworkbench.identity.application.AuthenticatedUser;
import com.demandworkbench.identity.controller.AuthController;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class SessionAuthenticationFilter extends OncePerRequestFilter {
    private final AuthService service;

    public SessionAuthenticationFilter(AuthService service) {
        this.service = service;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var user = service.authenticate(readToken(request));
        if (user != null) {
            var principal = new AuthenticatedUser(user.id(), user.userNo(), user.nickname(), user.status());
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(principal, null, List.of()));
        }
        chain.doFilter(request, response);
    }

    private String readToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (AuthController.SESSION_COOKIE.equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }
}
