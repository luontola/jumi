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
    private final int middleRepeats;
    private final String completeProgressBar;

    private double progress;
    private boolean indeterminate;
    private int incrementallyPrinted = 0;
    private boolean complete;

    public TextProgressBar(String start, String middle, String end, int middleRepeats) {
        this.start = start;
        this.middle = middle;
        this.end = end;
        this.middleRepeats = middleRepeats;

        StringBuilder sb = new StringBuilder();
        sb.append(start);
        for (int i = 0; i < middleRepeats; i++) {
            sb.append(middle);
        }
        sb.append(end);
        completeProgressBar = sb.toString();
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public void setIndeterminate(boolean indeterminate) {
        this.indeterminate = indeterminate;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public void resetIncremental() {
        incrementallyPrinted = 0;
    }

    @Override
    public String toString() {
        if (indeterminate) {
            return "";
        }

        int len = start.length() + (int) (middle.length() * middleRepeats * progress);
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
