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

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.db.*;
import net.orfjackal.dimdwarf.entities.dao.EntityDao;
import net.orfjackal.dimdwarf.gc.entities.*;
import net.orfjackal.dimdwarf.modules.FakeGarbageCollectionModule;
import net.orfjackal.dimdwarf.serial.*;
import static net.orfjackal.dimdwarf.util.Objects.uncheckedCast;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 4.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class EntitySerializationChecksSpec extends Specification<Object> {

    private static final BigInteger ENTITY_ID = BigInteger.valueOf(42);

    private DatabaseTableWithMetadata<Blob, Blob> db;
    private GcAwareEntityRepository repository;
    private DummyEntity entity;
    private DelegatingSerializationReplacer replacer;

    public void create() throws Exception {
        db = uncheckedCast(mock(DatabaseTableWithMetadata.class));
        SerializationListener[] listeners = {
                new CheckDirectlyReferredEntitySerialized(),
                new CheckInnerClassSerialized()
        };
        replacer = new DelegatingSerializationReplacer();
        ObjectSerializer serializer = new ObjectSerializerImpl(listeners, new SerializationReplacer[]{replacer});

        repository =
                new GcAwareEntityRepository(
                        new EntityDao(
                                db,
                                new ConvertBigIntegerToBytes(),
                                new NoConversion<Blob>()),
                        serializer,
                        new FakeGarbageCollectionModule.NullMutatorListener(),
                        new EntityReferenceUtil());
        entity = new DummyEntity();
    }

    private static Blob asBytes(BigInteger id) {
        return new ConvertBigIntegerToBytes().forth(id);
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
            checking(new Expectations() {{
                one(db).update(with(equal(asBytes(ENTITY_ID))), with(aNonNull(Blob.class)));
            }});
            entity.other = new EntityReferenceImpl<DummyEntity>(BigInteger.valueOf(123), new DummyEntity());
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
            checking(new Expectations() {{
                one(db).update(with(equal(asBytes(ENTITY_ID))), with(aNonNull(Blob.class)));
            }});
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
