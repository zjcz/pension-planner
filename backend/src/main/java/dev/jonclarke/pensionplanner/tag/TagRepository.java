package dev.jonclarke.pensionplanner.tag;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByUserIdOrderByNameAsc(Long userId);

    List<Tag> findByIdInAndUserId(List<Long> ids, Long userId);
}
