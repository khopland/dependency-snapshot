package com.github.khopland.dependency.snapshot;

import com.github.khopland.dependency.snapshot.dto.DependencyCoordinate;
import com.github.khopland.dependency.snapshot.dto.ProjectSnapshot;
import com.github.khopland.dependency.snapshot.dto.SnapshotDocument;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import java.util.List;
import java.util.Set;

final class DependencyCollector {

    SnapshotDocument collectAggregate(List<MavenProject> projects) {
        return new SnapshotDocument(
                SnapshotDocument.CURRENT_SCHEMA_VERSION,
                "aggregate",
                projects.stream().map(this::toProjectSnapshot).toList()
        );
    }

    SnapshotDocument collectModule(MavenProject project) {
        return new SnapshotDocument(
                SnapshotDocument.CURRENT_SCHEMA_VERSION,
                "module",
                List.of(toProjectSnapshot(project))
        );
    }

    ProjectSnapshot toProjectSnapshot(MavenProject project) {
        Set<Artifact> artifacts = project.getArtifacts();
        List<DependencyCoordinate> dependencies = artifacts == null
                ? List.of()
                : artifacts.stream().map(this::toDependencyCoordinate).sorted().toList();

        return new ProjectSnapshot(
                project.getGroupId(),
                project.getArtifactId(),
                project.getVersion(),
                dependencies
        );
    }

    private DependencyCoordinate toDependencyCoordinate(Artifact artifact) {
        return new DependencyCoordinate(
                artifact.getGroupId(),
                artifact.getArtifactId(),
                artifact.getVersion(),
                artifact.getType(),
                artifact.getClassifier(),
                artifact.getScope()
        );
    }
}
