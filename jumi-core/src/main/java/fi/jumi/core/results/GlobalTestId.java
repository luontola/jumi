// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.results;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.api.TestFile;

import javax.annotation.concurrent.Immutable;

@Immutable
class GlobalTestId {

    private final TestFile testFile;
    private final TestId testId;

    public GlobalTestId(TestFile testFile, TestId testId) {
        assert testFile != null;
        assert testId != null;
        this.testFile = testFile;
        this.testId = testId;
    }

    @Override
    public boolean equals(Object other) {
        GlobalTestId that = (GlobalTestId) other;
        return this.testFile.equals(that.testFile) &&
                this.testId.equals(that.testId);
    }

    @Override
    public int hashCode() {
        int result = testFile.hashCode();
        result = 31 * result + testId.hashCode();
        return result;
    }
}
