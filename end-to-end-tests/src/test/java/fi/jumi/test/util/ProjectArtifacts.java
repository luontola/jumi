// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test.util;

import com.google.common.collect.Iterables;

import java.io.IOException;
import java.nio.file.*;
import java.util.NoSuchElementException;

public class ProjectArtifacts {

    private final Path dir;

    public ProjectArtifacts(Path dir) {
        this.dir = dir;
    }

    public Path getProjectJar(String artifactId) throws IOException {
        return getProjectArtifact(artifactId + "-*.jar");
    }

    public Path getProjectPom(String artifactId) throws IOException {
        return getProjectArtifact(artifactId + "-*.pom");
    }

    public Path getProjectArtifact(String glob) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, glob)) {
            try {
                return Iterables.getOnlyElement(stream);
            } catch (NoSuchElementException | IllegalArgumentException e) {
                throw new IllegalArgumentException("could not find the artifact " + glob, e);
            }
        }
    }
}
