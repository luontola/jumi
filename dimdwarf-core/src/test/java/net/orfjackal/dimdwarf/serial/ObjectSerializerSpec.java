// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.serial;

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.db.Blob;
import net.orfjackal.dimdwarf.entities.DummyEntity;
import org.junit.runner.RunWith;

@RunWith(JDaveRunner.class)
@Group({"fast"})
public class ObjectSerializerSpec extends Specification<Object> {

    private static final NullSerializationFilter NO_FILTER = new NullSerializationFilter();

    private final ObjectSerializer objectSerializer = new ObjectSerializer();
    private final DummyEntity original = new DummyEntity("original");


    public class AnObjectSerializer {
        private Blob serialized;
        private DummyEntity deserialized;

        public void create() {
            serialized = objectSerializer.serialize(original, NO_FILTER);
            deserialized = (DummyEntity) objectSerializer.deserialize(serialized, NO_FILTER);
        }

        public void serializesObjects() {
            specify(serialized, should.not().equal(null));
            specify(serialized, should.not().equal(Blob.EMPTY_BLOB));
        }

        public void deserializesObjects() {
            specify(deserialized, should.not().equal(null));
            specify(deserialized.other, should.equal("original"));
        }
    }

    public class SerializationFilters {
        private SerializationFilter replacingFilter = new SerializationFilter() {
            public Object replaceSerialized(Object rootObject, Object obj) {
                if (obj.equals("original")) {
                    return "replacedOnSerialization";
                }
                return obj;
            }

            public Object resolveDeserialized(Object obj) {
                if (obj.equals("original")) {
                    return "resolvedOnDeserialization";
                }
                return obj;
            }
        };

        public void canReplaceObjectsOnSerialization() {
            Blob bytes = objectSerializer.serialize(original, replacingFilter);
            DummyEntity result = (DummyEntity) objectSerializer.deserialize(bytes, NO_FILTER);

            specify(original.other, should.equal("original"));
            specify(result.other, should.equal("replacedOnSerialization"));
        }

        public void canResolveObjectsOnDeserialization() {
            Blob bytes = objectSerializer.serialize(original, NO_FILTER);
            DummyEntity result = (DummyEntity) objectSerializer.deserialize(bytes, replacingFilter);

            specify(original.other, should.equal("original"));
            specify(result.other, should.equal("resolvedOnDeserialization"));
        }
    }
}
