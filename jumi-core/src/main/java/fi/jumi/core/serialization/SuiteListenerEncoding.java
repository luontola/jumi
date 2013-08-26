// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.serialization;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.api.drivers.TestId;
import fi.jumi.core.api.*;
import fi.jumi.core.ipc.IpcBuffer;

import javax.annotation.concurrent.NotThreadSafe;
import java.nio.file.Paths;
import java.util.*;

import static fi.jumi.core.serialization.StringEncoding.*;

@NotThreadSafe
public class SuiteListenerEncoding implements SuiteListener {

    public static final String INTERFACE_NAME = SuiteListener.class.getName();
    public static final int INTERFACE_VERSION = 1;

    private static final byte onSuiteStarted = 1;
    private static final byte onInternalError = 2;
    private static final byte onTestFileFound = 3;
    private static final byte onAllTestFilesFound = 4;
    private static final byte onTestFound = 5;
    private static final byte onRunStarted = 6;
    private static final byte onTestStarted = 7;
    private static final byte onPrintedOut = 8;
    private static final byte onPrintedErr = 9;
    private static final byte onFailure = 10;
    private static final byte onTestFinished = 11;
    private static final byte onRunFinished = 12;
    private static final byte onTestFileFinished = 13;
    private static final byte onSuiteFinished = 14;

    private final IpcBuffer target;

    public SuiteListenerEncoding(IpcBuffer target) {
        this.target = target;
    }

    public void serialize(Event<SuiteListener> message) {
        message.fireOn(this);
    }

    public static void deserialize(IpcBuffer source, SuiteListener target) {
        byte type = readEventType(source);
        switch (type) {
            case onSuiteStarted:
                target.onSuiteStarted();
                break;
            case onInternalError:
                target.onInternalError(readString(source), readStackTrace(source));
                break;
            case onTestFileFound:
                target.onTestFileFound(readTestFile(source));
                break;
            case onAllTestFilesFound:
                target.onAllTestFilesFound();
                break;
            case onTestFound:
                target.onTestFound(readTestFile(source), readTestId(source), readString(source));
                break;
            case onRunStarted:
                target.onRunStarted(readRunId(source), readTestFile(source));
                break;
            case onTestStarted:
                target.onTestStarted(readRunId(source), readTestId(source));
                break;
            case onPrintedOut:
                target.onPrintedOut(readRunId(source), readString(source));
                break;
            case onPrintedErr:
                target.onPrintedErr(readRunId(source), readString(source));
                break;
            case onFailure:
                target.onFailure(readRunId(source), readStackTrace(source));
                break;
            case onTestFinished:
                target.onTestFinished(readRunId(source));
                break;
            case onRunFinished:
                target.onRunFinished(readRunId(source));
                break;
            case onTestFileFinished:
                target.onTestFileFinished(readTestFile(source));
                break;
            case onSuiteFinished:
                target.onSuiteFinished();
                break;
            default:
                throw new IllegalArgumentException("Unknown type " + type);
        }
    }

    // serializing events

    @Override
    public void onSuiteStarted() {
        writeEventType(onSuiteStarted);
    }

    @Override
    public void onInternalError(String message, StackTrace cause) {
        writeEventType(onInternalError);
        writeString(target, message);
        writeStackTrace(cause);
    }

    @Override
    public void onTestFileFound(TestFile testFile) {
        writeEventType(onTestFileFound);
        writeTestFile(testFile);
    }

    @Override
    public void onAllTestFilesFound() {
        writeEventType(onAllTestFilesFound);
    }

    @Override
    public void onTestFound(TestFile testFile, TestId testId, String name) {
        writeEventType(onTestFound);
        writeTestFile(testFile);
        writeTestId(testId);
        writeString(target, name);
    }

    @Override
    public void onRunStarted(RunId runId, TestFile testFile) {
        writeEventType(onRunStarted);
        writeRunId(runId);
        writeTestFile(testFile);
    }

    @Override
    public void onTestStarted(RunId runId, TestId testId) {
        writeEventType(onTestStarted);
        writeRunId(runId);
        writeTestId(testId);
    }

    @Override
    public void onPrintedOut(RunId runId, String text) {
        writeEventType(onPrintedOut);
        writeRunId(runId);
        writeString(target, text);
    }

