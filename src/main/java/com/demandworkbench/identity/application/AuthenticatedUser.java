package com.demandworkbench.identity.application;

public record AuthenticatedUser(long id, String userNo, String nickname, String status) {
}
