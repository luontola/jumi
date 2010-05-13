// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.scheduler;

import com.google.inject.*;
import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.EntityInfo;
import net.orfjackal.dimdwarf.api.internal.EntityObject;
import net.orfjackal.dimdwarf.entities.*;
import net.orfjackal.dimdwarf.modules.*;
import net.orfjackal.dimdwarf.tasks.TaskExecutor;
import org.junit.runner.RunWith;

import java.io.Serializable;
import java.util.concurrent.Executor;

/**
 * @author Esko Luontola
 * @since 26.11.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class RecoverableSetSpec extends Specification<Object> {

    private static final String PREFIX = "prefix";

    private Executor taskContext;
    private Provider<BindingRepositoryImpl> bindings;
    private Provider<EntityInfo> info;

    private RecoverableSet<StoredValue> set;

    public void create() throws Exception {
        Injector injector = Guice.createInjector(
                new EntityModule(),
                new DatabaseModule(),
                new TaskContextModule()
        );
        taskContext = injector.getInstance(TaskExecutor.class);
        bindings = injector.getProvider(BindingRepositoryImpl.class);
        info = injector.getProvider(EntityInfo.class);
        specify(thereMayBeBindingsInOtherNamespaces());

        set = new RecoverableSetImpl<StoredValue>(PREFIX, bindings, info);
    }

    private boolean thereMayBeBindingsInOtherNamespaces() {
        taskContext.execute(new Runnable() {
            public void run() {
                bindings.get().update("a.shouldNotTouchThis", new DummyEntity());
                bindings.get().update("z.shouldNotTouchThis", new DummyEntity());
            }
        });
        return true;
    }


    public class WhenARecoverableSetIsEmpty {

        public void itContainsNoObjects() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(set.getAll(), should.containExactly());
                }
            });
        }
    }

    public class WhenARecoverableSetContainsSomeObjects {

        private StoredValue value1 = new StoredValue("1");
        private StoredValue value2 = new StoredValue("2");
        private String key1;
        private String key2;

        public void create() {
            taskContext.execute(new Runnable() {
                public void run() {
                    key1 = set.put(value1);
                    key2 = set.put(value2);
                }
            });
        }

        public void itContainsThoseObjects() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(set.getAll(), should.containExactly(value1, value2));
                }
            });
        }

        public void afterRestartANewSetWithTheSamePrefixStillContainsThoseObjects() {
            taskContext.execute(new Runnable() {
                public void run() {
                    set = new RecoverableSetImpl<StoredValue>(PREFIX, bindings, info);
                    specify(set.getAll(), should.containExactly(value1, value2));
                }
            });
        }

        public void theObjectsCanBeRetrievedUsingTheirKeys() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(set.get(key1), should.equal(value1));
                    specify(set.get(key2), should.equal(value2));
                }
            });
        }

        public void afterRemovingAnObjectTheSetDoesNotContainIt() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(set.remove(key1), should.equal(value1));
                    specify(set.get(key1), should.equal(null));
                    specify(set.getAll(), should.containExactly(value2));
                }
            });
        }

        public void duplicateAddsAreIgnored() {
            taskContext.execute(new Runnable() {
                public void run() {
                    set.put(set.get(key1));
                    specify(set.getAll(), should.containExactly(value1, value2));
                }
            });
        }

        public void tryingToAccessObjectsUnderADifferentPrefixIsNotAllowed() {
            final String invalidKey = "otherPrefix" + RecoverableSet.SEPARATOR + "1";
            specify(new Block() {
                public void run() throws Throwable {
                    set.get(invalidKey);
                }
            }, should.raise(IllegalArgumentException.class));
            specify(new Block() {
                public void run() throws Throwable {
                    set.remove(invalidKey);
                }
            }, should.raise(IllegalArgumentException.class));
        }
    }


    private static class StoredValue implements EntityObject, Serializable {

        private final String value;

        public StoredValue(String value) {
            this.value = value;
        }

        public boolean equals(Object obj) {
            if (obj instanceof StoredValue) {
                StoredValue other = (StoredValue) obj;
                return value.equals(other.value);
            }
            return false;
        }

        public String toString() {
            return getClass().getSimpleName() + "[" + value + "]";
        }
    }
}
