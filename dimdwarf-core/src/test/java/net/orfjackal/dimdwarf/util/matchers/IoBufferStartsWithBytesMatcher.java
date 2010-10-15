// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util.matchers;

import org.apache.mina.core.buffer.IoBuffer;
import org.hamcrest.*;

public class IoBufferStartsWithBytesMatcher extends TypeSafeMatcher<IoBuffer> {

    private final IoBuffer expectedFirstBytes;

    public IoBufferStartsWithBytesMatcher(IoBuffer expectedFirstBytes) {
        this.expectedFirstBytes = expectedFirstBytes;
    }

    protected boolean matchesSafely(IoBuffer actual) {
        int expectedLength = expectedFirstBytes.remaining();
        if (actual.remaining() < expectedLength) {
            return false;
        }
        IoBuffer actualFirstBytes = actual.getSlice(expectedLength);
        return expectedFirstBytes.equals(actualFirstBytes);
    }

    public void describeTo(Description description) {
        description
                .appendText("starts with " + expectedFirstBytes.remaining() + " bytes: ")
                .appendText(expectedFirstBytes.getHexDump());
    }
}
