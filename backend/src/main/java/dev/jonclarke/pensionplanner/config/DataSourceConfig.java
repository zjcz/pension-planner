package dev.jonclarke.pensionplanner.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class DataSourceConfig {

    @Bean
    public HikariDataSource dataSource(@Value("${DATABASE_PATH:/data/pension.db}") String databasePath) {
        ensureParentDirectoryExists(databasePath);
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + databasePath);
        config.setDriverClassName("org.sqlite.JDBC");
        config.setMaximumPoolSize(5);
        config.setConnectionInitSql("PRAGMA foreign_keys=ON");
        return new HikariDataSource(config);
    }

    private static void ensureParentDirectoryExists(String databasePath) {
        Path path = Path.of(databasePath);
        if (path.getParent() != null) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                throw new UncheckedIOException("Could not create database directory for " + databasePath, e);
            }
        }
    }
}
