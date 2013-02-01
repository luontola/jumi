// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.suite;


import fi.jumi.core.api.*;
import org.junit.Test;

import java.io.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;

public class InternalErrorReportingFailureHandlerTest {

    private final SuiteListener suiteListener = mock(SuiteListener.class);
    private final ByteArrayOutputStream standardOutput = new ByteArrayOutputStream();

    private final InternalErrorReportingFailureHandler failureHandler =
            new InternalErrorReportingFailureHandler(suiteListener, new PrintStream(standardOutput));

    @Test
    public void reports_internal_errors() {
        failureHandler.uncaughtException("the-actor", "the-message", new Throwable("the-exception-message"));

        String expected = String.format("Uncaught exception in thread %s from the-actor when processing the-message", Thread.currentThread().getName());
        verify(suiteListener).onInternalError(eq(expected), notNull(StackTrace.class));
    }

    @Test
    public void prints_internal_errors_to_standard_output() {
        failureHandler.uncaughtException("the-actor", "the-message", new Throwable("the-exception-message"));

        String output = standardOutput.toString();
        assertThat(output, containsString("Uncaught exception in thread"));
        assertThat(output, containsString("java.lang.Throwable: the-exception-message"));
    }
}
