// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.test.util;

import java.io.*;

/**
 * {@link java.io.PipedWriter} should be flushed after every write, or otherwise
 * there will be a one-second latency for the reader. This class does that automatically.
 * <a href="http://stackoverflow.com/questions/2843555/better-alternative-for-pipedreader-pipedwriter/2844689#2844689">More information</a>.
 */
public class LowLatencyPipedWriter extends PipedWriter {

    public LowLatencyPipedWriter(PipedReader snk) throws IOException {
        super(snk);
    }

    public LowLatencyPipedWriter() {
    }

    public void write(int c) throws IOException {
        super.write(c);
        flush();
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        super.write(cbuf, off, len);
        flush();
    }
}
