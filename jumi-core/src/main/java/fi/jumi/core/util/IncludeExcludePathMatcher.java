// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import javax.annotation.concurrent.*;
import java.nio.file.*;

@ThreadSafe
public class IncludeExcludePathMatcher implements PathMatcher {

    private final String includedPattern;
    private final String excludedPattern;
    private final PathMatcher included;
    private final PathMatcher excluded;

    public IncludeExcludePathMatcher(FileSystem fileSystem, String includedPattern, String excludedPattern) {
        this.includedPattern = includedPattern;
        this.excludedPattern = excludedPattern;
        included = fileSystem.getPathMatcher(includedPattern);
        excluded = excludedPattern.isEmpty() ? new ExcludeNothing() : fileSystem.getPathMatcher(excludedPattern);
    }

    @Override
    public boolean matches(Path path) {
        return included.matches(path) && !excluded.matches(path);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(include \"" + includedPattern + "\", exclude \"" + excludedPattern + "\")";
    }


    @Immutable
    private static class ExcludeNothing implements PathMatcher {
        @Override
        public boolean matches(Path path) {
            return false;
        }
    }
}
