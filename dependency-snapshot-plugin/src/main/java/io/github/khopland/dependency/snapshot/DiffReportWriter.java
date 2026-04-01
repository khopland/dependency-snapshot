package io.github.khopland.dependency.snapshot;


import io.github.khopland.dependency.snapshot.dto.DiffReportModel;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

 final class DiffReportWriter {

    private final ObjectMapper objectMapper = ObjectMapperFactory.create();

    void writeReports(
            DiffReportModel report,
            Path outputDirectory,
            String fileStem,
            String textReport,
            boolean writeTextReport,
            boolean writeJsonReport
    ) throws IOException {
        if (!writeTextReport && !writeJsonReport) {
            return;
        }

        Files.createDirectories(outputDirectory);

        if (writeTextReport) {
            Files.writeString(outputDirectory.resolve(fileStem + ".txt"), textReport, StandardCharsets.UTF_8);
        }

        if (writeJsonReport) {
            objectMapper.writeValue(outputDirectory.resolve(fileStem + ".json").toFile(), report);
        }
    }
}
