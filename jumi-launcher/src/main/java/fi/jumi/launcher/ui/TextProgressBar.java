// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.ui;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class TextProgressBar {

    private final String start;
    private final String middle;
    private final String end;
    private final String completeProgressBar;

    private double progress = 0;
    private boolean indeterminate = true;
    private boolean complete = false;
    private int incrementallyPrinted = 0;

    public TextProgressBar(String start, String middle, String end) {
        this.start = start;
        this.middle = middle;
        this.end = end;
        completeProgressBar = start + middle + end;
    }

    public TextProgressBar setProgress(double progress) {
        this.progress = progress;
        return this;
    }

    public TextProgressBar setIndeterminate(boolean indeterminate) {
        this.indeterminate = indeterminate;
        return this;
    }

    public TextProgressBar setComplete(boolean complete) {
        this.complete = complete;
        return this;
    }

    public void resetIncrementalPrinting() {
        incrementallyPrinted = 0;
    }

    @Override
    public String toString() {
        if (indeterminate) {
            return "";
        }

        int len = start.length() + (int) Math.round(middle.length() * progress);
        if (complete && progress == 1) {
            len += end.length();
        }
        return completeProgressBar.substring(0, len);
    }

    public String toStringIncremental() {
        String whole = toString();
        if (incrementallyPrinted > whole.length()) {
            return "";
        }
        String increment = whole.substring(incrementallyPrinted);
        incrementallyPrinted = whole.length();
        return increment;
    }
}