    @Override
    public void onPrintedErr(RunId runId, String text) {
        writeEventType(onPrintedErr);
        writeRunId(runId);
        writeString(target, text);
    }

    @Override
    public void onFailure(RunId runId, StackTrace cause) {
        writeEventType(onFailure);
        writeRunId(runId);
        writeStackTrace(cause);
    }

    @Override
    public void onTestFinished(RunId runId) {
        writeEventType(onTestFinished);
        writeRunId(runId);
    }

    @Override
    public void onRunFinished(RunId runId) {
        writeEventType(onRunFinished);
        writeRunId(runId);
    }

    @Override
    public void onTestFileFinished(TestFile testFile) {
        writeEventType(onTestFileFinished);
        writeTestFile(testFile);
    }

    @Override
    public void onSuiteFinished() {
        writeEventType(onSuiteFinished);
    }


    // event type

    private static byte readEventType(IpcBuffer source) {
        return source.readByte();
    }

    private void writeEventType(byte type) {
        target.writeByte(type);
    }

    // TestFile

    private static TestFile readTestFile(IpcBuffer source) {
        return TestFile.fromPath(Paths.get(readString(source)));
    }

    private void writeTestFile(TestFile testFile) {
        writeString(target, testFile.getPath());
    }

    // TestId

    private static TestId readTestId(IpcBuffer source) {
        int[] path = new int[source.readInt()];
        for (int i = 0; i < path.length; i++) {
            path[i] = source.readInt();
        }
        return TestId.of(path);
    }

    private void writeTestId(TestId testId) {
        // TODO: extract this into TestId as "getPath(): int[]"
        List<Integer> path = new ArrayList<>();
        for (TestId id = testId; !id.isRoot(); id = id.getParent()) {
            path.add(id.getIndex());
        }
        Collections.reverse(path);

        target.writeInt(path.size());
        for (Integer index : path) {
            target.writeInt(index);
        }
    }

    // RunId

    private static RunId readRunId(IpcBuffer source) {
        return new RunId(source.readInt());
    }

    private void writeRunId(RunId runId) {
        target.writeInt(runId.toInt());
    }

    // StackTrace

    static StackTrace readStackTrace(IpcBuffer source) {
        return new StackTrace.Builder()
                .setExceptionClass(readString(source))
                .setToString(readString(source))
                .setMessage(readNullableString(source))
                .setStackTrace(readStackTraceElements(source))
                .setCause(readOptionalException(source))
                .setSuppressed(readManyExceptions(source))
                .build();
    }

    void writeStackTrace(StackTrace stackTrace) {
        writeString(target, stackTrace.getExceptionClass());
        writeString(target, stackTrace.toString());
        writeNullableString(target, stackTrace.getMessage());
        writeStackTraceElements(stackTrace.getStackTrace());
        writeOptionalException(stackTrace.getCause());
        writeManyExceptions(stackTrace.getSuppressed());
    }

    private static StackTraceElement[] readStackTraceElements(IpcBuffer source) {
        StackTraceElement[] elements = new StackTraceElement[source.readInt()];
        for (int i = 0; i < elements.length; i++) {
            elements[i] = new StackTraceElement(readString(source), readString(source), readString(source), source.readInt());
        }
        return elements;
    }

    private void writeStackTraceElements(StackTraceElement[] elements) {
        target.writeInt(elements.length);
        for (StackTraceElement element : elements) {
            writeString(target, element.getClassName());
            writeString(target, element.getMethodName());
            writeString(target, element.getFileName());
            target.writeInt(element.getLineNumber());
        }
    }

    private static Throwable readOptionalException(IpcBuffer source) {
        Throwable[] exceptions = readManyExceptions(source);
        return exceptions.length == 0 ? null : exceptions[0];
    }

    private void writeOptionalException(Throwable exception) {
        writeManyExceptions(exception == null ? new Throwable[0] : new Throwable[]{exception});
    }

    private static Throwable[] readManyExceptions(IpcBuffer source) {
        Throwable[] exceptions = new Throwable[source.readInt()];
        for (int i = 0; i < exceptions.length; i++) {
            exceptions[i] = readStackTrace(source);
        }
        return exceptions;
    }

    private void writeManyExceptions(Throwable[] exceptions) {
        target.writeInt(exceptions.length);
        for (Throwable exception : exceptions) {
            writeStackTrace((StackTrace) exception);
        }
    }
}
