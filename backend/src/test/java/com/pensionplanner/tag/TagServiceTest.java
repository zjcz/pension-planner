package com.pensionplanner.tag;

import com.pensionplanner.common.ApiException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TagServiceTest {

    private final TagRepository tagRepository = mock(TagRepository.class);
    private final PensionTagRepository pensionTagRepository = mock(PensionTagRepository.class);
    private final OtherIncomeTagRepository otherIncomeTagRepository = mock(OtherIncomeTagRepository.class);
    private final TagService service = new TagService(tagRepository, pensionTagRepository, otherIncomeTagRepository);

    @Test
    void listForUserScopedToUser() {
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setUserId(42L);
        tag.setName("Work");
        when(tagRepository.findByUserIdOrderByNameAsc(42L)).thenReturn(List.of(tag));

        assertThat(service.listForUser(42L)).hasSize(1);
        assertThat(service.listForUser(42L).get(0).getName()).isEqualTo("Work");
    }

    @Test
    void createSavesAndReturnsTag() {
        when(tagRepository.save(any())).thenAnswer(invocation -> {
            Tag t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        Tag result = service.create(42L, "  Work  ");

        assertThat(result.getUserId()).isEqualTo(42L);
        assertThat(result.getName()).isEqualTo("Work");
        verify(tagRepository).save(any());
    }

    @Test
    void deleteRemovesTagAndJoinRecords() {
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setUserId(42L);
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(pensionTagRepository.findByTagId(1L)).thenReturn(List.of());
        when(otherIncomeTagRepository.findByTagId(1L)).thenReturn(List.of());

        service.delete(42L, 1L);

        verify(tagRepository).delete(tag);
    }

    @Test
    void deleteThrowsWhenNotFound() {
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(42L, 99L))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void deleteThrowsWhenOwnedByDifferentUser() {
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setUserId(99L);
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        assertThatThrownBy(() -> service.delete(42L, 1L))
                .isInstanceOf(ApiException.class);
    }
}
