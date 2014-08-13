// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.ui;

import fi.jumi.core.util.Boilerplate;

import javax.annotation.concurrent.*;
import java.io.IOException;

import static fi.jumi.launcher.ui.PlainTextPrinter.Mode.*;

@NotThreadSafe
public class PlainTextPrinter implements Printer {

    private final Appendable out;
    private boolean beginningOfLine = true;
    private Mode lastPrinted = Mode.STDOUT;

    public PlainTextPrinter(Appendable out) {
        this.out = out;
    }

    @Override
    public void printOut(String text) {
        beginOnNewLineUnlessAlreadyDoing(STDOUT);
        printTo(out, text);
    }

    @Override
    public void printErr(String text) {
        printOut(text);
    }

    @Override
    public void printMetaIncrement(String text) {
        beginOnNewLineUnlessAlreadyDoing(META_INCREMENT);
        printTo(out, text);
    }

    @Override
    public void printMetaLine(String line) {
        beginOnNewLineUnlessAlreadyDoing(META_LINE);
        printTo(out, line);
        printTo(out, "\n");
    }

    private void beginOnNewLineUnlessAlreadyDoing(Mode mode) {
        if (!beginningOfLine && lastPrinted != mode) {
            printTo(out, "\n");
        }
        lastPrinted = mode;
    }

    private void printTo(Appendable target, String text) {
        beginningOfLine = text.endsWith("\n"); // matches both "\r\n" and "\n"
        try {
            target.append(text);
        } catch (IOException e) {
            throw Boilerplate.rethrow(e);
        }
    }

    @Immutable
    enum Mode {
        STDOUT, META_INCREMENT, META_LINE
    }
}
