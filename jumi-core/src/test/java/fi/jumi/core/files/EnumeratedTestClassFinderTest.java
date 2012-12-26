// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.files;

import fi.jumi.actors.ActorRef;
import fi.jumi.core.files.dummies.*;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.*;

public class EnumeratedTestClassFinderTest {

    private final TestClassFinderListener listener = mock(TestClassFinderListener.class);
    private final List<String> failures = new ArrayList<>();

    @Test
    public void finds_a_single_test_class() {
        findTests(DummyTest.class.getName());

        verify(listener).onTestClassFound(DummyTest.class);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void finds_multiple_test_classes() {
        findTests(DummyTest.class.getName(), AnotherDummyTest.class.getName());

        verify(listener).onTestClassFound(DummyTest.class);
        verify(listener).onTestClassFound(AnotherDummyTest.class);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void fails_gracefully_if_a_class_is_not_found() {
        findTests(DummyTest.class.getName(), "NoSuchTest", AnotherDummyTest.class.getName());

        assertThat(failures, contains((Object) "NoSuchTest"));
        verify(listener).onTestClassFound(DummyTest.class);
        verify(listener).onTestClassFound(AnotherDummyTest.class);
        verifyNoMoreInteractions(listener);
    }


    private void findTests(String... testClassNames) {
        EnumeratedTestClassFinder finder = new EnumeratedTestClassFinder(Arrays.asList(testClassNames), getClass().getClassLoader()) {
            @Override
            protected void logTestClassNotFound(String testClassName, ClassNotFoundException e) {
                failures.add(testClassName);
            }
        };
        finder.findTestClasses(ActorRef.wrap(listener));
    }
}
