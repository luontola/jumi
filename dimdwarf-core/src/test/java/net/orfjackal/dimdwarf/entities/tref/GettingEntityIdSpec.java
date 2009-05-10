// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities.tref;

import com.google.inject.Provider;
import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.internal.EntityObject;
import net.orfjackal.dimdwarf.entities.*;
import net.orfjackal.dimdwarf.util.StubProvider;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 9.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class GettingEntityIdSpec extends Specification<Object> {

    private static final BigInteger ENTITY_ID = BigInteger.valueOf(42);

    private EntityInfoImpl entityInfo;
    private EntityManager entityManager;
    private EntityObject entity;
    private Object proxy;

    public void create() throws Exception {
        entityManager = mock(EntityManager.class);
        entityInfo = new EntityInfoImpl(entityManager);

        entity = new DummyEntity();
        checking(new Expectations() {{
            allowing(entityManager).getEntityId(entity); will(returnValue(ENTITY_ID));
        }});
        Provider<ReferenceFactory> refFactory = StubProvider.<ReferenceFactory>wrap(new ReferenceFactoryImpl(entityManager));
        proxy = new TransparentReferenceFactoryImpl(refFactory).createTransparentReference(entity);
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
