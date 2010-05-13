// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.api.internal.*;
import net.orfjackal.dimdwarf.util.TestUtil;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.io.IOException;

import static net.orfjackal.dimdwarf.util.Objects.uncheckedCast;

@RunWith(JDaveRunner.class)
@Group({"fast"})
public class ReadingEntityReferencesSpec extends Specification<Object> {

    private static final EntityId ID = new EntityObjectId(42);

    private EntitiesPersistedInDatabase database;
    private EntityManager manager;
    private EntityReferenceFactory refFactory;
    private DummyEntity entity;

    public void create() throws Exception {
        database = mock(EntitiesPersistedInDatabase.class);
        manager = new EntityManager(new EntityIdFactory(0), database, new DimdwarfEntityApi());
        refFactory = new EntityReferenceFactoryImpl(manager);
        entity = new DummyEntity();
    }

    private Expectations loadsFromDatabase(final EntityId id, final DummyEntity entity) {
        return new Expectations() {{
            one(database).read(id); will(returnValue(entity));
        }};
    }


    public class WhenTheReferenceWasJustCreated {

        private EntityReference<DummyEntity> ref;

        public void create() {
            ref = refFactory.createReference(entity);
        }

        public void theReferenceGetsAnEntityId() {
            specify(ref.getEntityId(), should.not().equal(null));
        }

        public void theEntityIsCachedLocally() {
            specify(ref.get(), should.equal(entity));
        }
    }

    public class WhenAReferenceHasBeenDeserialized {

        private EntityReferenceImpl<DummyEntity> ref;

        public void create() throws IOException, ClassNotFoundException {
            byte[] bytes = TestUtil.serialize(new EntityReferenceImpl<DummyEntity>(ID, entity));
            ref = uncheckedCast(TestUtil.deserialize(bytes));
            ref.setEntityLocator(manager);
        }

        public void theReferenceHasTheSameEntityIdAsBefore() {
            specify(ref.getEntityId(), should.equal(ID));
        }

        public void theEntityIsLazyLoadedFromDatabase() {
            checking(loadsFromDatabase(ID, entity));
            specify(ref.get(), should.equal(entity));
        }

        public void theEntityIsRegisteredOnLoad() {
            specify(manager.getRegisteredEntities(), should.equal(0));
            checking(loadsFromDatabase(ID, entity));
            ref.get();
            specify(manager.getRegisteredEntities(), should.equal(1));
        }
    }

    public class WhenManyReferencesToTheSameEntityHaveBeenDeserialized {

        private EntityReferenceImpl<DummyEntity> ref1;
        private EntityReferenceImpl<DummyEntity> ref2;

        public void create() throws IOException, ClassNotFoundException {
            byte[] bytes = TestUtil.serialize(new EntityReferenceImpl<DummyEntity>(ID, entity));
            ref1 = uncheckedCast(TestUtil.deserialize(bytes));
            ref1.setEntityLocator(manager);
            ref2 = uncheckedCast(TestUtil.deserialize(bytes));
            ref2.setEntityLocator(manager);
        }

        public void theEntityIsLoadedFromDatabaseOnlyOnce() {
            checking(loadsFromDatabase(ID, entity));
            specify(ref1.get(), should.equal(entity));
            specify(ref2.get(), should.equal(entity));
        }
    }
}
