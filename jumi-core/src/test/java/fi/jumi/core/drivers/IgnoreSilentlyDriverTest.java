// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.drivers;

import fi.jumi.api.drivers.SuiteNotifier;
import org.junit.Test;

import java.util.concurrent.Executor;

import static org.mockito.Mockito.*;

public class IgnoreSilentlyDriverTest {

    private final IgnoreSilentlyDriver driver = new IgnoreSilentlyDriver();

    @Test
    public void does_nothing() {
        SuiteNotifier suiteNotifier = mock(SuiteNotifier.class);
        Executor executor = mock(Executor.class);

        driver.findTests(Object.class, suiteNotifier, executor);

        verifyZeroInteractions(suiteNotifier, executor);
    }
}
