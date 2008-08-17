/*
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.orfjackal.dimdwarf.db;

import jdave.Block;
import jdave.Group;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;

/**
 * @author Esko Luontola
 * @since 18.8.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class BlobSpec extends Specification<Object> {

    private Blob blob;
    private byte[] bytes = {42};


    public class AnEmptyBlob {

        public Object create() {
            blob = new Blob();
            return null;
        }

        public void hasZeroLength() {
            specify(blob.length(), should.equal(0));
        }

        public void hasNoBytesThroughInputStream() {
            ByteArrayInputStream in = blob.getInputStream();
            specify(in.available(), should.equal(0));
            specify(in.read(), should.equal(-1));
        }

        public void hasNoBytesThroughByteBuffer() {
            specify(blob.getByteBuffer().capacity(), should.equal(0));
        }
    }

    public class ABlobCreatedFromAByteArray {

        public Object create() {
            blob = Blob.fromBytes(bytes);
            return null;
        }

        public void hasTheSameLengthAsTheArray() {
            specify(blob.length(), should.equal(bytes.length));
        }

        public void hasTheSameBytesThroughInputStream() {
            ByteArrayInputStream in = blob.getInputStream();
            specify(in.available(), should.equal(bytes.length));
            specify(in.read(), should.equal(bytes[0]));
            specify(in.read(), should.equal(-1));
        }

        public void hasTheSameBytesThroughByteBuffer() {
            ByteBuffer buf = blob.getByteBuffer();
            specify(buf.capacity(), should.equal(bytes.length));
        }

        public void canNotBeModifiedThroughTheSourceByteArray() {
            byte before = bytes[0];
            bytes[0]++;
            specify(blob.getInputStream().read(), should.equal(before));
        }

        public void canNotBeModifiedThroughtTheByteBuffer() {
            final byte before = bytes[0];
            specify(new Block() {
                public void run() throws Throwable {
                    blob.getByteBuffer().put((byte) (before + 1));
                }
            }, should.raise(ReadOnlyBufferException.class));
            specify(blob.getInputStream().read(), should.equal(before));
        }
    }
}
