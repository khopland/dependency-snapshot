# dependencysnapshot

`dependencysnapshot` is a Maven plugin that snapshots the resolved dependency graph for a build, compares it with the previous build's snapshot, logs a diff, and can write both text and JSON reports to disk.

## Features

- Diffs the resolved dependency set, including transitives
- Persists the previous build baseline under `.mvn/dependencysnapshot/`
- Supports one reactor-wide snapshot or one snapshot per module
- Writes human-readable and machine-readable reports under `target/dependencysnapshot/`
- Can optionally fail the build when dependency changes are detected

## Plugin Coordinates

```xml
<plugin>
  <groupId>com.github.khopland</groupId>
  <artifactId>dependencysnapshot-mojo</artifactId>
  <version>1.0-SNAPSHOT</version>
</plugin>
```

## Recommended Build Binding

```xml
<plugin>
  <groupId>com.github.khopland</groupId>
  <artifactId>dependencysnapshot-mojo</artifactId>
  <version>1.0-SNAPSHOT</version>
  <executions>
    <execution>
      <id>dependency-diff</id>
      <phase>verify</phase>
      <goals>
        <goal>diff</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

## Common Configuration

```xml
<plugin>
  <groupId>com.github.khopland</groupId>
  <artifactId>dependencysnapshot-mojo</artifactId>
  <version>1.0-SNAPSHOT</version>
  <executions>
    <execution>
      <id>dependency-diff</id>
      <phase>verify</phase>
      <goals>
        <goal>diff</goal>
      </goals>
      <configuration>
        <aggregate>true</aggregate>
        <failOnChange>false</failOnChange>
        <createSnapshotIfMissing>true</createSnapshotIfMissing>
        <writeReportFiles>true</writeReportFiles>
        <writeReportsOnNoChange>false</writeReportsOnNoChange>
      </configuration>
    </execution>
  </executions>
</plugin>
```

## Parameters

- `aggregate` default `true`
- `failOnChange` default `false`
- `snapshotDirectory` default `${session.executionRootDirectory}/.mvn/dependencysnapshot`
- `skip` default `false`
- `createSnapshotIfMissing` default `true`
- `writeReportFiles` default `true`
- `writeReportsOnNoChange` default `false`
- `reportOutputDirectory` default `${session.executionRootDirectory}/target/dependencysnapshot`
- `writeTextReport` default `true`
- `writeJsonReport` default `true`

## Overriding `failOnChange`

If a project configures:

```xml
<failOnChange>true</failOnChange>
```

the plugin now gives Maven user properties explicit precedence, so you can override it at build time with:

```bash
mvn verify -Ddependencysnapshot.failOnChange=false
```

You can also use a profile that changes the plugin configuration:

```xml
<profiles>
  <profile>
    <id>allow-dependency-diff</id>
    <build>
      <plugins>
        <plugin>
          <groupId>com.github.khopland</groupId>
          <artifactId>dependencysnapshot-mojo</artifactId>
          <executions>
            <execution>
              <id>dependency-diff</id>
              <configuration>
                <failOnChange>false</failOnChange>
              </configuration>
            </execution>
          </executions>
        </plugin>
      </plugins>
    </build>
  </profile>
</profiles>
```

Then run:

```bash
mvn verify -Pallow-dependency-diff
```

The direct CLI flag has the highest precedence, so it is the simplest way to temporarily disable `failOnChange` for one build.

## Output

Aggregate mode writes:

- `target/dependencysnapshot/dependency-diff.txt`
- `target/dependencysnapshot/dependency-diff.json`

Per-module mode writes:

- `target/dependencysnapshot/<group>_<artifact>-dependency-diff.txt`
- `target/dependencysnapshot/<group>_<artifact>-dependency-diff.json`

The snapshot baseline is stored outside `target/`, so `mvn clean verify` still compares against the last build.

If a snapshot is missing, the plugin creates a new baseline by default. Set `createSnapshotIfMissing` to `false` if you want missing snapshots to be reported without creating them.
