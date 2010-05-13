// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db;

import jdave.*;
import jdave.junit4.JDaveRunner;
import org.junit.runner.RunWith;

import java.io.*;
import java.nio.*;

@RunWith(JDaveRunner.class)
@Group({"fast"})
public class BlobSpec extends Specification<Blob> {

    private Blob blob;
    private byte[] bytes = {42};


    public class AnEmptyBlob {

        public void create() {
            blob = Blob.EMPTY_BLOB;
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

        public void create() {
            blob = Blob.fromBytes(bytes);
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

        public void create() throws IOException {
            blob = Blob.fromInputStream(new ByteArrayInputStream(bytes));
        }

        public void readsTheStreamFully() {
            specify(blob.getByteArray(), should.containInOrder(bytes));
        }
    }

    public class ABlobCreatedFromAByteBuffer {

        private ByteBuffer buffer;

        public void create() {
            buffer = ByteBuffer.wrap(bytes);
            blob = Blob.fromByteBuffer(buffer);
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

        public void create() {
            blob1a = Blob.fromBytes(new byte[]{1});
            blob1b = Blob.fromBytes(new byte[]{1});
            blob2 = Blob.fromBytes(new byte[]{2});
            blob23 = Blob.fromBytes(new byte[]{2, 3});
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

        public void create() {
            blob1a = Blob.fromBytes(new byte[]{1});
            blob1b = Blob.fromBytes(new byte[]{1});
            blob2 = Blob.fromBytes(new byte[]{2});
            blob23 = Blob.fromBytes(new byte[]{2, 3});
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
