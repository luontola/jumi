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

import jdave.*;
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

    private DummyEntity obj = new DummyEntity("A");


    public class AnObjectSerializer {
        private Blob serialized;
        private DummyEntity deserialized;

        public void create() {
            ObjectSerializer os = new ObjectSerializerImpl();
            serialized = os.serialize(obj).getSerializedBytes();
            deserialized = (DummyEntity) os.deserialize(serialized).getDeserializedObject();
        }

        public void serializesObjects() {
            specify(serialized, should.not().equal(null));
            specify(serialized, should.not().equal(Blob.EMPTY_BLOB));
        }

        public void deserializesObjects() {
            specify(deserialized, should.not().equal(null));
            specify(deserialized.other, should.equal("A"));
        }
    }

    public class SerializationListeners {
        private ObjectSerializer os;
        private SerializationListener listener;
        private Blob serialized;

        public void create() {
            serialized = new ObjectSerializerImpl().serialize(obj).getSerializedBytes();
            listener = mock(SerializationListener.class);
            os = new ObjectSerializerImpl(new SerializationListener[]{listener}, new SerializationReplacer[0]);
        }

        public void areNotifiedOfAllSerializedObjects() {
            checking(new Expectations() {{
                one(listener).beforeReplace(with(same(obj)), with(same(obj)), with(aNonNull(MetadataBuilder.class)));
                one(listener).beforeReplace(with(same(obj)), with(equal("A")), with(aNonNull(MetadataBuilder.class)));
                one(listener).beforeSerialize(with(same(obj)), with(same(obj)), with(aNonNull(MetadataBuilder.class)));
                one(listener).beforeSerialize(with(same(obj)), with(equal("A")), with(aNonNull(MetadataBuilder.class)));
            }});
            os.serialize(obj).getSerializedBytes();
        }

        public void areNotifiedOfAllDeserializedObjects() {
            checking(new Expectations() {{
                one(listener).afterDeserialize(with(a(DummyEntity.class)), with(aNonNull(MetadataBuilder.class)));
                one(listener).afterDeserialize(with(equal("A")), with(aNonNull(MetadataBuilder.class)));
                one(listener).afterResolve(with(a(DummyEntity.class)), with(aNonNull(MetadataBuilder.class)));
                one(listener).afterResolve(with(equal("A")), with(aNonNull(MetadataBuilder.class)));
            }});
            os.deserialize(serialized).getDeserializedObject();
        }
    }

    public class SerializationReplacers {
        private ObjectSerializer os;
        private SerializationListener listener;
        private Blob serialized;

        public void create() {
            serialized = new ObjectSerializerImpl().serialize(obj).getSerializedBytes();
            listener = mock(SerializationListener.class);
            SerializationReplacer replacer = new SerializationReplacer() {

                public Object replaceSerialized(Object rootObject, Object obj, MetadataBuilder meta) {
                    if (obj.equals("A")) {
                        return "B";
                    }
                    return obj;
                }

                public Object resolveDeserialized(Object obj, MetadataBuilder meta) {
                    if (obj.equals("A")) {
                        return "C";
                    }
                    return obj;
                }
            };
            os = new ObjectSerializerImpl(
                    new SerializationListener[]{listener},
                    new SerializationReplacer[]{replacer});
        }

        public void canReplaceObjectsOnSerialization() {
            checking(new Expectations() {{
                one(listener).beforeReplace(with(same(obj)), with(same(obj)), with(aNonNull(MetadataBuilder.class)));
                one(listener).beforeReplace(with(same(obj)), with(equal("A")), with(aNonNull(MetadataBuilder.class)));
                one(listener).beforeSerialize(with(same(obj)), with(same(obj)), with(aNonNull(MetadataBuilder.class)));
                one(listener).beforeSerialize(with(same(obj)), with(equal("B")), with(aNonNull(MetadataBuilder.class)));
            }});
            Blob bytes = os.serialize(obj).getSerializedBytes();
            DummyEntity serialized = (DummyEntity) new ObjectSerializerImpl().deserialize(bytes).getDeserializedObject();
            specify(obj.other, should.equal("A"));
            specify(serialized.other, should.equal("B"));
        }

        public void canResolveObjectsOnDeserialization() {
            checking(new Expectations() {{
                one(listener).afterDeserialize(with(a(DummyEntity.class)), with(aNonNull(MetadataBuilder.class)));
                one(listener).afterDeserialize(with(equal("A")), with(aNonNull(MetadataBuilder.class)));
                one(listener).afterResolve(with(a(DummyEntity.class)), with(aNonNull(MetadataBuilder.class)));
                one(listener).afterResolve(with(equal("C")), with(aNonNull(MetadataBuilder.class)));
            }});
            DummyEntity deserialized = (DummyEntity) os.deserialize(serialized).getDeserializedObject();
            specify(obj.other, should.equal("A"));
            specify(deserialized.other, should.equal("C"));
        }
    }

    public class MetadataAboutTheSerializationProcess {
        private SerializationResult ser;
        private DeserializationResult deser;

        public void create() {
            SerializationListener listener = new SerializationListener() {
                public void beforeReplace(Object rootObject, Object obj, MetadataBuilder meta) {
                    meta.append(SerializationListener.class, "beforeReplace");
                }

                public void beforeSerialize(Object rootObject, Object obj, MetadataBuilder meta) {
                    meta.append(SerializationListener.class, "beforeSerialize");
                }

                public void afterDeserialize(Object obj, MetadataBuilder meta) {
                    meta.append(SerializationListener.class, "afterDeserialize");
                }

                public void afterResolve(Object obj, MetadataBuilder meta) {
                    meta.append(SerializationListener.class, "afterResolve");
                }
            };
            SerializationReplacer replacer = new SerializationReplacer() {
                public Object replaceSerialized(Object rootObject, Object obj, MetadataBuilder meta) {
                    meta.append(SerializationReplacer.class, "replaceSerialized");
                    return obj;
                }

                public Object resolveDeserialized(Object obj, MetadataBuilder meta) {
                    meta.append(SerializationReplacer.class, "resolveDeserialized");
                    return obj;
                }
            };
            ObjectSerializer os = new ObjectSerializerImpl(
                    new SerializationListener[]{listener},
                    new SerializationReplacer[]{replacer});
            ser = os.serialize("X");
            deser = os.deserialize(ser.getSerializedBytes());
        }

        public void isCollectedByTheListenersDuringSerialization() {
            specify(ser.getMetadata().get(SerializationListener.class),
                    should.containInOrder("beforeReplace", "beforeSerialize"));
        }

        public void isCollectedByTheListenersDuringDeserialization() {
            specify(deser.getMetadata().get(SerializationListener.class),
                    should.containInOrder("afterDeserialize", "afterResolve"));
        }

        public void isCollectedByTheReplacersDuringSerialization() {
            specify(ser.getMetadata().get(SerializationReplacer.class),
                    should.containInOrder("replaceSerialized"));
        }

        public void isCollectedByTheReplacersDuringDeserialization() {
            specify(deser.getMetadata().get(SerializationReplacer.class),
                    should.containInOrder("resolveDeserialized"));
        }
    }
}
