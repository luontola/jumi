// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.ui;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class TextProgressBar {

    private final int startLength;
    private final int middleLength;
    private final String foreground;
    private final String background;

    private double progress = 0;
    private boolean indeterminate = true;
    private boolean complete = false;
    private int incrementallyPrinted = 0;

    public TextProgressBar(String start, String middle, String end) {
        startLength = start.length();
        middleLength = middle.length();
        foreground = start + middle;
        background = start + repeat(' ', middle.length()) + end;
    }

    private static String repeat(char c, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(c);
        }
        return sb.toString();
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

        int len = startLength + (int) Math.round(middleLength * progress);
        String s = foreground.substring(0, len);
        if (complete) {
            s += background.substring(len, background.length());
        }
        return s;
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
