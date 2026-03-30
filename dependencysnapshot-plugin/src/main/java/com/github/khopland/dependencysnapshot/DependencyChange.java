package com.github.khopland.dependencysnapshot;

public record DependencyChange(
        DependencyCoordinate before,
        DependencyCoordinate after
) implements Comparable<DependencyChange> {

    public String identityKey() {
        return after.identityKey();
    }

    @Override
    public int compareTo(DependencyChange other) {
        return identityKey().compareTo(other.identityKey());
    }
}
