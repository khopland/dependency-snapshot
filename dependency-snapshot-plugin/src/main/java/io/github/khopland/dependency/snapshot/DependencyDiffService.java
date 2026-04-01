package io.github.khopland.dependency.snapshot;

import io.github.khopland.dependency.snapshot.dto.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class DependencyDiffService {

    DiffReportModel diff(SnapshotDocument previous, SnapshotDocument current) {
        Map<String, ProjectSnapshot> previousProjects = indexProjects(previous.projects());
        Map<String, ProjectSnapshot> currentProjects = indexProjects(current.projects());

        List<ProjectSnapshot> addedProjects = new ArrayList<>();
        List<ProjectSnapshot> removedProjects = new ArrayList<>();
        List<ProjectDiff> projectDiffs = new ArrayList<>();

        for (ProjectSnapshot currentProject : current.projects()) {
            ProjectSnapshot previousProject = previousProjects.get(currentProject.projectKey());
            if (previousProject == null) {
                addedProjects.add(currentProject);
                continue;
            }

            ProjectDiff projectDiff = diffProject(previousProject, currentProject);
            if (projectDiff.hasChanges()) {
                projectDiffs.add(projectDiff);
            }
        }

        for (ProjectSnapshot previousProject : previous.projects()) {
            if (!currentProjects.containsKey(previousProject.projectKey())) {
                removedProjects.add(previousProject);
            }
        }

        int addedDependencies = projectDiffs.stream().mapToInt(projectDiff -> projectDiff.addedDependencies().size()).sum();
        int removedDependencies = projectDiffs.stream().mapToInt(projectDiff -> projectDiff.removedDependencies().size()).sum();
        int changedDependencies = projectDiffs.stream().mapToInt(projectDiff -> projectDiff.changedDependencies().size()).sum();

        ReportSummary summary = new ReportSummary(
                addedDependencies,
                removedDependencies,
                changedDependencies,
                addedProjects.size(),
                removedProjects.size()
        );

        boolean hasChanges = addedDependencies > 0
                || removedDependencies > 0
                || changedDependencies > 0
                || !addedProjects.isEmpty()
                || !removedProjects.isEmpty();

        return new DiffReportModel(
                SnapshotDocument.CURRENT_SCHEMA_VERSION,
                Instant.now(),
                current.mode(),
                hasChanges,
                summary,
                projectDiffs,
                addedProjects,
                removedProjects
        );
    }

    DiffReportModel emptyReport(String mode) {
        return new DiffReportModel(
                SnapshotDocument.CURRENT_SCHEMA_VERSION,
                Instant.now(),
                mode,
                false,
                new ReportSummary(0, 0, 0, 0, 0),
                List.of(),
                List.of(),
                List.of()
        );
    }

    private ProjectDiff diffProject(ProjectSnapshot previousProject, ProjectSnapshot currentProject) {
        Map<String, DependencyCoordinate> previousDependencies = indexDependencies(previousProject.dependencies());
        Map<String, DependencyCoordinate> currentDependencies = indexDependencies(currentProject.dependencies());

        List<DependencyCoordinate> addedDependencies = new ArrayList<>();
        List<DependencyCoordinate> removedDependencies = new ArrayList<>();
        List<DependencyChange> changedDependencies = new ArrayList<>();

        for (DependencyCoordinate currentDependency : currentProject.dependencies()) {
            DependencyCoordinate previousDependency = previousDependencies.get(currentDependency.identityKey());
            if (previousDependency == null) {
                addedDependencies.add(currentDependency);
                continue;
            }

            if (!previousDependency.sameCoordinates(currentDependency)) {
                changedDependencies.add(new DependencyChange(previousDependency, currentDependency));
            }
        }

        for (DependencyCoordinate previousDependency : previousProject.dependencies()) {
            if (!currentDependencies.containsKey(previousDependency.identityKey())) {
                removedDependencies.add(previousDependency);
            }
        }

        return new ProjectDiff(
                currentProject.groupId(),
                currentProject.artifactId(),
                currentProject.version(),
                addedDependencies,
                removedDependencies,
                changedDependencies
        );
    }

    private Map<String, ProjectSnapshot> indexProjects(List<ProjectSnapshot> projects) {
        Map<String, ProjectSnapshot> indexed = new LinkedHashMap<>();
        for (ProjectSnapshot project : projects) {
            indexed.put(project.projectKey(), project);
        }
        return indexed;
    }

    private Map<String, DependencyCoordinate> indexDependencies(List<DependencyCoordinate> dependencies) {
        Map<String, DependencyCoordinate> indexed = new LinkedHashMap<>();
        for (DependencyCoordinate dependency : dependencies) {
            indexed.put(dependency.identityKey(), dependency);
        }
        return indexed;
    }
}
