package com.github.khopland.dependency.snapshot;

import com.github.khopland.dependency.snapshot.dto.DependencyCoordinate;
import com.github.khopland.dependency.snapshot.dto.ProjectSnapshot;
import com.github.khopland.dependency.snapshot.dto.SnapshotDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnapshotRepositoryTest {

    private final SnapshotRepository repository = new SnapshotRepository();

    @TempDir
    Path tempDir;

    @Test
    void writesAndReadsSnapshotDocuments() throws Exception {
        SnapshotDocument snapshot = new SnapshotDocument(
                SnapshotDocument.CURRENT_SCHEMA_VERSION,
                "module",
                java.util.List.of(new ProjectSnapshot(
                        "com.example",
                        "demo",
                        "1.0.0",
                        java.util.List.of(new DependencyCoordinate("org.alpha", "one", "1.0.0", "jar", "", "compile"))
                ))
        );

        Path snapshotPath = repository.aggregateSnapshotPath(tempDir);
        repository.write(snapshotPath, snapshot);

        Optional<SnapshotDocument> reloaded = repository.read(snapshotPath);

        assertTrue(reloaded.isPresent());
        assertEquals(snapshot, reloaded.get());
    }

    @Test
    void sanitizesModuleSnapshotFileNames() {
        ProjectSnapshot snapshot = new ProjectSnapshot("com.example/team", "demo module", "1.0.0", java.util.List.of());

        Path snapshotPath = repository.moduleSnapshotPath(tempDir, snapshot);

        assertEquals("com.example_team_demo_module-deps.json", snapshotPath.getFileName().toString());
    }
}
