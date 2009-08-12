// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.internal.*;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 25.8.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class CreatingEntityReferencesSpec extends Specification<Object> {

    private EntityIdFactory idFactory;
    private EntityRepository repository;
    private EntityManagerImpl manager;
    private ReferenceFactory refFactory;
    private EntityObject entity;

    public void create() throws Exception {
        idFactory = mock(EntityIdFactory.class);
        repository = mock(EntityRepository.class);
        manager = new EntityManagerImpl(idFactory, repository, new DimdwarfEntityApi());
        refFactory = new ReferenceFactoryImpl(manager);
        entity = new DummyEntity();
    }


    public class WhenNoReferencesHaveBeenCreated {

        public void noEntitiesAreRegistered() {
            specify(manager.getRegisteredEntities(), should.equal(0));
        }
    }

    public class WhenAReferenceIsCreated {

        private EntityReference<EntityObject> ref;

        public void create() {
            checking(new Expectations() {{
                one(idFactory).newId(); will(returnValue(BigInteger.valueOf(42)));
            }});
            ref = refFactory.createReference(entity);
        }

        public void theReferenceIsCreated() {
            specify(ref, should.not().equal(null));
        }

        public void theEntityIsRegistered() {
            specify(manager.getRegisteredEntities(), should.equal(1));
        }

        public void theEntityGetsAnId() {
            specify(ref.getEntityId(), should.equal(BigInteger.valueOf(42)));
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

        private EntityReference<EntityObject> ref1;
        private EntityReference<DummyEntity> ref2;

        public void create() {
            checking(new Expectations() {{
                one(idFactory).newId(); will(returnValue(BigInteger.valueOf(1)));
                one(idFactory).newId(); will(returnValue(BigInteger.valueOf(2)));
            }});
            ref1 = refFactory.createReference(entity);
            ref2 = refFactory.createReference(new DummyEntity());
        }

        public void allTheEntitiesAreRegistered() {
            specify(manager.getRegisteredEntities(), should.equal(2));
        }

        public void eachEntityGetsItsOwnReference() {
            specify(ref1, should.not().equal(ref2));
        }

        public void eachEntityGetsItsOwnId() {
            specify(ref1.getEntityId(), should.equal(BigInteger.valueOf(1)));
            specify(ref2.getEntityId(), should.equal(BigInteger.valueOf(2)));
        }
    }
}
