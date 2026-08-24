package com.pensionplanner.pension;

import com.pensionplanner.security.CurrentUserService;
import com.pensionplanner.tag.TagDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pensions")
public class PensionController {

    private final PensionService pensionService;
    private final CurrentUserService currentUserService;

    public PensionController(PensionService pensionService, CurrentUserService currentUserService) {
        this.pensionService = pensionService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<PensionDto> list() {
        Long userId = currentUserService.currentUserId();
        List<Pension> pensions = pensionService.listForUser(userId);
        List<Long> pensionIds = pensions.stream().map(Pension::getPensionId).toList();
        java.util.Map<Long, List<TagDto>> tagsByPension = pensionService.getTagsForPensionsByPensionId(pensionIds);
        return pensions.stream()
                .map(p -> PensionDto.from(p, tagsByPension.getOrDefault(p.getPensionId(), List.of())))
                .toList();
    }

    @GetMapping("/{pensionId}")
    public PensionDto get(@PathVariable Long pensionId) {
        Long userId = currentUserService.currentUserId();
        Pension pension = pensionService.getForUser(userId, pensionId);
        List<TagDto> tags = pensionService.getTagsForPension(pensionId);
        return PensionDto.from(pension, tags);
    }

    @PostMapping
    public ResponseEntity<PensionDto> create(@Valid @RequestBody PensionRequest request) {
        Long userId = currentUserService.currentUserId();
        Pension pension = pensionService.create(userId, request);
        List<TagDto> tags = pensionService.getTagsForPension(pension.getPensionId());
        return ResponseEntity.status(HttpStatus.CREATED).body(PensionDto.from(pension, tags));
    }

    @PutMapping("/{pensionId}")
    public PensionDto update(@PathVariable Long pensionId, @Valid @RequestBody PensionRequest request) {
        Long userId = currentUserService.currentUserId();
        Pension pension = pensionService.update(userId, pensionId, request);
        List<TagDto> tags = pensionService.getTagsForPension(pensionId);
        return PensionDto.from(pension, tags);
    }

    @DeleteMapping("/{pensionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long pensionId) {
        pensionService.delete(currentUserService.currentUserId(), pensionId);
    }
}
