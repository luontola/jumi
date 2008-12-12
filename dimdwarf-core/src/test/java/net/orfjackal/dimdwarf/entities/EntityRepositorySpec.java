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

import com.google.inject.*;
import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.modules.*;
import net.orfjackal.dimdwarf.modules.options.NullGarbageCollectionOption;
import net.orfjackal.dimdwarf.tasks.TaskExecutor;
import org.junit.runner.RunWith;

import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 1.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class EntityRepositorySpec extends Specification<Object> {

    private static final BigInteger ENTITY_ID = BigInteger.valueOf(42);
    private static final BigInteger INVALID_ENTITY_ID = BigInteger.valueOf(999);

    private TaskExecutor taskContext;
    private Provider<EntityRepository> entities;

    public void create() throws Exception {
        Injector injector = Guice.createInjector(
                new TaskContextModule(),
                new DatabaseModule(),
                new EntityModule(),
                new NullGarbageCollectionOption()
        );
        taskContext = injector.getInstance(TaskExecutor.class);
        entities = injector.getProvider(EntityRepository.class);
    }


    public class AnEntityRepository {

        public void create() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(entities.get().exists(ENTITY_ID), should.equal(false));
                    entities.get().update(ENTITY_ID, new DummyEntity("A"));
                }
            });
        }

        public void createsEntities() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(entities.get().exists(ENTITY_ID));
                }
            });
        }

        public void readsEntities() {
            taskContext.execute(new Runnable() {
                public void run() {
                    DummyInterface e = (DummyInterface) entities.get().read(ENTITY_ID);
                    specify(e.getOther(), should.equal("A"));
                }
            });
        }

        public void updatesEntities() {
            // This is not a realistic test case, because entities are always saved at the end of the task,
            // and they are read using EntityManager. If an ID is already already owned by an entity, the only
            // possible object to be passed to the update() method is the same object which was read earlier
            // with the read() method. Would it be better to remove this test case?
            taskContext.execute(new Runnable() {
                public void run() {
                    entities.get().update(ENTITY_ID, new DummyEntity("B"));
                    DummyInterface e = (DummyInterface) entities.get().read(ENTITY_ID);
                    specify(e.getOther(), should.equal("B"));
                }
            });
        }

        public void deletesEntities() {
            taskContext.execute(new Runnable() {
                public void run() {
                    entities.get().delete(ENTITY_ID);
                    specify(entities.get().exists(ENTITY_ID), should.equal(false));
                    specify(new Block() {
                        public void run() throws Throwable {
                            entities.get().read(ENTITY_ID);
                        }
                    }, should.raise(EntityNotFoundException.class));
                }
            });
        }

        public void canNotReadNonexistentEntities() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(entities.get().exists(INVALID_ENTITY_ID), should.equal(false));
                    specify(new Block() {
                        public void run() throws Throwable {
                            entities.get().read(INVALID_ENTITY_ID);
                        }
                    }, should.raise(EntityNotFoundException.class));
                }
            });
        }
    }

    // TODO: do not update database if the entity has not changed
}
