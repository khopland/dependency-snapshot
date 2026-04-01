package io.github.khopland.dependency.snapshot;

import io.github.khopland.dependency.snapshot.dto.*;

final class DiffReportFormatter {

    String format(DiffReportModel report) {
        StringBuilder builder = new StringBuilder();
        builder.append("Dependency diff summary: ")
                .append(report.summary().addedDependencies()).append(" added, ")
                .append(report.summary().removedDependencies()).append(" removed, ")
                .append(report.summary().changedDependencies()).append(" changed dependencies; ")
                .append(report.summary().addedProjects()).append(" added, ")
                .append(report.summary().removedProjects()).append(" removed projects.")
                .append(System.lineSeparator());

        if (!report.addedProjects().isEmpty()) {
            builder.append(System.lineSeparator()).append("Added projects").append(System.lineSeparator());
            for (ProjectSnapshot project : report.addedProjects()) {
                builder.append("  + ").append(project.projectKey()).append(System.lineSeparator());
            }
        }

        if (!report.removedProjects().isEmpty()) {
            builder.append(System.lineSeparator()).append("Removed projects").append(System.lineSeparator());
            for (ProjectSnapshot project : report.removedProjects()) {
                builder.append("  - ").append(project.projectKey()).append(System.lineSeparator());
            }
        }

        for (ProjectDiff projectDiff : report.projectDiffs()) {
            builder.append(System.lineSeparator()).append("Project ").append(projectDiff.projectKey()).append(System.lineSeparator());

            if (!projectDiff.addedDependencies().isEmpty()) {
                builder.append("  Added").append(System.lineSeparator());
                for (DependencyCoordinate dependency : projectDiff.addedDependencies()) {
                    builder.append("    + ").append(dependency.displayNotation()).append(System.lineSeparator());
                }
            }

            if (!projectDiff.removedDependencies().isEmpty()) {
                builder.append("  Removed").append(System.lineSeparator());
                for (DependencyCoordinate dependency : projectDiff.removedDependencies()) {
                    builder.append("    - ").append(dependency.displayNotation()).append(System.lineSeparator());
                }
            }

            if (!projectDiff.changedDependencies().isEmpty()) {
                builder.append("  Changed").append(System.lineSeparator());
                for (DependencyChange change : projectDiff.changedDependencies()) {
                    builder.append("    ~ ")
                            .append(change.after().identityKey())
                            .append(" version ").append(change.before().version()).append(" -> ").append(change.after().version())
                            .append(", scope ").append(blankAsDash(change.before().scope())).append(" -> ").append(blankAsDash(change.after().scope()))
                            .append(System.lineSeparator());
                }
            }
        }

        return builder.toString().stripTrailing();
    }

    private String blankAsDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
