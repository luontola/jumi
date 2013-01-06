// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.api;

import javax.annotation.concurrent.Immutable;
import java.io.Serializable;

@Immutable
public class TestFile implements Serializable {

    private final String className; // TODO: remove me

    public static TestFile fromClass(Class<?> clazz) {
        return fromClassName(clazz.getName());
    }

    public static TestFile fromClassName(String className) {
        return new TestFile(className);
    }

    private TestFile(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public String toString() {
        return className;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TestFile)) {
            return false;
        }
        TestFile that = (TestFile) obj;
        return this.className.equals(that.className);
    }

    @Override
    public int hashCode() {
        return className.hashCode();
    }
}
