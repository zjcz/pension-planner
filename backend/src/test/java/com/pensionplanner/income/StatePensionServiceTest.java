package com.pensionplanner.income;

import com.pensionplanner.audit.AuditService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
    void getForUserReturnsNullWhenNotFound() {
        when(statePensionRepository.findByUserId(42L)).thenReturn(Optional.empty());

        assertThat(service.getForUser(42L)).isNull();
    }

    @Test
    void getForUserReturnsExisting() {
        StatePension existing = new StatePension();
        existing.setId(1L);
        existing.setUserId(42L);
        when(statePensionRepository.findByUserId(42L)).thenReturn(Optional.of(existing));

        assertThat(service.getForUser(42L).getId()).isEqualTo(1L);
    }

    @Test
    void upsertCreatesWhenNotExist() {
        when(statePensionRepository.findByUserId(42L)).thenReturn(Optional.empty());
        when(statePensionRepository.save(any())).thenAnswer(invocation -> {
            StatePension sp = invocation.getArgument(0);
            sp.setId(1L);
            return sp;
        });

        StatePensionRequest request = new StatePensionRequest(10000L, 2026);
        StatePension result = service.upsert(42L, request);

        assertThat(result.getUserId()).isEqualTo(42L);
        assertThat(result.getYearlyAmount()).isEqualTo(10000L);
        assertThat(result.getTakesEffectYear()).isEqualTo(2026);
        verify(auditService).recordCreate(result);
    }

    @Test
    void upsertUpdatesWhenExist() {
        StatePension existing = new StatePension();
        existing.setId(1L);
        existing.setUserId(42L);
        existing.setYearlyAmount(8000L);
        when(statePensionRepository.findByUserId(42L)).thenReturn(Optional.of(existing));
        when(statePensionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        StatePensionRequest request = new StatePensionRequest(12000L, 2030);
        StatePension result = service.upsert(42L, request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getYearlyAmount()).isEqualTo(12000L);
        verify(auditService).recordUpdate(existing);
        verify(auditService, never()).recordCreate(any(StatePension.class));
    }
}
