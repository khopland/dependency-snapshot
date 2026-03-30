package com.github.khopland.dependencysnapshot;

import java.time.Instant;
import java.util.List;

public record DiffReportModel(
        int schemaVersion,
        Instant generatedAt,
        String mode,
        boolean hasChanges,
        ReportSummary summary,
        List<ProjectDiff> projectDiffs,
        List<ProjectSnapshot> addedProjects,
        List<ProjectSnapshot> removedProjects
) {

    public DiffReportModel {
        projectDiffs = List.copyOf(projectDiffs == null ? List.of() : projectDiffs.stream().sorted().toList());
        addedProjects = List.copyOf(addedProjects == null ? List.of() : addedProjects.stream().sorted().toList());
        removedProjects = List.copyOf(removedProjects == null ? List.of() : removedProjects.stream().sorted().toList());
    }
}
