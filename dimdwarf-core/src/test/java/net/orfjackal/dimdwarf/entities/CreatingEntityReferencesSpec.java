// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.api.internal.*;
import org.junit.runner.RunWith;

/**
 * @author Esko Luontola
 * @since 25.8.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class CreatingEntityReferencesSpec extends Specification<Object> {

    private EntityManager manager;
    private EntityReferenceFactory refFactory;

    public void create() throws Exception {
        manager = new EntityManager(new EntityIdFactoryImpl(0), null, new DimdwarfEntityApi());
        refFactory = new EntityReferenceFactoryImpl(manager);
    }


    public class WhenNoReferencesHaveBeenCreated {

        public void noEntitiesAreRegistered() {
            specify(manager.getRegisteredEntities(), should.equal(0));
        }
    }

    public class WhenAReferenceIsCreated {

        private EntityReference<EntityObject> ref;
        private EntityObject entity = new DummyEntity();

        public void create() {
            ref = refFactory.createReference(entity);
        }

        public void theReferenceIsCreated() {
            specify(ref, should.not().equal(null));
        }

        public void theEntityIsRegistered() {
            specify(manager.getRegisteredEntities(), should.equal(1));
        }

        public void theEntityGetsAnId() {
            specify(ref.getEntityId(), should.not().equal(null));
        }

        public void onMultipleCallsAllReferencesToTheSameObjectAreEqual() {
            EntityReference<EntityObject> ref2 = refFactory.createReference(entity);
            specify(ref2 != ref);
            specify(ref2, should.equal(ref));
        }

        public void onMultipleCallsTheEntityIsRegisteredOnlyOnce() {
            refFactory.createReference(entity);
            specify(manager.getRegisteredEntities(), should.equal(1));
        }
    }

    public class WhenReferencesToManyEntitiesAreCreated {

        private EntityReference<DummyEntity> ref1;
        private EntityReference<DummyEntity> ref2;

        public void create() {
            ref1 = refFactory.createReference(new DummyEntity());
            ref2 = refFactory.createReference(new DummyEntity());
        }

        public void allTheEntitiesAreRegistered() {
            specify(manager.getRegisteredEntities(), should.equal(2));
        }

        public void eachEntityGetsItsOwnReference() {
            specify(ref1, should.not().equal(ref2));
        }

        public void eachEntityGetsItsOwnId() {
            EntityId id1 = ref1.getEntityId();
            EntityId id2 = ref2.getEntityId();
            specify(id1, should.not().equal(id2));
        }
    }
}
