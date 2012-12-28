// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.files;

import fi.jumi.actors.ActorRef;
import org.junit.Test;

import java.util.Arrays;

import static org.mockito.Mockito.*;

public class CompositeTestClassFinderTest {

    @Test
    public void invokes_each_of_the_finders() {
        TestClassFinder finder1 = mock(TestClassFinder.class);
        TestClassFinder finder2 = mock(TestClassFinder.class);
        ActorRef<TestClassFinderListener> listenerRef = ActorRef.wrap(mock(TestClassFinderListener.class));

        CompositeTestClassFinder composite = new CompositeTestClassFinder(Arrays.asList(finder1, finder2));
        composite.findTestClasses(listenerRef);

        verify(finder1).findTestClasses(listenerRef);
        verify(finder2).findTestClasses(listenerRef);
    }
}
