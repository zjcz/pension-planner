package dev.jonclarke.pensionplanner;

import dev.jonclarke.pensionplanner.config.NativeRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@ImportRuntimeHints(NativeRuntimeHints.Registrar.class)
public class PensionPlannerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PensionPlannerApplication.class, args);
    }
}
