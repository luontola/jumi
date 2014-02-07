// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.util;

import javax.annotation.concurrent.ThreadSafe;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

@ThreadSafe
public class Classpath {

    public static Path[] currentClasspath() {
        String classpath = System.getProperty("java.class.path");
        String pathSeparator = System.getProperty("path.separator");
        List<Path> results = excludeJdkClasses(getClasspathElements(classpath, pathSeparator));
        return results.toArray(new Path[results.size()]);
    }

    public static List<Path> getClasspathElements(String classpath, String pathSeparator) {
        List<Path> results = new ArrayList<>();
        for (String path : classpath.split(Pattern.quote(pathSeparator))) {
            results.add(Paths.get(path));
        }
        return results;
    }

    public static List<Path> excludeJdkClasses(List<Path> classpath) {
        Path javaHome = Paths.get(System.getProperty("java.home"));
        List<Path> results = new ArrayList<>();
        for (Path library : classpath) {
            if (!library.startsWith(javaHome)) {
                results.add(library);
            }
        }
        return results;
    }
}
