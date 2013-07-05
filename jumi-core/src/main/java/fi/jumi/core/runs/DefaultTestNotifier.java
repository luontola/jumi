// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runs;

import fi.jumi.api.drivers.*;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
class DefaultTestNotifier implements TestNotifier {

    private final CurrentRun currentRun;
    private final TestId testId;
    private volatile boolean testFinished = false;

    public DefaultTestNotifier(CurrentRun currentRun, TestId testId) {
        this.currentRun = currentRun;
        this.testId = testId;
    }

    @Override
    public void fireFailure(Throwable cause) {
        currentRun.fireFailure(testId, cause);
    }

    @Override
    public void fireTestFinished() {
        if (testFinished) {
            throw new IllegalStateException("cannot call multiple times; " + testId + " is already finished");
        }
        currentRun.fireTestFinished(testId);
        testFinished = true;
        if (currentRun.isRunFinished()) {
            currentRun.fireRunFinished();
        }
    }
}
