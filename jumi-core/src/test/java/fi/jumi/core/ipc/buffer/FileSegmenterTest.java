// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc.buffer;

import org.junit.*;
import org.junit.rules.ExpectedException;

import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FileSegmenterTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void segment_path() {
        FileSegmenter segmenter = new FileSegmenter(Paths.get("base"), 1, 1);

        assertThat("segment 0", segmenter.pathOf(0), is(Paths.get("base")));
        assertThat("segment 1", segmenter.pathOf(1), is(Paths.get("base.001")));
        assertThat("segment 2", segmenter.pathOf(2), is(Paths.get("base.002")));
        assertThat("segment 1000", segmenter.pathOf(1000), is(Paths.get("base.1000")));
    }

    @Test
    public void segment_size() {
        FileSegmenter segmenter = new FileSegmenter(Paths.get("base"), 10, 100);

        assertThat("starts at initial size", segmenter.sizeOf(0), is(10));
        assertThat("doubles for next segment", segmenter.sizeOf(1), is(20));
        assertThat("doubles for next segment", segmenter.sizeOf(2), is(40));
        assertThat("never goes over max size", segmenter.sizeOf(10), is(100));
    }

    @Test
    public void max_size_must_be_equal_or_greater_than_initial_size() {
        new FileSegmenter(Paths.get("base"), 10, 10); // OK

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("max size 9 was less than initial size 10");
        new FileSegmenter(Paths.get("base"), 10, 9);
    }
}
