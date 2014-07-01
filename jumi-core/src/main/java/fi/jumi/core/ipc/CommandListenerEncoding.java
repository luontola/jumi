// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;


import fi.jumi.actors.eventizers.Event;
import fi.jumi.core.CommandListener;
import fi.jumi.core.config.*;
import fi.jumi.core.ipc.buffer.IpcBuffer;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class CommandListenerEncoding extends EncodingUtil implements CommandListener, MessageEncoding<CommandListener> {

    private static final byte runTests = 1;
    private static final byte shutdown = 2;

    // SuiteConfiguration properties
    private static final String classpath = "classpath";
    private static final String jvmOptions = "jvmOptions";
    private static final String workingDirectory = "workingDirectory";
    private static final String includedTestsPattern = "includedTestsPattern";
    private static final String excludedTestsPattern = "excludedTestsPattern";

    public CommandListenerEncoding(IpcBuffer buffer) {
        super(buffer);
    }

    @Override
    public String getInterfaceName() {
        return CommandListener.class.getName();
    }

    @Override
    public int getInterfaceVersion() {
        return 1;
    }

    @Override
    public void encode(Event<CommandListener> message) {
        message.fireOn(this);
    }

    @Override
    public void decode(CommandListener target) {
        byte type = readEventType();
        switch (type) {
            case runTests:
                target.runTests(readSuiteConfiguration());
                break;
            case shutdown:
                target.shutdown();
                break;
            default:
                throw new IllegalArgumentException("Unknown type " + type);
        }
    }

    private SuiteConfiguration readSuiteConfiguration() {
        SuiteConfigurationBuilder config = new SuiteConfigurationBuilder();
        while (true) {
            String name = readNullableString();
            if (name == null) {
                return config.freeze();
            }
            switch (name) {
                case classpath:
                    config.setClasspath(readUriList());
                    break;
                case jvmOptions:
                    config.setJvmOptions(readStringList());
                    break;
                case workingDirectory:
                    config.setWorkingDirectory(readUri());
                    break;
                case includedTestsPattern:
                    config.setIncludedTestsPattern(readString());
                    break;
                case excludedTestsPattern:
                    config.setExcludedTestsPattern(readString());
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected property: " + name);
            }
        }
    }


    // encoding events

    @Override
    public void runTests(SuiteConfiguration config) {
        writeEventType(runTests);

        writeString(classpath);
        writeUriList(config.getClasspath());

        writeString(jvmOptions);
        writeStringList(config.getJvmOptions());

        writeString(workingDirectory);
        writeUri(config.getWorkingDirectory());

        writeString(includedTestsPattern);
        writeString(config.getIncludedTestsPattern());

        writeString(excludedTestsPattern);
        writeString(config.getExcludedTestsPattern());

        writeNullableString(null); // end of this null-terminated list
    }

    @Override
    public void shutdown() {
        writeEventType(shutdown);
    }
}
