package io.github.khopland.dependency.snapshot.dto;

import java.util.Objects;

public record DependencyCoordinate(
        String groupId,
        String artifactId,
        String version,
        String type,
        String classifier,
        String scope
) implements Comparable<DependencyCoordinate> {

    public DependencyCoordinate {
        groupId = normalize(groupId);
        artifactId = normalize(artifactId);
        version = normalize(version);
        type = normalize(type, "jar");
        classifier = normalize(classifier);
        scope = normalize(scope);
    }

    private static String normalize(String value) {
        return normalize(value, "");
    }

    private static String normalize(String value, String defaultValue) {
        return value == null ? defaultValue : value;
    }

    public String identityKey() {
        return groupId + ":" + artifactId + ":" + type + ":" + classifier;
    }

    public String displayNotation() {
        String classifierPart = classifier.isBlank() ? "" : ":" + classifier;
        String scopePart = scope.isBlank() ? "" : " [" + scope + "]";
        return groupId + ":" + artifactId + ":" + type + classifierPart + ":" + version + scopePart;
    }

    @Override
    public int compareTo(DependencyCoordinate other) {
        return sortKey().compareTo(other.sortKey());
    }

    private String sortKey() {
        return String.join("|", groupId, artifactId, type, classifier, version, scope);
    }

    public boolean sameCoordinates(DependencyCoordinate other) {
        return other != null
                && Objects.equals(groupId, other.groupId)
                && Objects.equals(artifactId, other.artifactId)
                && Objects.equals(type, other.type)
                && Objects.equals(classifier, other.classifier)
                && Objects.equals(version, other.version)
                && Objects.equals(scope, other.scope);
    }
}
