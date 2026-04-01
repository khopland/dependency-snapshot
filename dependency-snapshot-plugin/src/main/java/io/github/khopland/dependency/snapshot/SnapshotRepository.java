package io.github.khopland.dependency.snapshot;


import io.github.khopland.dependency.snapshot.dto.ProjectSnapshot;
import io.github.khopland.dependency.snapshot.dto.SnapshotDocument;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

final class SnapshotRepository {

    private final ObjectMapper objectMapper = ObjectMapperFactory.create();

    Optional<SnapshotDocument> read(Path snapshotPath) {
        if (!Files.exists(snapshotPath)) {
            return Optional.empty();
        }

        return Optional.of(objectMapper.readValue(snapshotPath.toFile(), SnapshotDocument.class));
    }

    void write(Path snapshotPath, SnapshotDocument snapshotDocument) throws IOException {
        Files.createDirectories(snapshotPath.getParent());
        objectMapper.writeValue(snapshotPath.toFile(), snapshotDocument);
    }

    Path aggregateSnapshotPath(Path snapshotDirectory) {
        return snapshotDirectory.resolve("agregate-deps.json");
    }

    Path moduleSnapshotPath(Path snapshotDirectory, ProjectSnapshot projectSnapshot) {
        return snapshotDirectory.resolve(FileNameHelper.moduleFileStem(projectSnapshot) + "-deps.json");
    }
}
