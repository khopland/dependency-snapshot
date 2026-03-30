package com.github.khopland.dependencysnapshot;

import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DependencyDiffMojoTest {

    @TempDir
    Path tempDir;

    @Test
    void createsSnapshotWhenMissingByDefault() throws Exception {
        DependencyDiffMojo mojo = configuredMojo(true);

        mojo.execute();

        assertTrue(Files.exists(tempDir.resolve("snapshots").resolve("reactor-deps.json")));
    }

    @Test
    void doesNotCreateSnapshotWhenDisabled() throws Exception {
        DependencyDiffMojo mojo = configuredMojo(false);

        mojo.execute();

        assertFalse(Files.exists(tempDir.resolve("snapshots").resolve("reactor-deps.json")));
    }

    @Test
    void userPropertyOverridesConfiguredFailOnChange() throws Exception {
        DependencyDiffMojo mojo = configuredMojo(true);
        setField(mojo, "failOnChange", true);

        Properties properties = new Properties();
        properties.setProperty("dependencysnapshot.failOnChange", "false");
        mojo.applyUserPropertyOverrides(properties);

        mojo.execute();

        assertTrue(Files.exists(tempDir.resolve("snapshots").resolve("reactor-deps.json")));
    }

    private DependencyDiffMojo configuredMojo(boolean createSnapshotIfMissing) throws Exception {
        DependencyDiffMojo mojo = new DependencyDiffMojo();
        MavenProject project = new MavenProject();
        project.setGroupId("com.example");
        project.setArtifactId("demo");
        project.setVersion("1.0.0");
        project.setArtifacts(Set.of());

        setField(mojo, "project", project);
        setField(mojo, "reactorProjects", List.of(project));
        setField(mojo, "aggregate", true);
        setField(mojo, "failOnChange", false);
        setField(mojo, "skip", false);
        setField(mojo, "createSnapshotIfMissing", createSnapshotIfMissing);
        setField(mojo, "snapshotDirectory", tempDir.resolve("snapshots").toFile());
        setField(mojo, "writeReportFiles", false);
        setField(mojo, "writeReportsOnNoChange", false);
        setField(mojo, "reportOutputDirectory", tempDir.resolve("reports").toFile());
        setField(mojo, "writeTextReport", true);
        setField(mojo, "writeJsonReport", true);
        return mojo;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
