// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.discovery;

import fi.jumi.actors.ActorRef;
import org.junit.Test;

import java.util.Arrays;

import static org.mockito.Mockito.*;

public class CompositeTestFileFinderTest {

    @Test
    public void invokes_each_of_the_finders() {
        TestFileFinder finder1 = mock(TestFileFinder.class);
        TestFileFinder finder2 = mock(TestFileFinder.class);
        ActorRef<TestFileFinderListener> listenerRef = ActorRef.wrap(mock(TestFileFinderListener.class));

        CompositeTestFileFinder composite = new CompositeTestFileFinder(Arrays.asList(finder1, finder2));
        composite.findTestFiles(listenerRef);

        verify(finder1).findTestFiles(listenerRef);
        verify(finder2).findTestFiles(listenerRef);
    }
}
