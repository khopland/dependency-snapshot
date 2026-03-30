package com.github.khopland.dependency.snapshot;

import com.github.khopland.dependency.snapshot.dto.DiffReportModel;
import com.github.khopland.dependency.snapshot.dto.ProjectSnapshot;
import com.github.khopland.dependency.snapshot.dto.SnapshotDocument;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Mojo(
        name = "snapshot",
        defaultPhase = LifecyclePhase.VERIFY,
        aggregator = true,
        requiresDependencyResolution = ResolutionScope.TEST,
        threadSafe = true
)
public class DependencySnapshotMojo extends AbstractMojo {

    private final DependencyCollector dependencyCollector = new DependencyCollector();
    private final SnapshotRepository snapshotRepository = new SnapshotRepository();
    private final DependencyDiffService dependencyDiffService = new DependencyDiffService();
    private final DiffReportFormatter diffReportFormatter = new DiffReportFormatter();
    private final DiffReportWriter diffReportWriter = new DiffReportWriter();

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Parameter(defaultValue = "${reactorProjects}", readonly = true, required = true)
    private List<MavenProject> reactorProjects;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(property = "dependency-snapshot.aggregate", defaultValue = "true")
    private boolean aggregate;

    @Parameter(property = "dependency-snapshot.failOnChange", defaultValue = "false")
    private boolean failOnChange;

    @Parameter(
            property = "dependency-snapshot.snapshotDirectory",
            defaultValue = "${session.executionRootDirectory}/.mvn/dependency-snapshot"
    )
    private File snapshotDirectory;

    @Parameter(property = "dependency-snapshot.skip", defaultValue = "false")
    private boolean skip;

    @Parameter(property = "dependency-snapshot.createSnapshotIfMissing", defaultValue = "true")
    private boolean createSnapshotIfMissing;

    @Parameter(property = "dependency-snapshot.writeReportFiles", defaultValue = "true")
    private boolean writeReportFiles;

    @Parameter(property = "dependency-snapshot.writeReportsOnNoChange", defaultValue = "false")
    private boolean writeReportsOnNoChange;

    @Parameter(
            property = "dependency-snapshot.reportOutputDirectory",
            defaultValue = "${session.executionRootDirectory}/target/dependency-snapshot"
    )
    private File reportOutputDirectory;

    @Parameter(property = "dependency-snapshot.writeTextReport", defaultValue = "true")
    private boolean writeTextReport;

    @Parameter(property = "dependency-snapshot.writeJsonReport", defaultValue = "true")
    private boolean writeJsonReport;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        applyUserPropertyOverrides();

        if (skip) {
            getLog().info("Skipping dependency diff.");
            return;
        }

        List<MavenProject> projects = (reactorProjects == null || reactorProjects.isEmpty())
                ? List.of(project)
                : reactorProjects;

