// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import fi.jumi.core.api.StackTrace;
import org.hamcrest.Matchers;

import static org.mockito.Matchers.argThat;

public class JumiMatchers {

    public static StackTrace stackTrace(String expectedToString) {
        return argThat(Matchers.<StackTrace>hasToString(expectedToString));
    }
}
