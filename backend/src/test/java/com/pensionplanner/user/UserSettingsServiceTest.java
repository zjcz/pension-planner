package com.pensionplanner.user;

import com.pensionplanner.audit.AuditService;
import com.pensionplanner.common.ApiException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserSettingsServiceTest {

    private final UserSettingsRepository repository = mock(UserSettingsRepository.class);
    private final AuditService auditService = mock(AuditService.class);
    private final UserSettingsService service = new UserSettingsService(repository, auditService);

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
        settings.setTargetIncome(5000000L);
        when(repository.findByUserId(1L)).thenReturn(Optional.of(settings));

        UserSettings result = service.getForUser(1L);

        assertThat(result.getTargetIncome()).isEqualTo(5000000L);
    }

    @Test
    void updateCreatesSettingsWhenMissing() {
        when(repository.findByUserId(2L)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UserSettings updated = service.update(2L, 5000000L, LocalDate.of(2045, 1, 1), true);

        assertThat(updated.getUserId()).isEqualTo(2L);
        assertThat(updated.getTargetIncome()).isEqualTo(5000000L);
        assertThat(updated.getRetirementDate()).isEqualTo(LocalDate.of(2045, 1, 1));
        verify(repository).save(any());
    }

    @Test
    void updateAmendsExistingSettings() {
        UserSettings settings = new UserSettings();
        settings.setId(10L);
        settings.setUserId(3L);
        settings.setTargetIncome(3000000L);
        when(repository.findByUserId(3L)).thenReturn(Optional.of(settings));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UserSettings updated = service.update(3L, 6000000L, null, true);

        assertThat(updated.getId()).isEqualTo(10L);
        assertThat(updated.getTargetIncome()).isEqualTo(6000000L);
        assertThat(updated.getRetirementDate()).isNull();
    }

    @Test
    void updateStoresAuditEnabled() {
        when(repository.findByUserId(5L)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UserSettings result = service.update(5L, null, null, false);

        assertThat(result.isAuditEnabled()).isFalse();
    }

    @Test
    void disablingAuditPurgesAuditData() {
        UserSettings settings = new UserSettings();
        settings.setUserId(3L);
        settings.setAuditEnabled(true);
        when(repository.findByUserId(3L)).thenReturn(Optional.of(settings));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.update(3L, null, null, false);

        verify(auditService).purgeUserAudits(3L);
    }

    @Test
    void enablingAuditSnapshotsRecords() {
        UserSettings settings = new UserSettings();
        settings.setUserId(3L);
        settings.setAuditEnabled(false);
        when(repository.findByUserId(3L)).thenReturn(Optional.of(settings));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.update(3L, null, null, true);

        verify(auditService).snapshotUserAudits(3L);
    }

    @Test
    void noAuditActionWhenSettingUnchanged() {
        UserSettings settings = new UserSettings();
        settings.setUserId(3L);
        settings.setAuditEnabled(true);
        when(repository.findByUserId(3L)).thenReturn(Optional.of(settings));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.update(3L, null, null, true);

        verify(auditService, never()).purgeUserAudits(any());
        verify(auditService, never()).snapshotUserAudits(any());
    }
}
