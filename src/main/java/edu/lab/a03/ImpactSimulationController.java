
package edu.lab.a03;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
public class ImpactSimulationController {

    private static final Path EVIDENCE_FILE =
            Path.of("/tmp/ejemplo.hack.txt");

    @PostMapping("/api/demo/impact")
    public Map<String, Object> simulateImpact() {

        // Simulación inocua: no representa una
        // explotación real de Log4Shell.
        String content = "1".repeat(64);

        try {
            Files.writeString(
                    EVIDENCE_FILE,
                    content,
                    StandardCharsets.US_ASCII
            );
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not create simulation evidence",
                    exception
            );
        }

        return Map.of(
                "result", "SIMULATED",
                "file", EVIDENCE_FILE.toString(),
                "bytes", 64
        );
    }
}
