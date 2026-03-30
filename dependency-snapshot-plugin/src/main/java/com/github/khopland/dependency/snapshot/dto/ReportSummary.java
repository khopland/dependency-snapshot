package com.github.khopland.dependency.snapshot.dto;

public record ReportSummary(
        int addedDependencies,
        int removedDependencies,
        int changedDependencies,
        int addedProjects,
        int removedProjects
) {
}
