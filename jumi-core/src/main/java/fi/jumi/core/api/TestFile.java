// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.api;

import fi.jumi.core.util.ClassFiles;

import javax.annotation.concurrent.Immutable;
import java.io.Serializable;
import java.nio.file.Path;

@Immutable
public class TestFile implements Serializable {

    private final String path;

    public static TestFile fromPath(Path path) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.getNameCount(); i++) {
            sb.append(path.getName(i));
            sb.append('/');
        }
        String filePath = sb.substring(0, sb.length() - 1);
        return new TestFile(filePath);
    }

    public static TestFile fromClass(Class<?> clazz) {
        return fromClassName(clazz.getName());
    }

    public static TestFile fromClassName(String className) {
        return new TestFile(ClassFiles.classNameToPath(className));
    }

    private TestFile(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public boolean isClass() {
        return path.endsWith(".class");
    }

    public String getClassName() {
        if (!isClass()) {
            throw new IllegalStateException("not a class file: " + path);
        }
        return ClassFiles.pathToClassName(path);
    }

    @Override
    public String toString() {
        if (isClass()) {
            return getClassName();
        }
        return getPath();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TestFile)) {
            return false;
        }
        TestFile that = (TestFile) obj;
        return this.path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}
