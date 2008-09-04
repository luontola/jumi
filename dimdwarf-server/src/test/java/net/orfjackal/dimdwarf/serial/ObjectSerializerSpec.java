/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
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

package net.orfjackal.dimdwarf.serial;

import jdave.Group;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.db.Blob;
import net.orfjackal.dimdwarf.entities.DummyEntity;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

/**
 * @author Esko Luontola
 * @since 1.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class ObjectSerializerSpec extends Specification<Object> {

    private DummyEntity obj;

    public void create() throws Exception {
        obj = new DummyEntity();
        obj.other = "foo";
    }


    public class AnObjectSerializer {
        private ObjectSerializerImpl serializer;

        public Object create() {
            serializer = new ObjectSerializerImpl();
            return null;
        }

        public void serializesAndDeserializesObjects() {
            Blob bytes = serializer.serialize(obj);
            specify(bytes, should.not().equal(null));
            specify(bytes, should.not().equal(Blob.EMPTY_BLOB));

            DummyEntity deserialized = (DummyEntity) serializer.deserialize(bytes);
            specify(deserialized, should.not().equal(null));
            specify(deserialized.other, should.equal("foo"));
        }
    }

    public class SerializationListeners {
        private Blob serializedBytes;
        private SerializationListener listener;
        private ObjectSerializerImpl serializer;

        public Object create() {
            serializedBytes = new ObjectSerializerImpl().serialize(obj);
            listener = mock(SerializationListener.class);
            serializer = new ObjectSerializerImpl(new SerializationListener[]{listener}, new SerializationReplacer[0]);
            return null;
        }

        public void areNotifiedOfAllSerializedObjects() {
            checking(new Expectations() {{
                one(listener).beforeReplace(obj, obj);
                one(listener).beforeReplace(obj, "foo");
                one(listener).beforeSerialize(obj, obj);
                one(listener).beforeSerialize(obj, "foo");
            }});
            serializer.serialize(obj);
        }

        public void areNotifiedOfAllDeserializedObjects() {
            checking(new Expectations() {{
                one(listener).afterDeserialize(with(a(DummyEntity.class)));
                one(listener).afterDeserialize("foo");
                one(listener).afterResolve(with(a(DummyEntity.class)));
                one(listener).afterResolve("foo");
            }});
            serializer.deserialize(serializedBytes);
        }
    }

    public class SerializationReplacers {
        private Blob serializedBytes;
        private ObjectSerializerImpl serializer;
        private SerializationListener listener;

        public Object create() {
            serializedBytes = new ObjectSerializerImpl().serialize(obj);
            listener = mock(SerializationListener.class);
            SerializationReplacer replacer = new SerializationReplacer() {
                public Object replaceSerialized(Object rootObject, Object obj) {
                    if (obj.equals("foo")) {
                        return "bar";
                    }
                    return obj;
                }

                public Object resolveDeserialized(Object obj) {
                    if (obj.equals("foo")) {
                        return "gazonk";
                    }
                    return obj;
                }
            };
            serializer = new ObjectSerializerImpl(
                    new SerializationListener[]{listener},
                    new SerializationReplacer[]{replacer});
            return null;
        }

        public void canReplaceObjectsOnSerialization() {
            checking(new Expectations() {{
                one(listener).beforeReplace(obj, obj);
                one(listener).beforeReplace(obj, "foo");
                one(listener).beforeSerialize(obj, obj);
                one(listener).beforeSerialize(obj, "bar");
            }});
            Blob bytes = serializer.serialize(obj);
            DummyEntity serialized = (DummyEntity) new ObjectSerializerImpl().deserialize(bytes);
            specify(obj.other, should.equal("foo"));
            specify(serialized.other, should.equal("bar"));
        }

        public void canResolveObjectsOnDeserialization() {
            checking(new Expectations() {{
                one(listener).afterDeserialize(with(a(DummyEntity.class)));
                one(listener).afterDeserialize("foo");
                one(listener).afterResolve(with(a(DummyEntity.class)));
                one(listener).afterResolve("gazonk");
            }});
            DummyEntity deserialized = (DummyEntity) serializer.deserialize(serializedBytes);
            specify(obj.other, should.equal("foo"));
            specify(deserialized.other, should.equal("gazonk"));
        }
    }
}
