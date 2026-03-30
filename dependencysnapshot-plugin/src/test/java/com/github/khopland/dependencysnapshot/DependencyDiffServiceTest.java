package com.github.khopland.dependencysnapshot;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DependencyDiffServiceTest {

    private final DependencyDiffService service = new DependencyDiffService();

    @Test
    void detectsAddedRemovedAndChangedDependencies() {
        SnapshotDocument previous = new SnapshotDocument(
                SnapshotDocument.CURRENT_SCHEMA_VERSION,
                "aggregate",
                List.of(new ProjectSnapshot(
                        "com.example",
                        "demo",
                        "1.0.0",
                        List.of(
                                new DependencyCoordinate("org.alpha", "one", "1.0.0", "jar", "", "compile"),
                                new DependencyCoordinate("org.beta", "two", "1.0.0", "jar", "", "runtime")
                        )
                ))
        );

        SnapshotDocument current = new SnapshotDocument(
                SnapshotDocument.CURRENT_SCHEMA_VERSION,
                "aggregate",
                List.of(new ProjectSnapshot(
                        "com.example",
                        "demo",
                        "1.0.0",
                        List.of(
                                new DependencyCoordinate("org.alpha", "one", "2.0.0", "jar", "", "compile"),
                                new DependencyCoordinate("org.gamma", "three", "1.1.0", "jar", "", "runtime")
                        )
                ))
        );

        DiffReportModel report = service.diff(previous, current);

        assertTrue(report.hasChanges());
        assertEquals(1, report.summary().addedDependencies());
        assertEquals(1, report.summary().removedDependencies());
        assertEquals(1, report.summary().changedDependencies());
        assertEquals(1, report.projectDiffs().size());
    }

    @Test
    void reportsProjectAdditionsAndRemovals() {
        SnapshotDocument previous = new SnapshotDocument(
                SnapshotDocument.CURRENT_SCHEMA_VERSION,
                "aggregate",
                List.of(new ProjectSnapshot("com.example", "old-module", "1.0.0", List.of()))
        );

        SnapshotDocument current = new SnapshotDocument(
                SnapshotDocument.CURRENT_SCHEMA_VERSION,
                "aggregate",
                List.of(new ProjectSnapshot("com.example", "new-module", "1.0.0", List.of()))
        );

        DiffReportModel report = service.diff(previous, current);

        assertTrue(report.hasChanges());
        assertEquals(1, report.summary().addedProjects());
        assertEquals(1, report.summary().removedProjects());
        assertTrue(report.projectDiffs().isEmpty());
    }

    @Test
    void producesEmptyReportWithoutChanges() {
        SnapshotDocument snapshot = new SnapshotDocument(
                SnapshotDocument.CURRENT_SCHEMA_VERSION,
                "module",
                List.of(new ProjectSnapshot(
                        "com.example",
                        "demo",
                        "1.0.0",
                        List.of(new DependencyCoordinate("org.alpha", "one", "1.0.0", "jar", "", "compile"))
                ))
        );

        DiffReportModel report = service.diff(snapshot, snapshot);

        assertFalse(report.hasChanges());
        assertEquals(0, report.summary().addedDependencies());
        assertEquals(0, report.summary().removedDependencies());
        assertEquals(0, report.summary().changedDependencies());
    }
}
