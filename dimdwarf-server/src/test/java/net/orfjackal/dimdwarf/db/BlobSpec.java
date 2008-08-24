/*
 * Dimdwarf Application Server
 * Copyright (c) 2008, Esko Luontola
 * All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 *     * Neither the name of the copyright holder nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;

/**
 * @author Esko Luontola
 * @since 18.8.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class BlobSpec extends Specification<Blob> {

    private Blob blob;
    private byte[] bytes = {42};


    public class AnEmptyBlob {

        public Object create() {
            blob = Blob.EMPTY_BLOB;
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

        public void hasNoBytesThroughByteArray() {
            specify(blob.getByteArray().length, should.equal(0));
        }
    }

    public class ABlobCreatedFromAByteArray {

        public Object create() {
            blob = Blob.fromBytes(bytes);
            return null;
        }

        public void hasTheSameLengthAsTheSourceByteArray() {
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
            specify(buf.get(), should.equal(bytes[0]));
        }

        public void hasTheSameBytesThroughByteArray() {
            specify(blob.getByteArray(), should.containInOrder(bytes));
        }

        public void canNotBeModifiedThroughTheSourceByteArray() {
            byte before = bytes[0];
            bytes[0]++;
            specify(blob.getInputStream().read(), should.equal(before));
        }

        public void canNotBeModifiedThroughTheResultByteBuffer() {
            final byte before = bytes[0];
            specify(new Block() {
                public void run() throws Throwable {
                    blob.getByteBuffer().put((byte) (before + 1));
                }
            }, should.raise(ReadOnlyBufferException.class));
            specify(blob.getInputStream().read(), should.equal(before));
        }

        public void canNotBeModifiedThroughTheResultByteArray() {
            byte before = bytes[0];
            blob.getByteArray()[0]++;
            specify(blob.getByteArray()[0], should.equal(before));
            specify(blob.getInputStream().read(), should.equal(before));
        }
    }

    public class ABlobCreatedFromAnInputStream {

        public Object create() throws IOException {
            blob = Blob.fromInputStream(new ByteArrayInputStream(bytes));
            return null;
        }

        public void readsTheStreamFully() {
            specify(blob.getByteArray(), should.containInOrder(bytes));
        }
    }

    public class ABlobCreatedFromAByteBuffer {

        private ByteBuffer buffer;

        public Object create() {
            buffer = ByteBuffer.wrap(bytes);
            blob = Blob.fromByteBuffer(buffer);
            return null;
        }

        public void readsTheBufferFully() {
            specify(blob.getByteArray(), should.containInOrder(bytes));
        }

        public void canNotBeModifiedThroughTheSourceBuffer() {
            byte before = bytes[0];
            buffer.put((byte) (before + 1));
            specify(blob.getInputStream().read(), should.equal(before));
        }

        public void theSourceBufferIsNotModified() {
            specify(buffer.position(), should.equal(0));
            specify(buffer.limit(), should.equal(bytes.length));
        }
    }

    public class BlobEqualityAndHashcode {

        private Blob blob1a;
        private Blob blob1b;
        private Blob blob2;
        private Blob blob23;

        public Object create() {
            blob1a = Blob.fromBytes(new byte[]{1});
            blob1b = Blob.fromBytes(new byte[]{1});
            blob2 = Blob.fromBytes(new byte[]{2});
            blob23 = Blob.fromBytes(new byte[]{2, 3});
            return null;
        }

        public void equalsItself() {
            areEqual(blob1a, blob1a);
        }

        public void equalsAnotherBlobWithSameLengthAndContent() {
            areEqual(blob1a, blob1b);
        }

        public void differsFromAnotherBlobWithDifferentContent() {
            areDifferent(blob1a, blob2);
        }

        public void differsFromAnotherBlobWithDifferentLength() {
            areDifferent(blob2, blob23);
        }

        public void differsFromObjectsOfAnotherType() {
            areDifferent(blob1a, "foo");
        }

        public void differsFromNull() {
            specify(blob1a, should.not().equal(null));
        }

        private void areEqual(Object a, Object b) {
            specify(a.equals(b));
            specify(b.equals(a));
            specify(a.hashCode(), should.equal(b.hashCode()));
        }

        private void areDifferent(Object a, Object b) {
            specify(a.equals(b), should.equal(false));
            specify(b.equals(a), should.equal(false));
            specify(a.hashCode(), should.not().equal(b.hashCode()));
        }
    }

    public class BlobComparability {

        private Blob blob1a;
        private Blob blob1b;
        private Blob blob2;
        private Blob blob23;

        public Object create() {
            blob1a = Blob.fromBytes(new byte[]{1});
            blob1b = Blob.fromBytes(new byte[]{1});
            blob2 = Blob.fromBytes(new byte[]{2});
            blob23 = Blob.fromBytes(new byte[]{2, 3});
            return null;
        }

        public void equalsItself() {
            specify(blob1a.compareTo(blob1a) == 0);
        }

        public void equalsAnotherWithSameContent() {
            specify(blob1a.compareTo(blob1b) == 0);
        }

        public void theOneWithSmallerCommonHeadWillBeFirst() {
            specify(blob1a.compareTo(blob2) < 0);
            specify(blob2.compareTo(blob1a) > 0);
        }

        public void whenCommonHeadsAreEqualTheShorterOneIsFirst() {
            specify(blob2.compareTo(blob23) < 0);
            specify(blob23.compareTo(blob2) > 0);
        }
    }
}
