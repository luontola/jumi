// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.junit;

import fi.jumi.api.drivers.SuiteNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.*;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class JUnitTestMechanismFailureReporter extends RunListener {

    private final SuiteNotifier notifier;

    public JUnitTestMechanismFailureReporter(SuiteNotifier notifier) {
        this.notifier = notifier;
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        if (failure.getDescription().equals(Description.TEST_MECHANISM)) {
            notifier.fireInternalError("Failure in JUnit test mechanism", failure.getException());
        }
    }
}
