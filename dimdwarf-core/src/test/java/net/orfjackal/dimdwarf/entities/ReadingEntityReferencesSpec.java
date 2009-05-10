// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

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
