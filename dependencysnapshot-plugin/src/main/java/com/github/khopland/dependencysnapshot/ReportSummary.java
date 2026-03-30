package com.github.khopland.dependencysnapshot;

public record ReportSummary(
        int addedDependencies,
        int removedDependencies,
        int changedDependencies,
        int addedProjects,
        int removedProjects
) {
}
