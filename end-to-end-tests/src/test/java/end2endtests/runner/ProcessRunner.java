// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package end2endtests.runner;

import net.orfjackal.dimdwarf.testutils.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.slf4j.*;

import java.io.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProcessRunner {

    private static final Logger logger = LoggerFactory.getLogger(ProcessRunner.class);

    private final File workingDir;
    private final List<String> command;
    private final OutputStream toOutputWatcher;
    private final StreamWatcher outputWatcher;
    private Process process;

    public ProcessRunner(File workingDir, List<String> command) {
        this.workingDir = workingDir;
        this.command = command;

        try {
            PipedInputStream in = new PipedInputStream();
            toOutputWatcher = new LowLatencyPipedOutputStream(in);
            outputWatcher = new StreamWatcher(new InputStreamReader(in));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public void start() throws IOException {
        logger.debug("Starting process in working directory {}\n\t{}", workingDir, prettyFormatCommand(command));

        process = startProcess(workingDir, command);
        redirectStream(process.getInputStream(), System.out, toOutputWatcher);
        redirectStream(process.getErrorStream(), System.err, toOutputWatcher);
    }

    public void waitForOutput(String expected, int timeout, TimeUnit unit) throws InterruptedException {
        outputWatcher.waitForLineContaining(expected, timeout, unit);
    }

    public boolean isAlive() {
        try {
            process.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }

    public void kill() throws InterruptedException {
        if (process != null) {
            process.destroy();
            process.waitFor();
        }
    }

    private static Process startProcess(File workingDir, List<String> command) throws IOException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(workingDir);
        builder.redirectErrorStream(false);
        builder.command(command);
        return builder.start();
    }

    private static void redirectStream(InputStream input, OutputStream systemOut, OutputStream toWatcher) {
        redirectStream(new TeeInputStream(input, toWatcher), new CloseShieldOutputStream(systemOut));
    }

    private static void redirectStream(final InputStream in, final OutputStream out) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    IOUtils.copy(in, out);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    IOUtils.closeQuietly(out);
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private static String prettyFormatCommand(List<String> command) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < command.size(); i++) {
            if (i > 0) {
                sb.append(' ');
            }
            String arg = command.get(i);
            if (arg.contains(" ") || arg.contains("\\")) {
                arg = '"' + arg + '"';
            }
            sb.append(arg);
        }
        return sb.toString();
    }
}
