package com.pensionplanner.income;

import com.pensionplanner.audit.AuditService;
import com.pensionplanner.common.ApiException;
import com.pensionplanner.tag.OtherIncomeTagRepository;
import com.pensionplanner.tag.TagRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OtherIncomeServiceTest {

    private final OtherIncomeRepository otherIncomeRepository = mock(OtherIncomeRepository.class);
    private final OtherIncomeTagRepository otherIncomeTagRepository = mock(OtherIncomeTagRepository.class);
    private final TagRepository tagRepository = mock(TagRepository.class);
    private final AuditService auditService = mock(AuditService.class);
    private final OtherIncomeService service = new OtherIncomeService(
            otherIncomeRepository, otherIncomeTagRepository, tagRepository, auditService);

    @Test
    void listForUserReturnsAll() {
        OtherIncome oi = new OtherIncome();
        oi.setId(1L);
        when(otherIncomeRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of(oi));

        assertThat(service.listForUser(42L)).hasSize(1);
    }

    @Test
    void getForUserThrowsWhenNotFound() {
        when(otherIncomeRepository.findByIdAndUserId(1L, 42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getForUser(42L, 1L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void getForUserReturnsExisting() {
        OtherIncome oi = new OtherIncome();
        oi.setId(1L);
        when(otherIncomeRepository.findByIdAndUserId(1L, 42L)).thenReturn(Optional.of(oi));

        assertThat(service.getForUser(42L, 1L).getId()).isEqualTo(1L);
    }

    @Test
    void createSetsFieldsAndAudits() {
        when(otherIncomeRepository.save(any())).thenAnswer(invocation -> {
            OtherIncome oi = invocation.getArgument(0);
            oi.setId(1L);
            return oi;
        });

        OtherIncomeRequest request = new OtherIncomeRequest("Rental", 5000L, "Some notes", null);
        OtherIncome result = service.create(42L, request);

        assertThat(result.getUserId()).isEqualTo(42L);
        assertThat(result.getName()).isEqualTo("Rental");
        assertThat(result.getAnnualAmount()).isEqualTo(5000L);
        assertThat(result.getNotes()).isEqualTo("Some notes");
        verify(auditService).recordCreate(result);
    }

    @Test
    void updateAuditsBeforeMutating() {
        OtherIncome existing = new OtherIncome();
        existing.setId(1L);
        existing.setUserId(42L);
        existing.setName("Old");
        when(otherIncomeRepository.findByIdAndUserId(1L, 42L)).thenReturn(Optional.of(existing));
        when(otherIncomeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        OtherIncomeRequest request = new OtherIncomeRequest("New", 8000L, null, null);
        OtherIncome result = service.update(42L, 1L, request);

        verify(auditService).recordUpdate(existing);
        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getAnnualAmount()).isEqualTo(8000L);
    }

    @Test
    void deleteAuditsThenRemoves() {
        OtherIncome existing = new OtherIncome();
        existing.setId(1L);
        when(otherIncomeRepository.findByIdAndUserId(1L, 42L)).thenReturn(Optional.of(existing));

        service.delete(42L, 1L);

        verify(auditService).recordDelete(existing);
        verify(otherIncomeRepository).delete(existing);
    }

    @Test
    void deleteThrowsWhenNotFound() {
        when(otherIncomeRepository.findByIdAndUserId(99L, 42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(42L, 99L))
                .isInstanceOf(ApiException.class);
    }
}
