// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.api.internal.*;
import net.orfjackal.dimdwarf.db.*;
import net.orfjackal.dimdwarf.entities.dao.*;
import net.orfjackal.dimdwarf.serial.*;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import static net.orfjackal.dimdwarf.util.Objects.uncheckedCast;

@RunWith(JDaveRunner.class)
@Group({"fast"})
public class EntitySerializationChecksSpec extends Specification<Object> {

    private static final EntityId ENTITY_ID = new EntityObjectId(42);

    private DatabaseTable<Blob, Blob> db;
    private EntityRepository repository;
    private DummyEntity entity;
    private DelegatingSerializationReplacer replacer;

    public void create() throws Exception {
        // TODO: would it be better for this class to use Guice, to make sure that the EntityModule has all listeners configured?
        db = uncheckedCast(mock(DatabaseTable.class));
        SerializationListener[] listeners = {
                new CheckDirectlyReferredEntitySerialized(new DimdwarfEntityApi()),
                new CheckInnerClassSerialized()
        };
        replacer = new DelegatingSerializationReplacer();
        ObjectSerializer serializer = new ObjectSerializer(listeners, new SerializationReplacer[]{replacer});

        repository =
                new EntityRepository(
                        new EntityDao(
                                db,
                                new ConvertEntityIdToBytes(),
                                new NoConversion<Blob>()),
                        serializer);
        entity = new DummyEntity();
    }

    private Expectations entityIsUpdated(final EntityId entityId) {
        return new Expectations() {{
            one(db).update(with(equal(asBytes(entityId))), with(aNonNull(Blob.class)));
            allowing(db).read(asBytes(entityId)); will(returnValue(Blob.EMPTY_BLOB));
        }};
    }

    private static Blob asBytes(EntityId id) {
        return new ConvertEntityIdToBytes().forth(id);
    }


    public class ReferringEntities {

        public void referringAnEntityDirectlyIsForbidden() {
            entity.other = new DummyEntity();
            specify(new Block() {
                public void run() throws Throwable {
                    repository.update(ENTITY_ID, entity);
                }
            }, should.raise(IllegalArgumentException.class));
        }

        public void referringAnEntityThroughAnEntityReferenceIsAllowed() {
            checking(entityIsUpdated(ENTITY_ID));
            entity.other = new EntityReferenceImpl<DummyEntity>(new EntityObjectId(123), new DummyEntity());
            repository.update(ENTITY_ID, entity);
        }

        public void checksAreDoneAfterAnyObjectsHaveBeenReplaced() {
            entity.other = "tmp";
            replacer.delegate = new SerializationReplacerAdapter() {
                @Override
                public Object replaceSerialized(Object rootObject, Object obj, MetadataBuilder meta) {
                    if (obj.equals("tmp")) {
                        return new DummyEntity();
                    }
                    return obj;
                }
            };
            specify(new Block() {
                public void run() throws Throwable {
                    repository.update(ENTITY_ID, entity);
                }
            }, should.raise(IllegalArgumentException.class));
        }
    }

    /**
     * <a href="http://java.sun.com/javase/6/docs/platform/serialization/spec/serial-arch.html#4539">Java Object
     * Serialization Specification</a> says that serialization of inner classes (i.e., nested classes that are not
     * static member classes), including local and anonymous classes, is strongly discouraged.
     */
    public class ReferringInnerClasses {

        public void serializingAnonymousClassesIsForbidden() {
            entity.other = newAnonymousClassInstance();
            specify(new Block() {
                public void run() throws Throwable {
                    repository.update(ENTITY_ID, entity);
                }
            }, should.raise(IllegalArgumentException.class));
        }

        public void serializingLocalClassesIsForbidden() {
            entity.other = newLocalClassInstance();
            specify(new Block() {
                public void run() throws Throwable {
                    repository.update(ENTITY_ID, entity);
                }
            }, should.raise(IllegalArgumentException.class));
        }

        public void serializingStaticMemberClassesIsAllowed() {
            checking(entityIsUpdated(ENTITY_ID));
            entity.other = new StaticMemberClass();
            repository.update(ENTITY_ID, entity);
        }

        public void checksAreDoneAfterAnyObjectsHaveBeenReplaced() {
            entity.other = "tmp";
            replacer.delegate = new SerializationReplacerAdapter() {
                @Override
                public Object replaceSerialized(Object rootObject, Object obj, MetadataBuilder meta) {
                    if (obj.equals("tmp")) {
                        return newAnonymousClassInstance();
                    }
                    return obj;
                }
            };
            specify(new Block() {
                public void run() throws Throwable {
                    repository.update(ENTITY_ID, entity);
                }
            }, should.raise(IllegalArgumentException.class));
        }
    }


    private static Object newAnonymousClassInstance() {
        return new DummyObject() {
        };
    }

    private static Object newLocalClassInstance() {
        class LocalClass extends DummyObject {
        }
        return new LocalClass();
    }

    private static class StaticMemberClass extends DummyObject {
    }

    private static class DelegatingSerializationReplacer implements SerializationReplacer {
        private SerializationReplacer delegate = null;

        public Object replaceSerialized(Object rootObject, Object obj, MetadataBuilder meta) {
            if (delegate != null) {
                return delegate.replaceSerialized(rootObject, obj, meta);
            }
            return obj;
        }

        public Object resolveDeserialized(Object obj, MetadataBuilder meta) {
            if (delegate != null) {
                return delegate.resolveDeserialized(obj, meta);
            }
            return obj;
        }
    }

    // TODO: warn about 'writeReplace' and 'readResolve' in an entity class (or is dimdwarf affected by this at all?). See: com.sun.sgs.impl.service.data.ClassesTable.checkObjectReplacement
}
