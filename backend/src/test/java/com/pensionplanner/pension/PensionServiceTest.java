package com.pensionplanner.pension;

import com.pensionplanner.audit.AuditService;
import com.pensionplanner.common.ApiException;
import com.pensionplanner.tag.PensionTagRepository;
import com.pensionplanner.tag.TagRepository;
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

class PensionServiceTest {

    private final PensionRepository pensionRepository = mock(PensionRepository.class);
    private final PensionStatementRepository statementRepository = mock(PensionStatementRepository.class);
    private final PensionTagRepository pensionTagRepository = mock(PensionTagRepository.class);
    private final TagRepository tagRepository = mock(TagRepository.class);
    private final AuditService auditService = mock(AuditService.class);
    private final PensionService service = new PensionService(
            pensionRepository, statementRepository, pensionTagRepository, tagRepository, auditService);

    @Test
    void createSetsUserIdAndAudits() {
        when(pensionRepository.save(any())).thenAnswer(invocation -> {
            Pension p = invocation.getArgument(0);
            p.setPensionId(7L);
            return p;
        });

        PensionRequest request = new PensionRequest("My Pension", LocalDate.of(2045, 1, 1),
                PensionStatus.ACTIVE, null, "#123456", null, null, null, null);

        Pension result = service.create(42L, request);

        assertThat(result.getUserId()).isEqualTo(42L);
        assertThat(result.getName()).isEqualTo("My Pension");
        assertThat(result.getStatusDate()).isEqualTo(LocalDate.now());
        assertThat(result.getColor()).isEqualTo("#123456");
        verify(auditService).recordCreate(result);
    }

    @Test
    void createAlwaysSetsStatusDateToToday() {
        when(pensionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Pension result = service.create(1L, new PensionRequest("P", LocalDate.of(2045, 1, 1),
                PensionStatus.CLOSED, null, null, null, null, null, null));

        assertThat(result.getStatusDate()).isEqualTo(LocalDate.now());
        assertThat(result.getStatus()).isEqualTo(PensionStatus.CLOSED);
    }

    @Test
    void getForUserReturnsOnlyOwnedPension() {
        Pension pension = new Pension();
        pension.setPensionId(3L);
        pension.setUserId(42L);
        when(pensionRepository.findByPensionIdAndUserId(3L, 42L)).thenReturn(Optional.of(pension));

        assertThat(service.getForUser(42L, 3L).getPensionId()).isEqualTo(3L);
        assertThatThrownBy(() -> service.getForUser(99L, 3L))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void updateAppliesChangesAndAudits() {
        Pension existing = new Pension();
        existing.setPensionId(5L);
        existing.setUserId(42L);
        existing.setName("Old Name");
        existing.setStatus(PensionStatus.ACTIVE);
        existing.setStatusDate(LocalDate.of(2026, 1, 1));
        when(pensionRepository.findByPensionIdAndUserId(5L, 42L)).thenReturn(Optional.of(existing));
        when(pensionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PensionRequest request = new PensionRequest("New Name", LocalDate.of(2046, 6, 30),
                PensionStatus.ACTIVE, "some notes", "#abcdef", null, null, null, null);

        Pension result = service.update(42L, 5L, request);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getNotes()).isEqualTo("some notes");
        assertThat(result.getStatusDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        verify(auditService).recordUpdate(existing);
    }

    @Test
    void updateStampsStatusDateWhenStatusChanges() {
        Pension existing = new Pension();
        existing.setPensionId(5L);
        existing.setUserId(42L);
        existing.setStatus(PensionStatus.ACTIVE);
        existing.setStatusDate(LocalDate.of(2026, 1, 1));
        when(pensionRepository.findByPensionIdAndUserId(5L, 42L)).thenReturn(Optional.of(existing));
        when(pensionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PensionRequest request = new PensionRequest("P", LocalDate.of(2046, 6, 30),
                PensionStatus.CLOSED, null, null, null, null, null, null);

        Pension result = service.update(42L, 5L, request);

        assertThat(result.getStatus()).isEqualTo(PensionStatus.CLOSED);
        assertThat(result.getStatusDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void updateThrowsNotFoundForOtherUsersPension() {
        when(pensionRepository.findByPensionIdAndUserId(5L, 42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(42L, 5L,
                new PensionRequest("N", LocalDate.of(2046, 6, 30), PensionStatus.ACTIVE, null, null, null, null, null, null)))
                .isInstanceOf(ApiException.class);
        verify(pensionRepository, never()).save(any());
    }

    @Test
    void deleteAuditsAndRemovesStatementsAndPension() {
        Pension pension = new Pension();
        pension.setPensionId(5L);
        pension.setUserId(42L);
        when(pensionRepository.findByPensionIdAndUserId(5L, 42L)).thenReturn(Optional.of(pension));

        PensionStatement statement = new PensionStatement();
        statement.setStatementId(1L);
        statement.setPensionId(5L);
        when(statementRepository.findByPensionIdOrderByStatementDateAsc(5L)).thenReturn(List.of(statement));

        service.delete(42L, 5L);

        verify(auditService).recordDelete(42L, statement);
        verify(statementRepository).deleteAll(List.of(statement));
        verify(auditService).recordDelete(pension);
        verify(pensionRepository).delete(pension);
    }

    @Test
    void deleteWithoutStatementsStillAuditsPension() {
        Pension pension = new Pension();
        pension.setPensionId(5L);
        pension.setUserId(42L);
        when(pensionRepository.findByPensionIdAndUserId(5L, 42L)).thenReturn(Optional.of(pension));
        when(statementRepository.findByPensionIdOrderByStatementDateAsc(5L)).thenReturn(List.of());

        service.delete(42L, 5L);

        verify(statementRepository, never()).deleteAll(any());
        verify(auditService).recordDelete(pension);
    }

    @Test
    void listForUserScopedToUser() {
        when(pensionRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of(new Pension()));

        assertThat(service.listForUser(42L)).hasSize(1);
        verify(pensionRepository).findByUserIdOrderByNameAsc(42L);
    }
}
