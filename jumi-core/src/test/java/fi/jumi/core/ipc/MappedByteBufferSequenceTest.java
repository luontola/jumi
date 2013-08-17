// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;

public class MappedByteBufferSequenceTest extends ByteBufferSequenceContract {

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    @Override
    protected ByteBufferSequence newByteBufferSequence() {
        Path path = tempDir.getRoot().toPath().resolve("buffer");
        return new MappedByteBufferSequence(path, 10);
    }
}
