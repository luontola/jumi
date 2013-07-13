// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.ui;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;

@NotThreadSafe
public class PlainTextPrinter implements Printer {

    private final Appendable out;
    private boolean beginningOfLineOrInsideMeta = true;

    public PlainTextPrinter(Appendable out) {
        this.out = out;
    }

    @Override
    public void printOut(String text) {
        printTo(out, text);
        printedNonMeta(text);
    }

    @Override
    public void printErr(String text) {
        printTo(out, text);
        printedNonMeta(text);
    }

    @Override
    public void printMeta(String text) {
        if (!beginningOfLineOrInsideMeta) {
            printOut("\n");
        }
        printTo(out, text);
    }

    @Override
    public void printlnMeta(String line) {
        printMeta(line);
        printMeta("\n");
    }

    private void printedNonMeta(String text) {
        beginningOfLineOrInsideMeta = text.endsWith("\n"); // matches both "\r\n" and "\n"
    }

    private void printTo(Appendable target, String text) {
        try {
            target.append(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
