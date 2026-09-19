package dev.jonclarke.pensionplanner.common;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@RestController
public class HealthController {

    private final HikariDataSource dataSource;

    public HealthController(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) {
                return ResponseEntity.ok(Map.of("status", "UP"));
            }
            return ResponseEntity.status(503).body(Map.of("status", "DOWN"));
        } catch (SQLException e) {
            return ResponseEntity.status(503).body(Map.of("status", "DOWN"));
        }
    }
}