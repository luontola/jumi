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

import jdave.Block;
import jdave.Group;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.junit.runner.RunWith;

import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 25.8.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class FlushingEntitiesSpec extends Specification<Object> {

    private EntityStorage storage;
    private EntityManager manager;
    private DummyEntity entity;
    private DummyEntity newEntity;

    public void create() throws Exception {
        storage = mock(EntityStorage.class);
        manager = new EntityManager(new DummyEntityIdFactory(), storage);
        entity = new DummyEntity();
        newEntity = new DummyEntity();
        manager.createReference(entity);
    }


    public class WhenRegisteredEntitiesAreFlushed {

        public Object create() {
            return null;
        }

        public void theyAreStoredInDatabase() {
            checking(new Expectations() {{
                one(storage).update(BigInteger.ONE, entity);
            }});
            manager.flushAllEntities();
        }

        public void flushingTwiseIsNotAllowed() {
            checking(new Expectations() {{
                one(storage).update(BigInteger.ONE, entity);
            }});
            manager.flushAllEntities();
            specify(new Block() {
                public void run() throws Throwable {
                    manager.flushAllEntities();
                }
            }, should.raise(IllegalStateException.class));
        }

        public void managerCanNotBeUsedAfterFlushHasEnded() {
            checking(new Expectations() {{
                one(storage).update(BigInteger.ONE, entity);
            }});
            manager.flushAllEntities();
            specify(new Block() {
                public void run() throws Throwable {
                    manager.createReference(entity);
                }
            }, should.raise(IllegalStateException.class));
            specify(new Block() {
                public void run() throws Throwable {
                    manager.loadEntity(new EntityReferenceImpl<Object>(BigInteger.ONE, null));
                }
            }, should.raise(IllegalStateException.class));
        }
    }

    public class WhenNewEntitiesAreRegisteredDuringFlush {

        public Object create() {
            return null;
        }

        public void theyAreAlsoStoredInDatabase() {
            checking(new Expectations() {{
                one(storage).update(BigInteger.ONE, entity); will(new RegisterEntity(newEntity));
                one(storage).update(BigInteger.valueOf(2), newEntity);
            }});
            manager.flushAllEntities();
        }
    }

    public class WhenAlreadyRegisteredEntitiesAreRegisteredDuringFlush {

        public Object create() {
            return null;
        }

        public void theyWillBeFlushedOnlyOnce() {
            checking(new Expectations() {{
                one(storage).update(BigInteger.ONE, entity); will(new RegisterEntity(entity));
            }});
            manager.flushAllEntities();
        }
    }


    private class RegisterEntity extends CustomAction {
        private final DummyEntity entity;

        public RegisterEntity(DummyEntity entity) {
            super("");
            this.entity = entity;
        }

        public Object invoke(Invocation invocation) throws Throwable {
            manager.createReference(entity);
            return null;
        }
    }
}
