package com.github.khopland.dependencysnapshot;

final class FileNameHelper {

    private FileNameHelper() {
    }

    static String sanitize(String input) {
        if (input == null || input.isBlank()) {
            return "unknown";
        }

        return input.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    static String moduleFileStem(ProjectSnapshot projectSnapshot) {
        return sanitize(projectSnapshot.groupId()) + "_" + sanitize(projectSnapshot.artifactId());
    }
}
