// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities.tref;

import com.google.inject.Provider;
import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.api.internal.*;
import net.orfjackal.dimdwarf.entities.*;
import net.orfjackal.dimdwarf.util.StubProvider;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

/**
 * @author Esko Luontola
 * @since 9.9.2008
 */
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
        Provider<EntityReferenceFactory> refFactory = StubProvider.<EntityReferenceFactory>wrap(new EntityReferenceFactoryImpl(entities));
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
