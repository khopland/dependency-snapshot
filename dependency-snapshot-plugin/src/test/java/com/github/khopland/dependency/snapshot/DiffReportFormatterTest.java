package com.github.khopland.dependency.snapshot;

import com.github.khopland.dependency.snapshot.dto.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DiffReportFormatterTest {

    private final DiffReportFormatter formatter = new DiffReportFormatter();

    @Test
    void formatsDependencyChangesIntoReadableSections() {
        DiffReportModel report = new DiffReportModel(
                SnapshotDocument.CURRENT_SCHEMA_VERSION,
                Instant.parse("2026-03-30T10:15:30Z"),
                "aggregate",
                true,
                new ReportSummary(1, 0, 1, 1, 0),
                List.of(new ProjectDiff(
                        "com.example",
                        "demo",
                        "1.0.0",
                        List.of(new DependencyCoordinate("org.gamma", "three", "1.1.0", "jar", "", "runtime")),
                        List.of(),
                        List.of(new DependencyChange(
                                new DependencyCoordinate("org.alpha", "one", "1.0.0", "jar", "", "compile"),
                                new DependencyCoordinate("org.alpha", "one", "2.0.0", "jar", "", "runtime")
                        ))
                )),
                List.of(new ProjectSnapshot("com.example", "new-module", "1.0.0", List.of())),
                List.of()
        );

        String text = formatter.format(report);

        assertTrue(text.contains("Dependency diff summary: 1 added, 0 removed, 1 changed dependencies; 1 added, 0 removed projects."));
        assertTrue(text.contains("Added projects"));
        assertTrue(text.contains("Project com.example:demo:1.0.0"));
        assertTrue(text.contains("+ org.gamma:three:jar:1.1.0 [runtime]"));
        assertTrue(text.contains("version 1.0.0 -> 2.0.0, scope compile -> runtime"));
    }
}
