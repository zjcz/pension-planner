package com.pensionplanner.user;

import com.pensionplanner.common.ApiException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserSettingsServiceTest {

    private final UserSettingsRepository repository = mock(UserSettingsRepository.class);
    private final UserSettingsService service = new UserSettingsService(repository);

    @Test
    void getForUserThrowsNotFoundWhenMissing() {
        when(repository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getForUser(1L))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void getForUserReturnsExistingSettings() {
        UserSettings settings = new UserSettings();
        settings.setUserId(1L);
        settings.setTargetIncome(50000.0);
        when(repository.findByUserId(1L)).thenReturn(Optional.of(settings));

        UserSettings result = service.getForUser(1L);

        assertThat(result.getTargetIncome()).isEqualTo(50000.0);
    }

    @Test
    void updateCreatesSettingsWhenMissing() {
        when(repository.findByUserId(2L)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UserSettings updated = service.update(2L, 50000.0, Instant.parse("2045-01-01T00:00:00Z"));

        assertThat(updated.getUserId()).isEqualTo(2L);
        assertThat(updated.getTargetIncome()).isEqualTo(50000.0);
        assertThat(updated.getRetirementDate()).isEqualTo(Instant.parse("2045-01-01T00:00:00Z"));
        verify(repository).save(any());
    }

    @Test
    void updateAmendsExistingSettings() {
        UserSettings settings = new UserSettings();
        settings.setId(10L);
        settings.setUserId(3L);
        settings.setTargetIncome(30000.0);
        when(repository.findByUserId(3L)).thenReturn(Optional.of(settings));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UserSettings updated = service.update(3L, 60000.0, null);

        assertThat(updated.getId()).isEqualTo(10L);
        assertThat(updated.getTargetIncome()).isEqualTo(60000.0);
        assertThat(updated.getRetirementDate()).isNull();
    }
}
