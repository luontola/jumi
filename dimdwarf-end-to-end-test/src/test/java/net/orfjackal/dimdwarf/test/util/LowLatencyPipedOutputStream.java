// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.test.util;

import java.io.*;

/**
 * {@link java.io.PipedOutputStream} should be flushed after every write, or otherwise
 * there will be a one-second latency for the reader. This class does that automatically.
 * <a href="http://stackoverflow.com/questions/2843555/better-alternative-for-pipedreader-pipedwriter/2844689#2844689">More information</a>.
 */
public class LowLatencyPipedOutputStream extends PipedOutputStream {

    public LowLatencyPipedOutputStream(PipedInputStream snk) throws IOException {
        super(snk);
    }

    public LowLatencyPipedOutputStream() {
    }

    public void write(int b) throws IOException {
        super.write(b);
        flush();
    }

    public void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
        flush();
    }
}
