package dev.jonclarke.pensionplanner.export;

import dev.jonclarke.pensionplanner.security.CurrentUserService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/export")
public class ExportController {

    private static final MediaType APPLICATION_ZIP = MediaType.parseMediaType("application/zip");
    private static final DateTimeFormatter FILENAME_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private final ExportService exportService;
    private final CurrentUserService currentUserService;

    public ExportController(ExportService exportService, CurrentUserService currentUserService) {
        this.exportService = exportService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<byte[]> exportData(
            @RequestParam ExportFormat format,
            @RequestParam ExportType type,
            @RequestParam(defaultValue = "true") boolean includeStatements,
            @RequestParam(required = false) Long pensionId) {
        Long userId = currentUserService.currentUserId();
        byte[] zipBytes = exportService.exportData(userId, format, type, includeStatements, pensionId);

        String timestamp = LocalDateTime.now().format(FILENAME_TIMESTAMP);
        String filename = "pension-planner-" + type.name().toLowerCase().replace('_', '-') + "-export-" + timestamp + ".zip";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_ZIP);
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(zipBytes);
    }
}
