package io.github.khopland.dependency.snapshot.dto;

import java.util.List;

public record ProjectDiff(
        String groupId,
        String artifactId,
        String version,
        List<DependencyCoordinate> addedDependencies,
        List<DependencyCoordinate> removedDependencies,
        List<DependencyChange> changedDependencies
) implements Comparable<ProjectDiff> {

    public ProjectDiff {
        addedDependencies = List.copyOf(addedDependencies == null ? List.of() : addedDependencies.stream().sorted().toList());
        removedDependencies = List.copyOf(removedDependencies == null ? List.of() : removedDependencies.stream().sorted().toList());
        changedDependencies = List.copyOf(changedDependencies == null ? List.of() : changedDependencies.stream().sorted().toList());
    }

    public String projectKey() {
        return groupId + ":" + artifactId + ":" + version;
    }

    public boolean hasChanges() {
        return !addedDependencies.isEmpty() || !removedDependencies.isEmpty() || !changedDependencies.isEmpty();
    }

    @Override
    public int compareTo(ProjectDiff other) {
        return projectKey().compareTo(other.projectKey());
    }
}
