package io.github.khopland.dependency.snapshot;

import io.github.khopland.dependency.snapshot.dto.DiffReportModel;
import io.github.khopland.dependency.snapshot.dto.ReportSummary;
import io.github.khopland.dependency.snapshot.dto.SnapshotDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiffReportWriterTest {

    private final DiffReportWriter writer = new DiffReportWriter();

    @TempDir
    Path tempDir;

    @Test
    void writesTextAndJsonReports() throws Exception {
        DiffReportModel report = new DiffReportModel(
                SnapshotDocument.CURRENT_SCHEMA_VERSION,
                Instant.parse("2026-03-30T10:15:30Z"),
                "aggregate",
                true,
                new ReportSummary(1, 0, 0, 0, 0),
                List.of(),
                List.of(),
                List.of()
        );

        writer.writeReports(report, tempDir, "dependency-diff", "example text", true, true);

        assertTrue(Files.exists(tempDir.resolve("dependency-diff.txt")));
        assertTrue(Files.exists(tempDir.resolve("dependency-diff.json")));
    }

    @Test
    void skipsWritingWhenAllFormatsAreDisabled() throws Exception {
        DiffReportModel report = new DiffReportModel(
                SnapshotDocument.CURRENT_SCHEMA_VERSION,
                Instant.parse("2026-03-30T10:15:30Z"),
                "aggregate",
                false,
                new ReportSummary(0, 0, 0, 0, 0),
                List.of(),
                List.of(),
                List.of()
        );

        writer.writeReports(report, tempDir, "dependency-diff", "example text", false, false);

        assertFalse(Files.exists(tempDir.resolve("dependency-diff.txt")));
        assertFalse(Files.exists(tempDir.resolve("dependency-diff.json")));
    }
}
