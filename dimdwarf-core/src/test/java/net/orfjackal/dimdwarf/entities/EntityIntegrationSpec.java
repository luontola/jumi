// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import com.google.inject.*;
import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.*;
import net.orfjackal.dimdwarf.api.internal.*;
import net.orfjackal.dimdwarf.modules.CommonModules;
import net.orfjackal.dimdwarf.tasks.TaskExecutor;
import org.junit.runner.RunWith;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(JDaveRunner.class)
@Group({"fast"})
public class EntityIntegrationSpec extends Specification<Object> {

    private Injector injector;
    private Executor taskExecutor;
    private Provider<BindingRepository> bindings;
    private EntityApi entityApi = new DimdwarfEntityApi();

    public void create() throws Exception {
        injector = Guice.createInjector(
                new CommonModules()
        );
        taskExecutor = injector.getInstance(TaskExecutor.class);
        bindings = injector.getProvider(BindingRepository.class);
    }


    public class WhenTasksAreRun {

        public void entitiesCreatedInOneTaskCanBeReadInTheNextTask() {
            final AtomicReference<EntityId> id = new AtomicReference<EntityId>();
            taskExecutor.execute(new Runnable() {
                public void run() {
                    EntityReferenceFactory factory = injector.getInstance(EntityReferenceFactory.class);
                    EntityReference<DummyEntity> ref = factory.createReference(new DummyEntity("foo"));
                    id.set(ref.getEntityId());
                }
            });
            taskExecutor.execute(new Runnable() {
                public void run() {
                    AllEntities entities = injector.getInstance(AllEntities.class);
                    DummyEntity entity = (DummyEntity) entities.getEntityById(id.get());
                    specify(entity.getOther(), should.equal("foo"));
                }
            });
        }

        public void entityIdsAreUniqueOverAllTasks() {
            final Provider<EntityInfo> info = injector.getProvider(EntityInfo.class);
            final AtomicReference<EntityId> idInFirstTask = new AtomicReference<EntityId>();
            taskExecutor.execute(new Runnable() {
                public void run() {
                    EntityId id1 = info.get().getEntityId(new DummyEntity());
                    idInFirstTask.set(id1);
                }
            });
            taskExecutor.execute(new Runnable() {
                public void run() {
                    EntityId id2 = info.get().getEntityId(new DummyEntity());
                    EntityId id1 = idInFirstTask.get();
                    specify(id2, should.not().equal(id1));
                }
            });
        }

        public void entityBindingsCreatedInOneTaskCanBeReadInTheNextTask() {
            taskExecutor.execute(new Runnable() {
                public void run() {
                    bindings.get().update("bar", new DummyEntity("foo"));
                }
            });
            taskExecutor.execute(new Runnable() {
                public void run() {
                    specify(bindings.get().firstKey(), should.equal("bar"));
                    specify(bindings.get().nextKeyAfter("bar"), should.equal(null));
                    DummyEntity entity = (DummyEntity) bindings.get().read("bar");
                    specify(entity.getOther(), should.equal("foo"));
                }
            });
        }

        public void transparentReferencesAreCreatedAutomatically() {
            taskExecutor.execute(new Runnable() {
                public void run() {
                    bindings.get().update("foo", new DummyEntity(new DummyEntity("other")));
                }
            });
            taskExecutor.execute(new Runnable() {
                public void run() {
                    DummyEntity entity = (DummyEntity) bindings.get().read("foo");
                    DummyInterface other = (DummyInterface) entity.getOther();
                    specify(other.getOther(), should.equal("other"));
                    specify(entityApi.isEntity(entity));
                    specify(entityApi.isTransparentReference(other));
                }
            });
        }

        public void referenceFactoryAndEntityLoaderAreInSync() {
            taskExecutor.execute(new Runnable() {
                public void run() {
                    EntityReferenceFactory factory = injector.getInstance(EntityReferenceFactory.class);
                    AllEntities entities = injector.getInstance(AllEntities.class);

                    DummyEntity entity = new DummyEntity();
                    EntityReference<DummyEntity> ref = factory.createReference(entity);

                    DummyEntity loaded = (DummyEntity) entities.getEntityById(ref.getEntityId());
                    specify(loaded, should.equal(entity));
                }
            });
        }
    }

    public class BindingRepositoryBugfix {

        public void bindingsCanBeCreatedForTransparentReferenceProxies() {
            // TODO: move this test to a better place, maybe the tests of the future EntityBindings in public API
            // (the bug was in ConvertEntityToEntityId - it used AllEntities.getEntityId instead of EntityInfo.getEntityId
            // - check that the new tests will notice that bug)
            taskExecutor.execute(new Runnable() {
                public void run() {
                    bindings.get().update("root", new DummyEntity(new DummyEntity("x")));
                }
            });
            taskExecutor.execute(new Runnable() {
                public void run() {
                    DummyEntity root = (DummyEntity) bindings.get().read("root");
                    DummyInterface tref = (DummyInterface) root.getOther();
                    specify(entityApi.isTransparentReference(tref));
                    bindings.get().update("tref", tref);
                }
            });
            taskExecutor.execute(new Runnable() {
                public void run() {
                    DummyEntity e = (DummyEntity) bindings.get().read("tref");
                    specify(e.getOther(), should.equal("x"));
                }
            });
        }
    }

    // TODO: check that entities from one task are not passed on to another task (using AOP)
}
