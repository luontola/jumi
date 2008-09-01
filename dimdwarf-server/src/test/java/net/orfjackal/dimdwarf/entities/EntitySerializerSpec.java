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

package net.orfjackal.dimdwarf.entities;

import jdave.Group;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.db.Blob;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

/**
 * @author Esko Luontola
 * @since 1.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class EntitySerializerSpec extends Specification<Object> {

    private EntitySerializerImpl serializer;
    private SerializationListener listener;
    private DummyEntity entity;

    public void create() throws Exception {
        serializer = new EntitySerializerImpl();
        listener = mock(SerializationListener.class);
        entity = new DummyEntity();
        entity.other = "foo";
    }


    public class AnEntitySerializer {

        public Object create() {
            return null;
        }

        public void serializesAndDeserializesEntities() {
            Blob bytes = serializer.serialize(entity);
            specify(bytes, should.not().equal(null));
            specify(bytes, should.not().equal(Blob.EMPTY_BLOB));

            DummyEntity deserialized = (DummyEntity) serializer.deserialize(bytes);
            specify(deserialized, should.not().equal(null));
            specify(deserialized.other, should.equal("foo"));
        }

        public void notifiesListenersOfAllSerializedObjects() {
            checking(new Expectations() {{
                one(listener).beforeSerialized(entity, entity);
                one(listener).beforeSerialized(entity, "foo");
            }});
            serializer.addSerializationListener(listener);
            serializer.serialize(entity);
        }

        public void notifiesListenersOfAllDeserializedObjects() {
            Blob bytes = serializer.serialize(entity);
            checking(new Expectations() {{
                one(listener).afterDeserialized(with(a(DummyEntity.class)));
                one(listener).afterDeserialized("foo");
            }});
            serializer.addSerializationListener(listener);
            serializer.deserialize(bytes);
        }

        public void allowsReplacingObjectsOnSerialization() {
            serializer.addSerializationReplacer(new SerializationAdapter() {
                public Object replaceSerialized(Object rootObject, Object obj) {
                    if (obj.equals("foo")) {
                        return "bar";
                    }
                    return obj;
                }
            });
            Blob bytes = serializer.serialize(entity);
            DummyEntity deserialized = (DummyEntity) new EntitySerializerImpl().deserialize(bytes);
            specify(entity.other, should.equal("foo"));
            specify(deserialized.other, should.equal("bar"));
        }

        public void allowsReplacingObjectsOnDeserialization() {
            Blob bytes = serializer.serialize(entity);
            serializer.addSerializationReplacer(new SerializationAdapter() {
                public Object resolveDeserialized(Object obj) {
                    if (obj.equals("foo")) {
                        return "bar";
                    }
                    return obj;
                }
            });
            DummyEntity deserialized = (DummyEntity) serializer.deserialize(bytes);
            specify(entity.other, should.equal("foo"));
            specify(deserialized.other, should.equal("bar"));
        }
    }
}
