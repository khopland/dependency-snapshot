package com.github.khopland.dependencysnapshot;

import java.util.List;

public record SnapshotDocument(
        int schemaVersion,
        String mode,
        List<ProjectSnapshot> projects
) {

    public static final int CURRENT_SCHEMA_VERSION = 1;

    public SnapshotDocument {
        projects = List.copyOf(projects == null ? List.of() : projects.stream().sorted().toList());
    }
}
