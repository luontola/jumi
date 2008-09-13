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

package net.orfjackal.dimdwarf.tasks;

import com.google.inject.Guice;
import com.google.inject.Injector;
import jdave.Group;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.impl.EntityReference;
import net.orfjackal.dimdwarf.context.ThreadContext;
import net.orfjackal.dimdwarf.entities.BindingManager;
import net.orfjackal.dimdwarf.entities.DummyEntity;
import net.orfjackal.dimdwarf.entities.EntityLoader;
import net.orfjackal.dimdwarf.entities.ReferenceFactory;
import net.orfjackal.dimdwarf.modules.DimdwarfModules;
import org.junit.runner.RunWith;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Esko Luontola
 * @since 13.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class RunningTasksSpec extends Specification<Object> {

    private Injector injector;
    private TaskExecutor taskExecutor;

    public void create() throws Exception {
        injector = Guice.createInjector(new DimdwarfModules());
        taskExecutor = injector.getInstance(TaskExecutor.class);
    }

    public void destroy() throws Exception {
        if (ThreadContext.currentContext() != null) {
            ThreadContext.tearDown();
        }
    }

    public class WhenTasksAreRun {

        public Object create() {
            return null;
        }

        public void entitiesCreatedInOneTaskCanBeReadInTheNextTask() {
            final AtomicReference<BigInteger> id = new AtomicReference<BigInteger>();
            taskExecutor.execute(new Runnable() {
                public void run() {
                    ReferenceFactory factory = injector.getInstance(ReferenceFactory.class);
                    EntityReference<DummyEntity> ref = factory.createReference(new DummyEntity("foo"));
                    id.set(ref.getId());
                }
            });
            taskExecutor.execute(new Runnable() {
                public void run() {
                    EntityLoader loader = injector.getInstance(EntityLoader.class);
                    DummyEntity entity = (DummyEntity) loader.loadEntity(id.get());
                    specify(entity.getOther(), should.equal("foo"));
                }
            });
        }

        public void entityBindingsCreatedInOneTaskCanBeReadInTheNextTask() {
            taskExecutor.execute(new Runnable() {
                public void run() {
                    BindingManager bindings = injector.getInstance(BindingManager.class);
                    bindings.update("bar", new DummyEntity("foo"));
                }
            });
        }

        public void referenceFactoryAndEntityLoaderAreInSync() {
            taskExecutor.execute(new Runnable() {
                public void run() {
                    ReferenceFactory factory = injector.getInstance(ReferenceFactory.class);
                    EntityLoader loader = injector.getInstance(EntityLoader.class);

                    DummyEntity entity = new DummyEntity();
                    EntityReference<DummyEntity> ref = factory.createReference(entity);

                    DummyEntity loaded = (DummyEntity) loader.loadEntity(ref.getId());
                    specify(loaded, should.equal(entity));
                    specify(factory, should.equal(loader));
                }
            });
        }
    }
}
