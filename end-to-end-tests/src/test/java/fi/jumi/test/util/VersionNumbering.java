// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test.util;

import org.hamcrest.*;

public class VersionNumbering extends TypeSafeMatcher<String> {

    private final String releasePattern;
    private final String snapshotPattern;

    public VersionNumbering() {
        this("\\d+\\.\\d+\\.\\d+", "\\d+\\.\\d+-SNAPSHOT");
    }

    public VersionNumbering(String releasePattern, String snapshotPattern) {
        this.releasePattern = releasePattern;
        this.snapshotPattern = snapshotPattern;
    }

    public String getPattern() {
        return "(" + releasePattern + "|" + snapshotPattern + ")";
    }

    public boolean isSnapshot(String version) {
        return version.matches(snapshotPattern);
    }

    public boolean isRelease(String version) {
        return version.matches(releasePattern);
    }

    @Override
    protected boolean matchesSafely(String item) {
        // Must be either release or snapshot. It's an error for both patterns to match.
        return isRelease(item) != isSnapshot(item);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("version number of format ").appendValue(releasePattern)
                .appendText(" or ").appendValue(snapshotPattern);
    }
}
