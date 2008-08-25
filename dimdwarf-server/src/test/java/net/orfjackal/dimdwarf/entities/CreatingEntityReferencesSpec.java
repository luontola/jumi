/*
 * Dimdwarf Application Server
 * Copyright (c) 2008, Esko Luontola
 * All Rights Reserved.
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

import jdave.Group;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.Entity;
import net.orfjackal.dimdwarf.api.internal.EntityReference;
import org.junit.runner.RunWith;

/**
 * @author Esko Luontola
 * @since 25.8.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class CreatingEntityReferencesSpec extends Specification<Object> {

    private EntityManager manager;
    private Entity entity;

    public void create() throws Exception {
        manager = new EntityManager(null);
        entity = new DummyEntity();
    }


    public class WhenNoReferencesHaveBeenCreated {

        public Object create() {
            return null;
        }

        public void noEntitiesAreRegistered() {
            specify(manager.getRegisteredEntities(), should.equal(0));
        }
    }

    public class WhenAReferenceIsCreated {

        private EntityReference<Entity> ref;

        public Object create() {
            ref = manager.createReference(entity);
            return null;
        }

        public void theReferenceIsCreated() {
            specify(ref, should.not().equal(null));
        }

        public void theEntityIsRegistered() {
            specify(manager.getRegisteredEntities(), should.equal(1));
        }

        public void onMultipleCallsTheSameReferenceInstanceIsReturned() {
            specify(manager.createReference(entity), should.equal(ref));
        }

        public void onMultipleCallsTheEntityIsRegisteredOnlyOnce() {
            manager.createReference(entity);
            specify(manager.getRegisteredEntities(), should.equal(1));
        }
    }

    public class WhenReferencesToManyEntitiesAreCreated {

        private EntityReference<Entity> ref1;
        private EntityReference<DummyEntity> ref2;

        public Object create() {
            ref1 = manager.createReference(entity);
            ref2 = manager.createReference(new DummyEntity());
            return null;
        }

        public void allTheEntitiesAreRegistered() {
            specify(manager.getRegisteredEntities(), should.equal(2));
        }

        public void eachEntityWillGetItsOwnReference() {
            specify(ref1, should.not().equal(ref2));
        }
    }
}
