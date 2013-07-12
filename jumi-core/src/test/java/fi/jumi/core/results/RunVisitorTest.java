// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.results;

import fi.jumi.core.api.*;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RunVisitorTest {

    @Test
    public void contains_all_run_specific_events_from_SuiteListener() {
        assertThat("RunVisitor should include all run-specific events from SuiteListener",
                getMethods(RunVisitor.class), is(getMethodsWithParameter(SuiteListener.class, RunId.class)));
    }

    private static SortedSet<String> getMethods(Class<?> type) {
        SortedSet<String> names = new TreeSet<>();
        for (Method method : type.getMethods()) {
            names.add(method.getName());
        }
        return names;
    }

    private static SortedSet<String> getMethodsWithParameter(Class<?> type, Class<?> parameterType) {
        SortedSet<String> names = new TreeSet<>();
        for (Method method : type.getMethods()) {
            if (Arrays.asList(method.getParameterTypes()).contains(parameterType)) {
                names.add(method.getName());
            }
        }
        return names;
    }
}
