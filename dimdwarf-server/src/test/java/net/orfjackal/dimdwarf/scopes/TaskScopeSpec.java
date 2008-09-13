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

package net.orfjackal.dimdwarf.scopes;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import jdave.Block;
import jdave.Group;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.context.Context;
import net.orfjackal.dimdwarf.context.SimpleContext;
import net.orfjackal.dimdwarf.context.ThreadContext;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Esko Luontola
 * @since 13.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class TaskScopeSpec extends Specification<Object> {

    private Injector injector;
    private Provider<Context> contextProvider;

    public void create() throws Exception {
        injector = Guice.createInjector(new MyModule());
        contextProvider = injector.getProvider(Context.class);
    }

    public void destroy() throws Exception {
        if (ThreadContext.currentContext() != null) {
            ThreadContext.tearDown();
        }
    }

    private static void awaitForOthers(CountDownLatch latch) {
        try {
            latch.countDown();
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public class WhenOutOfTaskScope {

        public Object create() {
            return null;
        }

        public void taskScopedBindingsCanNotBeAccessed() {
            specify(new Block() {
                public void run() throws Throwable {
                    injector.getInstance(MyService.class);
                }
            }, should.raise(IllegalStateException.class));
        }

        public void taskScopedBindingsCanNotBeAccessedInOtherThreadContexts() {
            ThreadContext.setUp(new SimpleContext(new HashMap<Class<?>, Object>()));
            specify(new Block() {
                public void run() throws Throwable {
                    injector.getInstance(MyService.class);
                }
            }, should.raise(IllegalStateException.class));
        }
    }

    public class WhenInsideTaskScope {

        public Object create() {
            ThreadContext.setUp(contextProvider.get());
            return null;
        }

        public void taskScopedBindingsMayBeAccessed() {
            specify(injector.getInstance(MyService.class), should.not().equal(null));
        }

        public void onlyOneInstanceIsCreatedPerBinding() {
            MyService s1 = injector.getInstance(MyService.class);
            MyService s2 = injector.getInstance(MyService.class);
            specify(s1, should.equal(s2));
        }

        public void eachNewScopeHasItsOwnInstances() {
            MyService s1 = injector.getInstance(MyService.class);
            ThreadContext.tearDown();
            ThreadContext.setUp(contextProvider.get());
            MyService s2 = injector.getInstance(MyService.class);
            specify(s1, should.not().equal(s2));
        }

        public void eachConcurrentScopeHasItsOwnInstances() throws InterruptedException {
            final AtomicReference<MyService> s1 = new AtomicReference<MyService>();
            final AtomicReference<MyService> s2 = new AtomicReference<MyService>();
            final CountDownLatch taskRunning = new CountDownLatch(2);
            final CountDownLatch gotInstance = new CountDownLatch(2);

            Thread t1 = new Thread(new Runnable() {
                public void run() {
                    ThreadContext.runInContext(contextProvider.get(), new Runnable() {
                        public void run() {
                            awaitForOthers(taskRunning);
                            s1.set(injector.getInstance(MyService.class));
                            awaitForOthers(gotInstance);
                        }
                    });
                }
            });
            Thread t2 = new Thread(new Runnable() {
                public void run() {
                    ThreadContext.runInContext(contextProvider.get(), new Runnable() {
                        public void run() {
                            awaitForOthers(taskRunning);
                            s2.set(injector.getInstance(MyService.class));
                            awaitForOthers(gotInstance);
                        }
                    });
                }
            });
            t1.setDaemon(true);
            t1.start();
            t2.setDaemon(true);
            t2.start();
            gotInstance.await(100, TimeUnit.MILLISECONDS);

            specify(s1.get(), should.not().equal(null));
            specify(s2.get(), should.not().equal(null));
            specify(s1.get(), should.not().equal(s2.get()));
        }

        public void boundInstancesCanBeAccessedThroughThreadContext() {
            specify(ThreadContext.get(MyService.class), should.not().equal(null));
        }
    }


    public static class MyModule extends AbstractModule {
        protected void configure() {
            bind(Context.class)
                    .to(TaskScopedContext.class);
            bind(MyService.class)
                    .to(MyServiceImpl.class)
                    .in(DimdwarfScopes.TASK);
        }
    }

    public interface MyService {
    }

    public static class MyServiceImpl implements MyService {
    }
}
