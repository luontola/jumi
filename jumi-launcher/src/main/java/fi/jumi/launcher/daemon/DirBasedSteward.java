// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.daemon;

import org.apache.commons.io.IOUtils;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.*;
import java.nio.file.*;

@NotThreadSafe
public class DirBasedSteward implements Steward {

    private final DaemonJar daemonJar;

    public DirBasedSteward(DaemonJar daemonJar) {
        this.daemonJar = daemonJar;
    }

    @Override
    public Path getDaemonJar(Path jumiHome) {
        Path extractedJar = jumiHome.resolve("lib/" + daemonJar.getDaemonJarName());
        createIfDoesNotExist(extractedJar);
        return extractedJar;
    }

    private void createIfDoesNotExist(Path extractedJar) {
        try (InputStream embeddedJar = daemonJar.getDaemonJarAsStream()) {
            if (sameSize(extractedJar, embeddedJar)) {
                return;
            }
            copyToFile(embeddedJar, extractedJar);
        } catch (IOException e) {
            throw new RuntimeException("failed to copy the embedded daemon JAR to " + extractedJar, e);
        }
    }

    private static boolean sameSize(Path path, InputStream in) throws IOException {
        return Files.exists(path) && Files.size(path) == in.available();
    }

    private static void copyToFile(InputStream source, Path destination) throws IOException {
        Files.createDirectories(destination.getParent());
        try (OutputStream out = Files.newOutputStream(destination)) {
            IOUtils.copy(source, out);
        }
    }
}
