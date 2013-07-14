// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.ui;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PlainTextPrinterTest {

    private final StringBuilder output = new StringBuilder();
    private final PlainTextPrinter printer = new PlainTextPrinter(output);

    @Test
    public void prints_stdout() {
        printer.printOut("foo");
        printer.printOut("bar");

        assertThat(output.toString(), is("foobar"));
    }

    @Test
    public void prints_stderr() {
        printer.printErr("foo");
        printer.printErr("bar");

        assertThat(output.toString(), is("foobar"));
    }

    @Test
    public void prints_meta_lines() {
        printer.printMetaLine("foo");
        printer.printMetaLine("bar");

        assertThat(output.toString(), is("foo\nbar\n"));
    }

    @Test
    public void if_previous_stdout_did_not_end_with_newline_then_meta_goes_on_a_new_line() {
        printer.printOut("out");
        printer.printMetaLine("meta");

        assertThat(output.toString(), is("out\nmeta\n"));
    }

    @Test
    public void if_previous_stdout_ended_with_Unix_newline_then_meta_is_printed_right_after_it() {
        printer.printOut("out\n");
        printer.printMetaLine("meta");

        assertThat(output.toString(), is("out\nmeta\n"));
    }

    @Test
    public void if_previous_stdout_ended_with_DOS_newline_then_meta_is_printed_right_after_it() {
        printer.printOut("out\r\n");
        printer.printMetaLine("meta");

        assertThat(output.toString(), is("out\r\nmeta\n"));
    }

    @Test
    public void printing_meta_incrementally() {
        printer.printMetaIncrement("a");
        printer.printMetaIncrement("b");
        printer.printMetaIncrement("c");

        assertThat(output.toString(), is("abc"));
    }

    @Test
    public void printing_meta_incrementally_prints_a_newline_if_previous_stdout_did_not_end_with_newline() {
        printer.printOut("out");
        printer.printMetaIncrement("meta");

        assertThat(output.toString(), is("out\nmeta"));
    }

    @Test
    public void meta_line_starts_a_new_line_after_meta_increment() {
        printer.printMetaIncrement("inc");
        printer.printMetaLine("line");

        assertThat(output.toString(), is("inc\nline\n"));
    }

    @Test
    public void stdout_starts_a_new_line_after_meta_increment() {
        printer.printMetaIncrement("inc");
        printer.printOut("out");

        assertThat(output.toString(), is("inc\nout"));
    }
}