        try {
            if (aggregate) {
                processAggregate(projects);
                return;
            }

            processPerModule(projects);
        } catch (IOException exception) {
            throw new MojoExecutionException("Failed to read or write dependency diff state", exception);
        }
    }

    private void applyUserPropertyOverrides() {
        if (session == null) {
            return;
        }

        applyUserPropertyOverrides(session.getUserProperties());
    }

    void applyUserPropertyOverrides(Properties userProperties) {
        aggregate = booleanOverride(userProperties, "dependency-snapshot.aggregate", aggregate);
        failOnChange = booleanOverride(userProperties, "dependency-snapshot.failOnChange", failOnChange);
        skip = booleanOverride(userProperties, "dependency-snapshot.skip", skip);
        createSnapshotIfMissing = booleanOverride(userProperties, "dependency-snapshot.createSnapshotIfMissing", createSnapshotIfMissing);
        writeReportFiles = booleanOverride(userProperties, "dependency-snapshot.writeReportFiles", writeReportFiles);
        writeReportsOnNoChange = booleanOverride(userProperties, "dependency-snapshot.writeReportsOnNoChange", writeReportsOnNoChange);
        writeTextReport = booleanOverride(userProperties, "dependency-snapshot.writeTextReport", writeTextReport);
        writeJsonReport = booleanOverride(userProperties, "dependency-snapshot.writeJsonReport", writeJsonReport);
        snapshotDirectory = fileOverride(userProperties, "dependency-snapshot.snapshotDirectory", snapshotDirectory);
        reportOutputDirectory = fileOverride(userProperties, "dependency-snapshot.reportOutputDirectory", reportOutputDirectory);
    }

    private boolean booleanOverride(Properties userProperties, String propertyName, boolean currentValue) {
        String value = userProperties.getProperty(propertyName);
        return value == null ? currentValue : Boolean.parseBoolean(value);
    }

    private File fileOverride(Properties userProperties, String propertyName, File currentValue) {
        String value = userProperties.getProperty(propertyName);
        return value == null || value.isBlank() ? currentValue : new File(value);
    }

    private void processAggregate(List<MavenProject> projects) throws IOException, MojoFailureException {
        SnapshotDocument current = dependencyCollector.collectAggregate(projects);
        Path snapshotPath = snapshotRepository.aggregateSnapshotPath(snapshotDirectory.toPath());
        boolean changed = processOneSnapshot(snapshotPath, current, "dependency-diff", current.projects().isEmpty() ? "aggregate" : null);

        if (changed && failOnChange) {
            throw new MojoFailureException("Dependency changes detected.");
        }
    }

    private void processPerModule(List<MavenProject> projects) throws IOException, MojoFailureException {
        boolean anyChanged = false;

        for (MavenProject reactorProject : projects) {
            SnapshotDocument current = dependencyCollector.collectModule(reactorProject);
            ProjectSnapshot projectSnapshot = current.projects().get(0);
            Path snapshotPath = snapshotRepository.moduleSnapshotPath(snapshotDirectory.toPath(), projectSnapshot);
            String fileStem = FileNameHelper.moduleFileStem(projectSnapshot) + "-dependency-diff";
            boolean changed = processOneSnapshot(snapshotPath, current, fileStem, projectSnapshot.projectKey());
            anyChanged = anyChanged || changed;
        }

        if (anyChanged && failOnChange) {
            throw new MojoFailureException("Dependency changes detected.");
        }
    }

    private boolean processOneSnapshot(Path snapshotPath, SnapshotDocument current, String reportFileStem, String label)
            throws IOException {
        Optional<SnapshotDocument> previous = snapshotRepository.read(snapshotPath);
        String subject = label == null ? current.mode() : label;

        if (previous.isEmpty()) {
            if (!createSnapshotIfMissing) {
                getLog().warn("No previous dependency snapshot found for " + subject + ", and snapshot creation is disabled.");
                return false;
            }

            getLog().info("No previous dependency snapshot found for " + subject + ". Creating baseline at " + snapshotPath + ".");
            snapshotRepository.write(snapshotPath, current);
            maybeWriteNoChangeReport(current.mode(), reportFileStem);
            return false;
        }

        DiffReportModel report = dependencyDiffService.diff(previous.get(), current);
        String textReport = report.hasChanges()
                ? diffReportFormatter.format(report)
                : "No dependency changes detected.";

        if (report.hasChanges()) {
            getLog().info(System.lineSeparator() + textReport);
            maybeWriteReport(report, reportFileStem, textReport, true);
            if (!failOnChange) {
                snapshotRepository.write(snapshotPath, current);
            }
            return true;
        }

        getLog().info("No dependency changes detected for " + subject + ".");
        maybeWriteReport(report, reportFileStem, textReport, false);
        snapshotRepository.write(snapshotPath, current);
        return false;
    }

    private void maybeWriteNoChangeReport(String mode, String reportFileStem) throws IOException {
        DiffReportModel report = dependencyDiffService.emptyReport(mode);
        maybeWriteReport(report, reportFileStem, "No dependency changes detected.", false);
    }

    private void maybeWriteReport(DiffReportModel report, String reportFileStem, String textReport, boolean hasChanges) throws IOException {
        if (!writeReportFiles) {
            return;
        }

        if (!hasChanges && !writeReportsOnNoChange) {
            return;
        }

        diffReportWriter.writeReports(
                report,
                reportOutputDirectory.toPath(),
                reportFileStem,
                textReport,
                writeTextReport,
                writeJsonReport
        );
    }
}
