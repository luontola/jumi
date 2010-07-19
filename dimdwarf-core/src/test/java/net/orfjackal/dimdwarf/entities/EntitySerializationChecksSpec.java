// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.internal.*;
import net.orfjackal.dimdwarf.entities.tref.*;
import net.orfjackal.dimdwarf.serial.ObjectSerializer;
import org.junit.runner.RunWith;

@RunWith(JDaveRunner.class)
@Group({"fast"})
public class EntitySerializationChecksSpec extends Specification<Object> {

    private static final ReplaceEntitiesWithTransparentReferences DISABLE_TREF_CREATION = new ReplaceEntitiesWithTransparentReferences(null, null) {
        public Object replaceSerialized(Object rootObject, Object obj) {
            return obj;
        }
    };

    private final ObjectSerializer serializer = new ObjectSerializer();
    private final TrefAwareEntitySerializationFilter filter = new TrefAwareEntitySerializationFilter(
            DISABLE_TREF_CREATION,
            new CheckDirectlyReferredEntitySerialized(new DimdwarfEntityApi()),
            new CheckInnerClassSerialized(),
            null
    );

    private final DummyEntity entity = new DummyEntity();


    public class ReferringEntities {

        public void referringAnEntityDirectlyIsForbidden() {
            entity.other = new DummyEntity();

            specify(new Block() {
                public void run() throws Throwable {
                    serializer.serialize(entity, filter);
                }
            }, should.raise(IllegalArgumentException.class));
        }

        public void referringAnEntityThroughAnEntityReferenceIsAllowed() {
            entity.other = referenceTo(new DummyEntity());

            serializer.serialize(entity, filter);
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
                    serializer.serialize(entity, filter);
                }
            }, should.raise(IllegalArgumentException.class));
        }

        public void serializingLocalClassesIsForbidden() {
            entity.other = newLocalClassInstance();

            specify(new Block() {
                public void run() throws Throwable {
                    serializer.serialize(entity, filter);
                }
            }, should.raise(IllegalArgumentException.class));
        }

        public void serializingStaticMemberClassesIsAllowed() {
            entity.other = new StaticMemberClass();

            serializer.serialize(entity, filter);
        }
    }


    private static EntityReferenceImpl<DummyEntity> referenceTo(DummyEntity entity) {
        return new EntityReferenceImpl<DummyEntity>(new EntityObjectId(entity.hashCode()), entity);
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

    // TODO: warn about 'writeReplace' and 'readResolve' in an entity class (or is dimdwarf affected by this at all?). See: com.sun.sgs.impl.service.data.ClassesTable.checkObjectReplacement
}
