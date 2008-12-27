/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 *     * Neither the name of the copyright holder nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
