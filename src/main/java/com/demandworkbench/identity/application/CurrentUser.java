package com.demandworkbench.identity.application;

public record CurrentUser(long id, String userNo, String nickname, String phone, String status) {
}
