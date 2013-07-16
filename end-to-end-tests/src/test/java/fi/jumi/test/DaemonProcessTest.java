// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.jumi.core.config.SuiteConfiguration;
import fi.jumi.core.network.*;
import fi.jumi.launcher.JumiLauncher;
import org.junit.*;
import org.junit.rules.Timeout;
import sample.*;

import java.io.IOException;
import java.util.*;
import java.util.regex.*;

import static fi.jumi.core.util.AsyncAssert.assertEventually;
import static fi.jumi.test.util.ProcessMatchers.*;
import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DaemonProcessTest {

    @Rule
    public final AppRunner app = new AppRunner();

    @Rule
    public final Timeout timeout = new Timeout(Timeouts.END_TO_END_TEST);

    private JumiLauncher launcher;
    private Process daemonProcess;

    @Test
    public void daemon_process_prints_the_program_name_and_version_number_on_startup() throws Exception {
        startDaemonProcess();

        assertThat(firstLine(app.getCurrentDaemonOutput())).matches("Jumi " + BuildTest.VERSION_PATTERN + " starting up");
    }

    @Test
    public void daemon_process_can_be_closed_by_sending_it_a_shutdown_command() throws Exception {
        // Use a high enough idle timeout to avoid the daemon shutting down automatically
        app.daemon.setIdleTimeout(Timeouts.END_TO_END_TEST * 2);

        startDaemonProcess();
        launcher.shutdownDaemon();

        assertEventually(daemonProcess, is(dead()), Timeouts.ASSERTION);
        assertThat(app.getFinishedDaemonOutput(), containsString("The system will now exit: ordered to shut down"));
    }

    @Test
    public void daemon_process_will_exit_after_a_timeout_after_all_clients_disconnect() throws Exception {
        app.daemon.setIdleTimeout(0);

        startDaemonProcess();
        launcher.close();

        assertEventually(daemonProcess, is(dead()), Timeouts.ASSERTION);
        assertThat(app.getFinishedDaemonOutput(), containsString("The system will now exit: timed out after everybody disconnected"));
    }

    @Test
    public void daemon_process_will_exit_if_it_cannot_connect_to_the_launcher_on_startup() throws Exception {
        app.setMockNetworkServer(new NonFunctionalNetworkServer());
        app.daemon.setStartupTimeout(0);

        startDaemonProcessAsynchronously();

        assertEventually(daemonProcess, is(dead()), Timeouts.ASSERTION);
        assertThat(app.getFinishedDaemonOutput(), containsString("The system will now exit: timed out before anybody connected"));
    }

    @Test
    public void classes_showing_up_in_actor_logs_have_custom_toString_methods() throws Exception {
        List<String> irrelevantClasses = Arrays.asList(
                "sample.BuggyDriver",
                "sample.BuggyDriver$1",
                "fi.jumi.simpleunit.SimpleUnit",
                "fi.jumi.simpleunit.SimpleUnit$RunTestMethod",
                "java.util.concurrent.ThreadPoolExecutor",
                "fi.jumi.core.suite.SuiteRunner" // TODO: remove me, if SuiteRunner starts containing interesting information
        );
        app.daemon.setLogActorMessages(true);

        // sample test classes to produce all possible events
        startDaemonProcess(
                OnePassingTest.class, // the most common events
                OneFailingTest.class, // onFailure
                BuggyDriverTest.class // onInternalError
        );
        launcher.close();

        Set<String> classes = findClassesWithDefaultToString(app.getFinishedDaemonOutput());
        classes.removeAll(irrelevantClasses);
        assertThat("did not expect the following classes to have default toString() method", classes, is(empty()));
    }

    private static Set<String> findClassesWithDefaultToString(String subject) {
        Set<String> classNames = new HashSet<>();
        Matcher m = Pattern.compile("([\\w\\.\\$]+)@[0-9a-f]{6,8}").matcher(subject);
        while (m.find()) {
            classNames.add(m.group(1));
        }
        return classNames;
    }


    // helpers

    private void startDaemonProcess(Class<?>... testClasses) throws Exception {
        app.runTests(testClasses);
        initTestHelpers();
    }

    private void startDaemonProcessAsynchronously() throws Exception {
        app.startSuiteAsynchronously(new SuiteConfiguration());
        initTestHelpers();
    }

    private void initTestHelpers() throws Exception {
        launcher = app.getLauncher();
        daemonProcess = app.getDaemonProcess();
        assertThat(daemonProcess, is(alive()));
    }

    private static String firstLine(String output) {
        return new Scanner(output).nextLine();
    }

    private static class NonFunctionalNetworkServer implements NetworkServer {
        @Override
        public <In, Out> int listenOnAnyPort(NetworkEndpointFactory<In, Out> endpointFactory) {
            return 10; // unassigned port according to http://en.wikipedia.org/wiki/List_of_TCP_and_UDP_port_numbers
        }

        @Override
        public void close() throws IOException {
        }
    }
}
