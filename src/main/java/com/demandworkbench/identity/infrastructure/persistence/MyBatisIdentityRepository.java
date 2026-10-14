package com.demandworkbench.identity.infrastructure.persistence;

import com.demandworkbench.identity.application.CurrentUser;
import com.demandworkbench.identity.application.IdentityRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class MyBatisIdentityRepository implements IdentityRepository {
    private final IdentityMapper mapper;

    public MyBatisIdentityRepository(IdentityMapper mapper) {
        this.mapper = mapper;
    }

    @Override public CurrentUser findByPhone(String phone) { return toUser(mapper.findByPhone(phone)); }
    @Override public CurrentUser findBySessionTokenHash(String tokenHash, LocalDateTime now) { return toUser(mapper.findBySession(tokenHash, now)); }

    @Override
    @Transactional
    public CurrentUser createPhoneUser(String phone, String nickname) {
        var record = new IdentityRecord();
        record.setPhone(phone);
        record.setNickname(nickname);
        mapper.insertUser(record);
        record.setUserNo("USER-" + String.format("%06d", record.getId()));
        mapper.updateUserNo(record);
        mapper.insertPhoneIdentity(record);
        mapper.insertPublisherIdentity(record);
        mapper.insertPublisherProfile(record);
        return findByPhone(phone);
    }

    @Override public void saveSession(String sessionNo, long userId, String tokenHash, LocalDateTime expiresAt) { mapper.insertSession(sessionNo, userId, tokenHash, expiresAt); }
    @Override public void revokeSession(String tokenHash, LocalDateTime revokedAt) { mapper.revokeSession(tokenHash, revokedAt); }
    @Override public void updateLastLogin(long userId, LocalDateTime loginAt) { mapper.updateLastLogin(userId, loginAt); }

    private CurrentUser toUser(IdentityRecord record) {
        return record == null ? null : new CurrentUser(record.getId(), record.getUserNo(), record.getNickname(), record.getPhone(), record.getStatus());
    }
}
