// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.testutils;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Sandbox {

    private static final int SAFETY_LIMIT = 1000;

    private final File sandboxDir;

    public Sandbox(File sandboxDir) {
        this.sandboxDir = sandboxDir;
    }

    public File createTempDir() {
        int counter = 0;
        File dir;
        do {
            counter++;
            dir = new File(sandboxDir, "temp_" + timestamp() + "_" + counter);
        } while (!dir.mkdir() && counter < SAFETY_LIMIT);

        if (!dir.isDirectory()) {
            throw new IllegalStateException("Unable to create directory: " + dir.getAbsolutePath());
        }
        return dir;
    }

    private static String timestamp() {
        return new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date());
    }

    public void deleteTempDir(File dir) {
        if (!sandboxDir.equals(dir.getParentFile())) {
            throw new IllegalArgumentException("I did not create that file, deleting it would be dangerous: " + dir);
        }
        try {
            FileUtils.forceDelete(dir);
        } catch (IOException e) {
            throw new RuntimeException("Unable to delete directory: " + dir, e);
        }
    }
}
