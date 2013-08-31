// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import fi.jumi.core.ipc.buffer.*;

import javax.annotation.concurrent.ThreadSafe;
import java.nio.file.Path;

@ThreadSafe
public class IpcChannel {

    private static final int KB = 1024;
    private static final int INITIAL_SEGMENT_SIZE = 4 * KB;
    private static final int MAX_SEGMENT_SIZE = 512 * KB;

    public static <T> IpcWriter<T> writer(Path basePath, IpcProtocol.EncodingFactory<T> encodingFactory) {
        return writer(defaultFileSegmenter(basePath), encodingFactory);
    }

    public static <T> IpcWriter<T> writer(FileSegmenter fileSegmenter, IpcProtocol.EncodingFactory<T> encodingFactory) {
        IpcBuffer buffer = new IpcBuffer(MappedByteBufferSequence.readWrite(fileSegmenter));
        IpcProtocol<T> protocol = new IpcProtocol<>(buffer, encodingFactory);
        protocol.start();
        return protocol;
    }

    public static <T> IpcReader<T> reader(Path basePath, IpcProtocol.EncodingFactory<T> encodingFactory) {
        return reader(defaultFileSegmenter(basePath), encodingFactory);
    }

    public static <T> IpcReader<T> reader(FileSegmenter fileSegmenter, IpcProtocol.EncodingFactory<T> encodingFactory) {
        IpcBuffer buffer = new IpcBuffer(MappedByteBufferSequence.readWrite(fileSegmenter));
        return new IpcProtocol<>(buffer, encodingFactory);
    }

    private static FileSegmenter defaultFileSegmenter(Path basePath) {
        return new FileSegmenter(basePath, INITIAL_SEGMENT_SIZE, MAX_SEGMENT_SIZE);
    }
}
