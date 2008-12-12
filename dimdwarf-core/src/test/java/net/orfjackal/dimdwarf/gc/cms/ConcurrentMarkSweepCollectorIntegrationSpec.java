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

package net.orfjackal.dimdwarf.gc.cms;

import com.google.inject.*;
import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.EntityInfo;
import net.orfjackal.dimdwarf.entities.*;
import net.orfjackal.dimdwarf.gc.entities.GarbageCollectorManager;
import net.orfjackal.dimdwarf.modules.CommonModules;
import net.orfjackal.dimdwarf.modules.options.CmsGarbageCollectionOption;
import net.orfjackal.dimdwarf.server.TestServer;
import net.orfjackal.dimdwarf.tasks.TaskExecutor;
import org.junit.runner.RunWith;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Esko Luontola
 * @since 10.12.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class ConcurrentMarkSweepCollectorIntegrationSpec extends Specification<Object> {

    private Provider<EntityInfo> info;
    private Provider<EntityRepository> entities;
    private Provider<BindingRepository> bindings;
    private TaskExecutor taskContext;
    private TestServer server;

    private GarbageCollectorManager gc;

    private BigInteger liveRootId;
    private BigInteger liveRefId;
    private BigInteger garbageRootId;
    private BigInteger garbageRefId;
    private BigInteger garbageCycleId1;
    private BigInteger garbageCycleId2;

    public void create() throws Exception {
        server = new TestServer(
                new CommonModules(),
                new CmsGarbageCollectionOption()
        );
        server.hideStartupShutdownLogs();
        server.start();

        Injector injector = server.getInjector();
        info = injector.getProvider(EntityInfo.class);
        entities = injector.getProvider(EntityRepository.class);
        bindings = injector.getProvider(BindingRepository.class);
        taskContext = injector.getInstance(TaskExecutor.class);

        gc = injector.getInstance(GarbageCollectorManager.class);

        initGraphNoGarbage();
        createGarbage();
    }

    public void destroy() throws Exception {
        server.shutdownIfRunning();
    }

    private void initGraphNoGarbage() {
        taskContext.execute(new Runnable() {
            public void run() {
                DummyEntity liveRoot = new DummyEntity();
                liveRootId = info.get().getEntityId(liveRoot);
                bindings.get().update("live", liveRoot);

                DummyEntity liveRef = new DummyEntity();
                liveRefId = info.get().getEntityId(liveRef);
                liveRoot.setOther(liveRef);

                DummyEntity garbageRoot = new DummyEntity();
                garbageRootId = info.get().getEntityId(garbageRoot);
                bindings.get().update("garbage", garbageRoot);

                DummyEntity garbageRef = new DummyEntity();
                garbageRefId = info.get().getEntityId(garbageRef);
                garbageRoot.setOther(garbageRef);

                DummyEntity garbageCycle1 = new DummyEntity();
                DummyEntity garbageCycle2 = new DummyEntity();
                garbageCycleId1 = info.get().getEntityId(garbageCycle1);
                garbageCycleId2 = info.get().getEntityId(garbageCycle1);
                garbageCycle1.setOther(garbageCycle2);
                garbageCycle2.setOther(garbageCycle1);
                garbageRef.setOther(garbageCycle1);
            }
        });
    }

    private void createGarbage() {
        taskContext.execute(new Runnable() {
            public void run() {
                bindings.get().delete("garbage");
            }
        });
    }

    private boolean entityExists(final BigInteger id) {
        final AtomicBoolean exists = new AtomicBoolean(false);
        taskContext.execute(new Runnable() {
            public void run() {
                exists.set(entities.get().exists(id));
            }
        });
        return exists.get();
    }


    public class WhenGarbageCollectorIsRun {

        public void create() throws InterruptedException {
            gc.runGarbageCollector();
        }

        public void liveNodesAreKept() {
            specify(entityExists(liveRootId));
            specify(entityExists(liveRefId));
        }

        public void garbageRootNodesAreCollected() {
            specify(entityExists(garbageRootId), should.equal(false));
        }

        public void garbageNodesAreCollected() {
            specify(entityExists(garbageRefId), should.equal(false));
        }

        public void garbageCyclesAreCollected() {
            specify(entityExists(garbageCycleId1), should.equal(false));
            specify(entityExists(garbageCycleId2), should.equal(false));
        }
    }

    // TODO: MutatorListener
}
