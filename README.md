# dependency-snapshot

`dependency-snapshot` is a Maven plugin that snapshots the resolved dependency graph for a build, compares it with the previous build's snapshot, logs a diff, and can write both text and JSON reports to disk.

## Features

- Diffs the resolved dependency set, including transitives
- Persists the previous build baseline under `.mvn/dependency-snapshot/`
- Supports one reactor-wide snapshot or one snapshot per module
- Writes human-readable and machine-readable reports under `target/dependency-snapshot/`
- Can optionally fail the build when dependency changes are detected

## Plugin Coordinates

```xml
<plugin>
  <groupId>io.github.khopland</groupId>
  <artifactId>dependency-snapshot-plugin</artifactId>
  <version>1.0-SNAPSHOT</version>
</plugin>
```

## Recommended Build Binding

```xml
<plugin>
  <groupId>io.github.khopland</groupId>
  <artifactId>dependency-snapshot-plugin</artifactId>
  <version>1.0-SNAPSHOT</version>
  <executions>
    <execution>
      <id>dependency-diff</id>
      <phase>verify</phase>
      <goals>
        <goal>snapshot</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

## Common Configuration

```xml
<plugin>
  <groupId>io.github.khopland</groupId>
  <artifactId>dependency-snapshot-plugin</artifactId>
  <version>1.0-SNAPSHOT</version>
  <executions>
    <execution>
      <id>dependency-diff</id>
      <phase>verify</phase>
      <goals>
        <goal>snapshot</goal>
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
- `snapshotDirectory` default `${session.executionRootDirectory}/.mvn/dependency-snapshot`
- `skip` default `false`
- `createSnapshotIfMissing` default `true`
- `writeReportFiles` default `true`
- `writeReportsOnNoChange` default `false`
- `reportOutputDirectory` default `${session.executionRootDirectory}/target/dependency-snapshot`
- `writeTextReport` default `true`
- `writeJsonReport` default `true`

## Overriding `failOnChange`

If a project configures:

```xml
<failOnChange>true</failOnChange>
```

the plugin now gives Maven user properties explicit precedence, so you can override it at build time with:

```bash
mvn verify -Ddependency-snapshot.failOnChange=false
```

For a profile-based override, the reliable pattern is to make the plugin configuration read from a property:

```xml
<properties>
  <dependency-snapshot.failOnChange>true</dependency-snapshot.failOnChange>
</properties>

<build>
  <plugins>
    <plugin>
      <groupId>io.github.khopland</groupId>
      <artifactId>dependency-snapshot-plugin</artifactId>
      <version>1.0-SNAPSHOT</version>
      <executions>
        <execution>
          <id>dependency-diff</id>
          <phase>verify</phase>
          <goals>
            <goal>snapshot</goal>
          </goals>
          <configuration>
            <failOnChange>${dependency-snapshot.failOnChange}</failOnChange>
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>

<profiles>
  <profile>
    <id>allow-dependency-snapshot-change</id>
    <properties>
      <dependency-snapshot.failOnChange>false</dependency-snapshot.failOnChange>
    </properties>
  </profile>
</profiles>
```

Then run:

```bash
mvn verify -Pallow-dependency-snapshot-change
```

The direct CLI flag has the highest precedence, so it is the simplest way to temporarily disable `failOnChange` for one build.

## Publish To GitHub Packages

This project is configured to publish with Maven `deploy` to:

`https://maven.pkg.github.com/khopland/dependency-snapshot`

### Publish from GitHub Actions

A workflow is included at `.github/workflows/publish.yml` and runs when:

- A GitHub Release is published
- You trigger it manually (`workflow_dispatch`)

It uses the built-in `GITHUB_TOKEN` and publishes with:

```bash
mvn -B -ntp clean deploy
```

## Automatic SemVer Version Bump

A workflow is included at `.github/workflows/bump-version.yml` that bumps Maven versions automatically following SemVer.

### How it works

- Trigger it manually from **Actions** -> **Bump Version**
- Choose bump type: `patch`, `minor`, or `major`
- Choose whether to append `-SNAPSHOT`
- The workflow updates `pom.xml` files, commits, and pushes the change

### SemVer behavior

- `patch`: `x.y.z` -> `x.y.(z+1)`
- `minor`: `x.y.z` -> `x.(y+1).0`
- `major`: `x.y.z` -> `(x+1).0.0`

It accepts versions in the form `x.y.z` or `x.y.z-SNAPSHOT`.

### Publish from local machine

Create or update `~/.m2/settings.xml`:

```xml
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_GITHUB_PERSONAL_ACCESS_TOKEN</password>
    </server>
  </servers>
</settings>
```

Your token needs package write access (`write:packages`).

Then publish with:

```bash
mvn clean deploy
```

## Output

Aggregate mode writes:

- `target/dependency-snapshot/dependency-diff.txt`
- `target/dependency-snapshot/dependency-diff.json`

Per-module mode writes:

- `target/dependency-snapshot/<group>_<artifact>-dependency-diff.txt`
- `target/dependency-snapshot/<group>_<artifact>-dependency-diff.json`

The snapshot baseline is stored outside `target/`, so `mvn clean verify` still compares against the last build.

If a snapshot is missing, the plugin creates a new baseline by default. Set `createSnapshotIfMissing` to `false` if you want missing snapshots to be reported without creating them.
