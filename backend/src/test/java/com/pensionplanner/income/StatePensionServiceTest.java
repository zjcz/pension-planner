package com.pensionplanner.income;

import com.pensionplanner.audit.AuditService;
import com.pensionplanner.common.ApiException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StatePensionServiceTest {

    private final StatePensionRepository statePensionRepository = mock(StatePensionRepository.class);
    private final AuditService auditService = mock(AuditService.class);
    private final StatePensionService service = new StatePensionService(statePensionRepository, auditService);

    @Test
    void listForUserReturnsAllRecords() {
        StatePension existing = new StatePension();
        existing.setId(1L);
        existing.setUserId(42L);
        when(statePensionRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of(existing));

        assertThat(service.listForUser(42L)).hasSize(1);
        assertThat(service.listForUser(42L).get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getForUserThrowsWhenNotFound() {
        when(statePensionRepository.findByIdAndUserId(1L, 42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getForUser(42L, 1L))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void getForUserReturnsExisting() {
        StatePension existing = new StatePension();
        existing.setId(1L);
        existing.setUserId(42L);
        when(statePensionRepository.findByIdAndUserId(1L, 42L)).thenReturn(Optional.of(existing));

        assertThat(service.getForUser(42L, 1L).getId()).isEqualTo(1L);
    }

    @Test
    void createPersistsAndAudits() {
        when(statePensionRepository.save(any())).thenAnswer(invocation -> {
            StatePension sp = invocation.getArgument(0);
            sp.setId(1L);
            return sp;
        });

        StatePensionRequest request = new StatePensionRequest("My State Pension", 10000L, 2026, "forecast");
        StatePension result = service.create(42L, request);

        assertThat(result.getUserId()).isEqualTo(42L);
        assertThat(result.getName()).isEqualTo("My State Pension");
        assertThat(result.getYearlyAmount()).isEqualTo(10000L);
        assertThat(result.getTakesEffectYear()).isEqualTo(2026);
        assertThat(result.getNotes()).isEqualTo("forecast");
        verify(auditService).recordCreate(result);
    }

    @Test
    void updateAppliesChangesAndAudits() {
        StatePension existing = new StatePension();
        existing.setId(1L);
        existing.setUserId(42L);
        existing.setYearlyAmount(8000L);
        when(statePensionRepository.findByIdAndUserId(1L, 42L)).thenReturn(Optional.of(existing));
        when(statePensionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        StatePensionRequest request = new StatePensionRequest("Renamed", 12000L, 2030, null);
        StatePension result = service.update(42L, 1L, request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Renamed");
        assertThat(result.getYearlyAmount()).isEqualTo(12000L);
        assertThat(result.getTakesEffectYear()).isEqualTo(2030);
        verify(auditService).recordUpdate(existing);
        verify(auditService, never()).recordCreate(any(StatePension.class));
    }

    @Test
    void deleteRemovesAndAudits() {
        StatePension existing = new StatePension();
        existing.setId(1L);
        existing.setUserId(42L);
        when(statePensionRepository.findByIdAndUserId(1L, 42L)).thenReturn(Optional.of(existing));

        service.delete(42L, 1L);

        verify(statePensionRepository).delete(existing);
        verify(auditService).recordDelete(existing);
    }
}