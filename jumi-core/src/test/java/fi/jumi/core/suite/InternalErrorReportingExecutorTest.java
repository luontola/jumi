// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.suite;

import fi.jumi.actors.SingleThreadedActors;
import fi.jumi.actors.eventizers.dynamic.DynamicEventizerProvider;
import fi.jumi.actors.listeners.*;
import fi.jumi.core.api.*;
import org.junit.Test;

import java.io.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.*;

public class InternalErrorReportingExecutorTest {

    private final SuiteListener suiteListener = mock(SuiteListener.class);
    private final ByteArrayOutputStream standardOutput = new ByteArrayOutputStream();
    private final SingleThreadedActors backingExecutor = new SingleThreadedActors(new DynamicEventizerProvider(), new CrashEarlyFailureHandler(), new NullMessageListener());

    private final InternalErrorReportingExecutor executor =
            new InternalErrorReportingExecutor(backingExecutor.getExecutor(), suiteListener, new PrintStream(standardOutput));

    @Test
    public void reports_internal_errors() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                throw new RuntimeException("the-exception-message");
            }
        });
        backingExecutor.processEventsUntilIdle();

        String expected = String.format("Uncaught exception in thread %s", Thread.currentThread().getName());
        verify(suiteListener).onInternalError(eq(expected), notNull(StackTrace.class));
    }

    @Test
    public void prints_internal_errors_to_standard_output() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                throw new RuntimeException("the-exception-message");
            }
        });
        backingExecutor.processEventsUntilIdle();

        String output = standardOutput.toString();
        assertThat(output, containsString("Uncaught exception in thread"));
        assertThat(output, containsString("java.lang.RuntimeException: the-exception-message"));
    }

    // TODO: should we have special handling of InterruptedException and ThreadDeath
}
