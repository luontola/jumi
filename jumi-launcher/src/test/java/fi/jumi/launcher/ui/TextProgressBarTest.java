// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.ui;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TextProgressBarTest {

    private final TextProgressBar progressBar = new TextProgressBar("[", "=", "]", 10);

    @Test
    public void no_progress() {
        progressBar.setProgress(0.0);

        assertThat(progressBar.toString(), is("["));
    }

    @Test
    public void full_progress() {
        progressBar.setProgress(1.0);
        progressBar.setComplete(true);

        assertThat(progressBar.toString(), is("[==========]"));
    }

    @Test
    public void partial_progress() {
        progressBar.setProgress(0.1);
        assertThat("10%", progressBar.toString(), is("[="));

        progressBar.setProgress(0.5);
        assertThat("50%", progressBar.toString(), is("[====="));

        progressBar.setProgress(0.90);
        assertThat("90%", progressBar.toString(), is("[========="));
    }


    // incremental mode

    @Test
    public void prints_incrementally_when_progress_increases() {
        progressBar.setProgress(0.0);
        assertThat("0% first printing", progressBar.toStringIncremental(), is("["));

        progressBar.setProgress(0.0);
        assertThat("0% second printing", progressBar.toStringIncremental(), is(""));

        progressBar.setProgress(0.1);
        assertThat("10% first printing", progressBar.toStringIncremental(), is("="));
    }

    @Test
    public void incremental_mode_ignores_progress_getting_lower_temporarily() {
        progressBar.setProgress(0.5);
        assertThat("50%", progressBar.toStringIncremental(), is("[====="));

        progressBar.setProgress(0.3);
        assertThat("drop to 30%", progressBar.toStringIncremental(), is(""));

        progressBar.setProgress(0.7);
        assertThat("increase to 70%", progressBar.toStringIncremental(), is("=="));
    }

    @Test
    public void resetting_incremental_mode_starts_printing_from_the_beginning() {
        progressBar.setProgress(0.5);
        assertThat("50% before reset", progressBar.toStringIncremental(), is("[====="));

        progressBar.resetIncremental();

        assertThat("50% after reset", progressBar.toStringIncremental(), is("[====="));
    }


    // indeterminate mode

    @Test
    public void not_visible_in_indeterminate_mode_regardless_of_progress() {
        progressBar.setProgress(0.5);

        progressBar.setIndeterminate(true);

        assertThat(progressBar.toString(), is(""));
    }


    // complete

    @Test
    public void does_not_print_the_end_until_marked_as_complete() {
        progressBar.setProgress(1.0);
        assertThat("100% but not complete", progressBar.toString(), is("[=========="));

        progressBar.setComplete(true);

        assertThat("100% and complete", progressBar.toString(), is("[==========]"));
    }

    @Test
    public void does_not_print_the_end_if_marked_complete_before_100_percent() {
        progressBar.setProgress(0.2);
        progressBar.setComplete(true);

        // this might happen if the JVM crashes or there is some other problem
        assertThat("20% but complete", progressBar.toString(), is("[=="));
        // TODO: instead print "[==        ]"
    }

    // TODO: "|" spacers every 25%
}
