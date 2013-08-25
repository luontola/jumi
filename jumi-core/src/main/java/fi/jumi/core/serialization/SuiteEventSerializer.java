// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.serialization;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.api.*;
import fi.jumi.core.ipc.IpcBuffer;
import fi.jumi.core.util.MemoryBarrier;

import javax.annotation.concurrent.NotThreadSafe;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;

@NotThreadSafe
public class SuiteEventSerializer implements SuiteListener {

    private static final byte[] HEADER_MAGIC_BYTES = "Jumi".getBytes(StandardCharsets.US_ASCII);
    private static final int PROTOCOL_VERSION = 1;
    private static final String INTERFACE = SuiteListener.class.getName();
    private static final int INTERFACE_VERSION = 1;

    private static final byte STATUS_EMPTY = 0;
    private static final byte STATUS_EXISTS = 1;
    private static final byte STATUS_END_OF_STREAM = 2;

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

    private final MemoryBarrier memoryBarrier = new MemoryBarrier();
    private final IpcBuffer target;

    public SuiteEventSerializer(IpcBuffer target) {
        this.target = target;
    }

    public void start() {
        writeHeader();
    }

    public void end() {
        writeStatusEndOfStream();
    }

    public static void deserialize(IpcBuffer source, SuiteListener target) {
        readHeader(source);

        while (true) {
            // TODO: create proper waiting util
            // TODO: we should do a read barrier here
            int index = source.position();
            byte status = readStatus(source);
            if (status == STATUS_EMPTY) {
                source.position(index);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                continue;
            } else if (status == STATUS_END_OF_STREAM) {
                return;
            } else {
                assert status == STATUS_EXISTS : status;
            }

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
    }

    private void writeHeader() {
        // first byte is zero to signify that the whole header has not yet been written
        target.writeByte((byte) 0);
        for (int i = 1; i < HEADER_MAGIC_BYTES.length; i++) {
            target.writeByte(HEADER_MAGIC_BYTES[i]);
        }

        target.writeInt(PROTOCOL_VERSION);
        writeString(SuiteListener.class.getName());
        target.writeInt(INTERFACE_VERSION);

        // all done
        memoryBarrier.storeStore();
        target.setByte(0, HEADER_MAGIC_BYTES[0]);
    }

    private static void readHeader(IpcBuffer source) {
        waitUntilNonZero(source, 0);

        checkMagicBytes(source);
        checkProtocolVersion(source);
        checkInterface(source);
        checkInterfaceVersion(source);
    }

    private static void waitUntilNonZero(IpcBuffer source, int index) {
        // TODO: create proper waiting util
        // TODO: we should do a read barrier here
        while (source.getByte(index) == 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private static void checkMagicBytes(IpcBuffer source) {
        byte[] actual = new byte[HEADER_MAGIC_BYTES.length];
        for (int i = 0; i < actual.length; i++) {
            actual[i] = source.readByte();
        }
        if (!Arrays.equals(actual, HEADER_MAGIC_BYTES)) {
            throw new IllegalArgumentException("wrong header: expected " + format(HEADER_MAGIC_BYTES) + " but was " + format(actual));
        }
    }

    private static String format(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    private static void checkProtocolVersion(IpcBuffer source) {
        int actual = source.readInt();
        if (actual != PROTOCOL_VERSION) {
            throw new IllegalArgumentException("unsupported protocol version: " + actual);
        }
    }

    private static void checkInterface(IpcBuffer source) {
        String actual = readString(source);
        if (!actual.equals(INTERFACE)) {
            throw new IllegalArgumentException("wrong interface: expected " + INTERFACE + " but was " + actual);
        }
    }

    private static void checkInterfaceVersion(IpcBuffer source) {
        int actual = source.readInt();
        if (actual != INTERFACE_VERSION) {
            throw new IllegalArgumentException("unsupported interface version: " + actual);
        }
    }


    // event write status

    private static byte readStatus(IpcBuffer source) {
        return source.readByte();
    }

    private int writeStatusEmpty() {
        int index = target.position();
        target.writeByte(STATUS_EMPTY);
        return index;
    }

    private void setStatusExists(int index) {
        target.setByte(index, STATUS_EXISTS);
    }

    private void writeStatusEndOfStream() {
        target.writeByte(STATUS_END_OF_STREAM);
    }


    // serializing events

    @Override
    public void onSuiteStarted() {
        int index = writeStatusEmpty();
        writeEventType(onSuiteStarted);
        setStatusExists(index);
    }

    @Override
    public void onInternalError(String message, StackTrace cause) {
        int index = writeStatusEmpty();
        writeEventType(onInternalError);
        writeString(message);
        writeStackTrace(cause);
        setStatusExists(index);
    }

    @Override
    public void onTestFileFound(TestFile testFile) {
        int index = writeStatusEmpty();
        writeEventType(onTestFileFound);
        writeTestFile(testFile);
        setStatusExists(index);
    }

    @Override
    public void onAllTestFilesFound() {
        int index = writeStatusEmpty();
        writeEventType(onAllTestFilesFound);
        setStatusExists(index);
    }

    @Override
    public void onTestFound(TestFile testFile, TestId testId, String name) {
        int index = writeStatusEmpty();
        writeEventType(onTestFound);
        writeTestFile(testFile);
        writeTestId(testId);
        writeString(name);
        setStatusExists(index);
    }

    @Override
    public void onRunStarted(RunId runId, TestFile testFile) {
        int index = writeStatusEmpty();
        writeEventType(onRunStarted);
        writeRunId(runId);
        writeTestFile(testFile);
        setStatusExists(index);
    }

    @Override
    public void onTestStarted(RunId runId, TestId testId) {
        int index = writeStatusEmpty();
        writeEventType(onTestStarted);
        writeRunId(runId);
        writeTestId(testId);
        setStatusExists(index);
    }

    @Override
    public void onPrintedOut(RunId runId, String text) {
        int index = writeStatusEmpty();
        writeEventType(onPrintedOut);
        writeRunId(runId);
        writeString(text);
        setStatusExists(index);
    }

    @Override
    public void onPrintedErr(RunId runId, String text) {
        int index = writeStatusEmpty();
        writeEventType(onPrintedErr);
        writeRunId(runId);
        writeString(text);
        setStatusExists(index);
    }

    @Override
    public void onFailure(RunId runId, StackTrace cause) {
        int index = writeStatusEmpty();
        writeEventType(onFailure);
        writeRunId(runId);
        writeStackTrace(cause);
        setStatusExists(index);
    }

    @Override
    public void onTestFinished(RunId runId) {
        int index = writeStatusEmpty();
        writeEventType(onTestFinished);
        writeRunId(runId);
        setStatusExists(index);
    }

    @Override
    public void onRunFinished(RunId runId) {
        int index = writeStatusEmpty();
        writeEventType(onRunFinished);
        writeRunId(runId);
        setStatusExists(index);
    }

    @Override
    public void onTestFileFinished(TestFile testFile) {
        int index = writeStatusEmpty();
        writeEventType(onTestFileFinished);
        writeTestFile(testFile);
        setStatusExists(index);
    }

    @Override
    public void onSuiteFinished() {
        int index = writeStatusEmpty();
        writeEventType(onSuiteFinished);
        setStatusExists(index);
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
        writeString(testFile.getPath());
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
        writeString(stackTrace.getExceptionClass());
        writeString(stackTrace.toString());
        writeNullableString(stackTrace.getMessage());
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
            writeString(element.getClassName());
            writeString(element.getMethodName());
            writeString(element.getFileName());
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

    // String

    static String readString(IpcBuffer source) {
        String s = readNullableString(source);
        if (s == null) {
            throw new NullPointerException();
        }
        return s;
    }

    static String readNullableString(IpcBuffer source) {
        int length = source.readInt();
        if (length < 0) {
            return null;
        } else {
            char[] chars = new char[length];
            for (int i = 0; i < chars.length; i++) {
                chars[i] = source.readChar();
            }
            return new String(chars);
        }
    }

    void writeString(String s) {
        if (s == null) {
            throw new NullPointerException();
        }
        writeNullableString(s);
    }

    void writeNullableString(String s) {
        if (s == null) {
            target.writeInt(-1);
        } else {
            int length = s.length();
            target.writeInt(length);
            for (int i = 0; i < length; i++) {
                target.writeChar(s.charAt(i));
            }
        }
    }
}
