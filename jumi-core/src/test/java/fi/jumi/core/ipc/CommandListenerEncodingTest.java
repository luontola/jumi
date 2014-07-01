// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.MessageSender;
import fi.jumi.core.*;
import fi.jumi.core.config.*;
import fi.jumi.core.events.CommandListenerEventizer;
import fi.jumi.core.ipc.buffer.IpcBuffer;
import fi.jumi.core.util.SpyListener;
import org.junit.Test;

import java.lang.reflect.*;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CommandListenerEncodingTest {

    @Test
    public void encodes_and_decodes_all_events() throws Exception {
        SpyListener<CommandListener> spy = new SpyListener<>(CommandListener.class);
        exampleUsage(spy.getListener());
        spy.replay();
        IpcBuffer buffer = TestUtil.newIpcBuffer();

        // encode
        IpcProtocol<CommandListener> protocol = newIpcProtocol(buffer);
        protocol.start();
        exampleUsage(sendTo(protocol));
        protocol.close();

        // decode
        buffer.position(0);
        TestUtil.decodeAll(protocol, spy.getListener());

        spy.verify();
    }

    @Test
    public void example_usage_invokes_every_method_in_the_interface() throws Exception {
        CommandListenerSpy spy = new CommandListenerSpy();

        exampleUsage(spy);

        for (Method method : CommandListener.class.getMethods()) {
            assertThat("invoked methods", spy.methodInvocations.keySet(), hasItem(method));
        }
    }

    private static void exampleUsage(CommandListener listener) throws Exception {
        SuiteConfiguration config = new SuiteConfigurationBuilder()
                .addToClasspath(Paths.get("foo/bar.jar"))
                .addJvmOptions("-jvmOption")
                .setWorkingDirectory(Paths.get("workingDir"))
                .setIncludedTestsPattern("glob:Included.class")
                .setExcludedTestsPattern("glob:Excluded.class")
                .freeze();
        assertNoDefaultValues(config);
        listener.runTests(config);
        listener.shutdown();
    }

    private static void assertNoDefaultValues(SuiteConfiguration config) throws Exception {
        for (Field field : SuiteConfiguration.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            Object actualValue = field.get(config);
            Object defaultValue = field.get(SuiteConfiguration.DEFAULTS);
            assertThat("'" + field.getName() + "' field was at default value", actualValue, is(not(defaultValue)));
        }
    }


    // helpers

    private static IpcProtocol<CommandListener> newIpcProtocol(IpcBuffer buffer) {
        return new IpcProtocol<>(buffer, CommandListenerEncoding::new);
    }

    private static CommandListener sendTo(MessageSender<Event<CommandListener>> target) {
        return new CommandListenerEventizer().newFrontend(target);
    }
}
