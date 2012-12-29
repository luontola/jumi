// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import javax.annotation.concurrent.Immutable;
import java.nio.file.Path;

@Immutable
public class ClassFiles {

    public static String classNameToPath(String className) {
        return className.replace('.', '/') + ".class";
    }

    public static String pathToClassName(Path path) {
        if (!path.toString().endsWith(".class")) {
            throw new IllegalArgumentException("Not a class file: " + path);
        }
        StringBuilder sb = new StringBuilder();
        for (Path p : path) {
            sb.append(p.getFileName());
            sb.append(".");
        }
        return sb.substring(0, sb.lastIndexOf(".class."));
    }
}
