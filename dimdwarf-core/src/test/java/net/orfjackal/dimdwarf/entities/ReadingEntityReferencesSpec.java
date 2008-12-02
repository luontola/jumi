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
import net.orfjackal.dimdwarf.api.internal.EntityReference;
import static net.orfjackal.dimdwarf.util.Objects.uncheckedCast;
import net.orfjackal.dimdwarf.util.TestUtil;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 25.8.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class ReadingEntityReferencesSpec extends Specification<Object> {

    private static final BigInteger ENTITY_ID = BigInteger.valueOf(42);

    private EntityIdFactory idFactory;
    private EntityRepository repository;
    private EntityManagerImpl manager;
    private ReferenceFactory refFactory;
    private DummyEntity entity;

    public void create() throws Exception {
        idFactory = mock(EntityIdFactory.class);
        repository = mock(EntityRepository.class);
        manager = new EntityManagerImpl(idFactory, repository);
        refFactory = new ReferenceFactoryImpl(manager);
        entity = new DummyEntity();
    }

    private Expectations loadsFromRepository(final BigInteger id, final DummyEntity entity) {
        return new Expectations() {{
            one(repository).read(id); will(returnValue(entity));
        }};
    }


    public class WhenTheReferenceWasJustCreated {

        private EntityReference<DummyEntity> ref;

        public void create() {
            checking(new Expectations() {{
                one(idFactory).newId(); will(returnValue(ENTITY_ID));
            }});
            ref = refFactory.createReference(entity);
        }

        public void theReferenceHasTheEntityId() {
            specify(ref.getEntityId(), should.equal(ENTITY_ID));
        }

        public void theEntityIsCachedLocally() {
            specify(ref.get(), should.equal(entity));
        }
    }

    public class WhenAReferenceHasBeenDeserialized {

        private EntityReferenceImpl<DummyEntity> ref;

        public void create() throws IOException, ClassNotFoundException {
            byte[] bytes = TestUtil.serialize(new EntityReferenceImpl<DummyEntity>(ENTITY_ID, entity));
            ref = uncheckedCast(TestUtil.deserialize(bytes));
            ref.setEntityManager(manager);
        }

        public void theReferenceHasTheEntityId() {
            specify(ref.getEntityId(), should.equal(ENTITY_ID));
        }

        public void theEntityIsLazyLoadedFromDatabase() {
            checking(loadsFromRepository(ENTITY_ID, entity));
            specify(ref.get(), should.equal(entity));
        }

        public void theEntityIsRegisteredOnLoad() {
            specify(manager.getRegisteredEntities(), should.equal(0));
            checking(loadsFromRepository(ENTITY_ID, entity));
            ref.get();
            specify(manager.getRegisteredEntities(), should.equal(1));
        }
    }

    public class WhenManyReferencesToTheSameEntityHaveBeenDeserialized {

        private EntityReferenceImpl<DummyEntity> ref1;
        private EntityReferenceImpl<DummyEntity> ref2;

        public void create() throws IOException, ClassNotFoundException {
            byte[] bytes = TestUtil.serialize(new EntityReferenceImpl<DummyEntity>(ENTITY_ID, entity));
            ref1 = uncheckedCast(TestUtil.deserialize(bytes));
            ref1.setEntityManager(manager);
            ref2 = uncheckedCast(TestUtil.deserialize(bytes));
            ref2.setEntityManager(manager);
        }

        public void theEntityIsLoadedFromDatabaseOnlyOnce() {
            checking(loadsFromRepository(ENTITY_ID, entity));
            specify(ref1.get(), should.equal(entity));
            specify(ref2.get(), should.equal(entity));
        }
    }
}
