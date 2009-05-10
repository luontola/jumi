// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.aop;

import java.util.regex.Pattern;

/**
 * Matches class names with a pattern syntax similar to <a href="http://ant.apache.org/manual/dirtasks.html">Ant</a>.
 * <pre>
 * foo.Bar - Single class foo.bar
 * foo.*   - All classes in package foo
 * foo.**  - All classes in package foo and its subpackages
 * </pre>
 *
 * @author Esko Luontola
 * @since 27.12.2008
 */
public class ClassNameMatcher {

    private static final String PACKAGE_REGEX = "[^\\.]*";
    private static final String SUBPACKAGE_REGEX = ".*";

    private final Pattern pattern;

    public ClassNameMatcher(String pattern) {
        this.pattern = Pattern.compile(toRegex(pattern));
    }

    private static String toRegex(String pattern) {
        String regex = "";
        for (int i = 0; i < pattern.length(); i++) {
            if (subpackagePatternAt(i, pattern)) {
                regex += SUBPACKAGE_REGEX;
            } else if (packagePatternAt(i, pattern)) {
                regex += PACKAGE_REGEX;
            } else {
                regex += quoteCharAt(i, pattern);
            }
        }
        return regex;
    }

    private static boolean subpackagePatternAt(int i, String pattern) {
        return packagePatternAt(i, pattern)
                && packagePatternAt(i + 1, pattern);
    }

    private static boolean packagePatternAt(int i, String pattern) {
        return i < pattern.length()
                && pattern.charAt(i) == '*';
    }

    private static String quoteCharAt(int i, String pattern) {
        return Pattern.quote("" + pattern.charAt(i));
    }

    public boolean matches(String className) {
        return pattern.matcher(className).matches();
    }
}
