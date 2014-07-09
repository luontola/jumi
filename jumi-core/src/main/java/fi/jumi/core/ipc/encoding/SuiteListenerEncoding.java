// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc.encoding;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.api.drivers.TestId;
import fi.jumi.core.api.*;
import fi.jumi.core.ipc.buffer.IpcBuffer;

import javax.annotation.concurrent.NotThreadSafe;
import java.nio.file.Paths;

@NotThreadSafe
public class SuiteListenerEncoding extends EncodingUtil implements SuiteListener, MessageEncoding<SuiteListener> {

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

    public SuiteListenerEncoding(IpcBuffer buffer) {
        super(buffer);
    }

    @Override
    public String getInterfaceName() {
        return SuiteListener.class.getName();
    }

    @Override
    public int getInterfaceVersion() {
        return 1;
    }

    @Override
    public void encode(Event<SuiteListener> message) {
        message.fireOn(this);
    }

    @Override
    public void decode(SuiteListener target) {
        byte type = readEventType();
        switch (type) {
            case onSuiteStarted:
                target.onSuiteStarted();
                break;
            case onInternalError:
                target.onInternalError(readString(), readStackTrace());
                break;
            case onTestFileFound:
                target.onTestFileFound(readTestFile());
                break;
            case onAllTestFilesFound:
                target.onAllTestFilesFound();
                break;
            case onTestFound:
                target.onTestFound(readTestFile(), readTestId(), readString());
                break;
            case onRunStarted:
                target.onRunStarted(readRunId(), readTestFile());
                break;
            case onTestStarted:
                target.onTestStarted(readRunId(), readTestId());
                break;
            case onPrintedOut:
                target.onPrintedOut(readRunId(), readString());
                break;
            case onPrintedErr:
                target.onPrintedErr(readRunId(), readString());
                break;
            case onFailure:
                target.onFailure(readRunId(), readStackTrace());
                break;
            case onTestFinished:
                target.onTestFinished(readRunId());
                break;
            case onRunFinished:
                target.onRunFinished(readRunId());
                break;
            case onTestFileFinished:
                target.onTestFileFinished(readTestFile());
                break;
            case onSuiteFinished:
                target.onSuiteFinished();
                break;
            default:
                throw new IllegalArgumentException("Unknown type " + type);
        }
    }

    // encoding events

    @Override
    public void onSuiteStarted() {
        writeEventType(onSuiteStarted);
    }

    @Override
    public void onInternalError(String message, StackTrace cause) {
        writeEventType(onInternalError);
        writeString(message);
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
        writeString(name);
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
        writeString(text);
    }

    @Override
    public void onPrintedErr(RunId runId, String text) {
        writeEventType(onPrintedErr);
        writeRunId(runId);
        writeString(text);
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


    // TestFile

    private void writeTestFile(TestFile testFile) {
        writeString(testFile.getPath());
    }

    private TestFile readTestFile() {
        return TestFile.fromPath(Paths.get(readString()));
    }

    // TestId

    private void writeTestId(TestId testId) {
        writeIntArray(testId.getPath());
    }

    private TestId readTestId() {
        return TestId.of(readIntArray());
    }

    // RunId

    private void writeRunId(RunId runId) {
        buffer.writeInt(runId.toInt());
    }

    private RunId readRunId() {
        return new RunId(buffer.readInt());
    }

    // StackTrace

    void writeStackTrace(StackTrace stackTrace) {
        writeString(stackTrace.getExceptionClass());
        writeString(stackTrace.toString());
        writeNullableString(stackTrace.getMessage());
        writeStackTraceElements(stackTrace.getStackTrace());
        writeOptionalException(stackTrace.getCause());
        writeExceptions(stackTrace.getSuppressed());
    }

    StackTrace readStackTrace() {
        return new StackTrace.Builder()
                .setExceptionClass(readString())
                .setToString(readString())
                .setMessage(readNullableString())
                .setStackTrace(readStackTraceElements())
                .setCause(readOptionalException())
                .setSuppressed(readExceptions())
                .build();
    }

    private void writeStackTraceElements(StackTraceElement[] elements) {
        writeArray(elements, this::writeStackTraceElement);
    }

    private StackTraceElement[] readStackTraceElements() {
        return readArray(this::readStackTraceElement, StackTraceElement[]::new);
    }

    private void writeStackTraceElement(StackTraceElement element) {
        writeString(element.getClassName());
        writeString(element.getMethodName());
        writeString(element.getFileName());
        buffer.writeInt(element.getLineNumber());
    }

    private StackTraceElement readStackTraceElement() {
        String className = readString();
        String methodName = readString();
        String fileName = readString();
        int lineNumber = buffer.readInt();
        return new StackTraceElement(className, methodName, fileName, lineNumber);
    }

    // Throwable (assumed to be StackTrace at runtime)

    private void writeOptionalException(Throwable exception) {
        writeExceptions(exception == null ? new Throwable[0] : new Throwable[]{exception});
    }

    private Throwable readOptionalException() {
        Throwable[] exceptions = readExceptions();
        return exceptions.length == 0 ? null : exceptions[0];
    }

    private void writeExceptions(Throwable[] exceptions) {
        writeArray(exceptions, this::writeException);
    }

    private Throwable[] readExceptions() {
        return readArray(this::readException, Throwable[]::new);
    }

    private void writeException(Throwable exception) {
        writeStackTrace((StackTrace) exception);
    }

    private Throwable readException() {
        return readStackTrace();
    }
}
