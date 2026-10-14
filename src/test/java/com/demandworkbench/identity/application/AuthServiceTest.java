package com.demandworkbench.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

class AuthServiceTest {
    private final IdentityRepository repository = mock(IdentityRepository.class);
    private final AuthService service = new AuthService(repository, 30);

    @Test
    void shouldCreateUserAndSessionAfterValidDevelopmentCode() {
        var user = new CurrentUser(2L, "USER-000002", "用户0002", "13900000000", "active");
        when(repository.findByPhone("13900000000")).thenReturn(null);
        when(repository.createPhoneUser("13900000000", "用户0000")).thenReturn(user);

        service.sendCode("13900000000");
        var result = service.login("13900000000", "123456");

        assertThat(result.user()).isEqualTo(user);
        assertThat(result.token()).hasSize(64);
        verify(repository).saveSession(anyString(), anyLong(), anyString(), any());
    }

    @Test
    void shouldRejectCodeThatWasNotRequested() {
        assertThatThrownBy(() -> service.login("13900000000", "123456"))
                .isInstanceOf(AuthException.class)
                .hasMessage("验证码无效或已过期");
    }
}
