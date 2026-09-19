package dev.jonclarke.pensionplanner.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/info")
public class InfoController {

    private final String name;
    private final String version;

    public InfoController(@Value("${app.name}") String name,
                          @Value("${app.version}") String version) {
        this.name = name;
        this.version = version;
    }

    @GetMapping
    public Map<String, String> info() {
        return Map.of(
                "name", name,
                "version", version
        );
    }
}
