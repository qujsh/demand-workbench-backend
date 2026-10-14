package com.demandworkbench.identity.infrastructure.persistence;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IdentityRecord {
    private Long id;
    private String userNo;
    private String nickname;
    private String phone;
    private String status;
}
