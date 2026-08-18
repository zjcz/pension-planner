package com.pensionplanner.pension;

import com.pensionplanner.audit.AuditService;
import com.pensionplanner.common.ApiException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StatementServiceTest {

    private final PensionRepository pensionRepository = mock(PensionRepository.class);
    private final PensionStatementRepository statementRepository = mock(PensionStatementRepository.class);
    private final AuditService auditService = mock(AuditService.class);
    private final StatementService service = new StatementService(pensionRepository, statementRepository, auditService);

    @Test
    void listForPensionReturnsStatements() {
        when(pensionRepository.findByPensionIdAndUserId(1L, 42L)).thenReturn(Optional.of(new Pension()));
        when(statementRepository.findByPensionIdOrderByStatementDateAsc(1L)).thenReturn(List.of(new PensionStatement()));

        assertThat(service.listForPension(42L, 1L)).hasSize(1);
    }

    @Test
    void listForPensionThrowsIfPensionNotFound() {
        when(pensionRepository.findByPensionIdAndUserId(1L, 42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listForPension(42L, 1L))
                .isInstanceOf(ApiException.class);
        verify(statementRepository, never()).findByPensionIdOrderByStatementDateAsc(any());
    }

    @Test
    void createSetsPensionIdAndAudits() {
        when(pensionRepository.findByPensionIdAndUserId(1L, 42L)).thenReturn(Optional.of(new Pension()));
        when(statementRepository.save(any())).thenAnswer(invocation -> {
            PensionStatement s = invocation.getArgument(0);
            s.setStatementId(10L);
            return s;
        });

        StatementRequest request = new StatementRequest(
                LocalDate.of(2026, 1, 1), 100000L, 5000L,
                500L, null, 1000L, "Initial");

        PensionStatement result = service.create(42L, 1L, request);

        assertThat(result.getPensionId()).isEqualTo(1L);
        assertThat(result.getStatementDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(result.getPlanValue()).isEqualTo(100000L);
        assertThat(result.getStatementNotes()).isEqualTo("Initial");
        verify(auditService).recordCreate(result);
    }

    @Test
    void getForUserVerifiesOwnership() {
        when(pensionRepository.findByPensionIdAndUserId(1L, 42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getForUser(42L, 1L, 10L))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void getForUserThrowsIfStatementNotFound() {
        when(pensionRepository.findByPensionIdAndUserId(1L, 42L)).thenReturn(Optional.of(new Pension()));
        when(statementRepository.findByStatementIdAndPensionId(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getForUser(42L, 1L, 10L))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void updateAuditsBeforeMutationAndSaves() {
        PensionStatement existing = new PensionStatement();
        existing.setStatementId(10L);
        existing.setPensionId(1L);
        existing.setPlanValue(100000L);

        when(pensionRepository.findByPensionIdAndUserId(1L, 42L)).thenReturn(Optional.of(new Pension()));
        when(statementRepository.findByStatementIdAndPensionId(10L, 1L)).thenReturn(Optional.of(existing));
        when(statementRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        StatementRequest request = new StatementRequest(
                LocalDate.of(2026, 6, 1), 150000L, 7500L,
                null, null, null, null);

        PensionStatement result = service.update(42L, 1L, 10L, request);

        verify(auditService).recordUpdate(existing);
        assertThat(result.getPlanValue()).isEqualTo(150000L);
    }

    @Test
    void deleteAuditsAndRemoves() {
        PensionStatement statement = new PensionStatement();
        statement.setStatementId(10L);
        statement.setPensionId(1L);

        when(pensionRepository.findByPensionIdAndUserId(1L, 42L)).thenReturn(Optional.of(new Pension()));
        when(statementRepository.findByStatementIdAndPensionId(10L, 1L)).thenReturn(Optional.of(statement));

        service.delete(42L, 1L, 10L);

        verify(auditService).recordDelete(statement);
        verify(statementRepository).delete(statement);
    }

    @Test
    void updateThrowsForOtherUsersPension() {
        when(pensionRepository.findByPensionIdAndUserId(1L, 42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(42L, 1L, 10L,
                new StatementRequest(LocalDate.of(2026, 1, 1), 100000L, 5000L,
                        null, null, null, null)))
                .isInstanceOf(ApiException.class);
        verify(statementRepository, never()).save(any());
    }
}
