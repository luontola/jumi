// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities.tref;

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.api.internal.*;
import net.orfjackal.dimdwarf.entities.*;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import javax.inject.Provider;

import static net.orfjackal.dimdwarf.util.StubProvider.providerOf;

@RunWith(JDaveRunner.class)
@Group({"fast"})
public class GettingEntityIdSpec extends Specification<Object> {

    private static final EntityId ENTITY_ID = new EntityObjectId(42);

    private TrefAwareEntityInfo entityInfo;
    private AllEntities entities;
    private EntityObject entity;
    private Object proxy;

    public void create() throws Exception {
        entities = mock(AllEntities.class);
        entityInfo = new TrefAwareEntityInfo(entities, new DimdwarfEntityApi());

        entity = new DummyEntity();
        checking(new Expectations() {{
            allowing(entities).getEntityId(entity); will(returnValue(ENTITY_ID));
        }});
        Provider<EntityReferenceFactory> refFactory = providerOf((EntityReferenceFactory) new EntityReferenceFactoryImpl(entities));
        proxy = new TransparentReferenceFactory(refFactory).createTransparentReference(entity);
    }


    public class TheEntityId {

        public void canBeGetFromEntity() {
            specify(entityInfo.getEntityId(entity), should.equal(ENTITY_ID));
        }

        public void canBeGetFromTransparentReferenceProxy() {
            specify(entityInfo.getEntityId(proxy), should.equal(ENTITY_ID));
        }

        public void normalObjectsDoNotHaveAnEntityId() {
            specify(new Block() {
                public void run() throws Throwable {
                    entityInfo.getEntityId(new Object());
                }
            }, should.raise(IllegalArgumentException.class));
        }
    }
}
