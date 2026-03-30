package com.github.khopland.dependency.snapshot.dto;

import java.util.List;

public record ProjectSnapshot(
        String groupId,
        String artifactId,
        String version,
        List<DependencyCoordinate> dependencies
) implements Comparable<ProjectSnapshot> {

    public ProjectSnapshot {
        dependencies = List.copyOf(dependencies == null ? List.of() : dependencies.stream().sorted().toList());
    }

    public String projectKey() {
        return groupId + ":" + artifactId + ":" + version;
    }

    @Override
    public int compareTo(ProjectSnapshot other) {
        return projectKey().compareTo(other.projectKey());
    }
}
