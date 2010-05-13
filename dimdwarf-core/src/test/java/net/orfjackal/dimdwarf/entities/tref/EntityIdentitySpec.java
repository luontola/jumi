// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities.tref;

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.internal.*;
import net.orfjackal.dimdwarf.context.*;
import net.orfjackal.dimdwarf.entities.*;
import net.orfjackal.dimdwarf.util.StubProvider;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

/**
 * @author Esko Luontola
 * @since 5.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class EntityIdentitySpec extends Specification<Object> {

    private EntityReferenceFactory referenceFactory;
    private TransparentReferenceFactoryImpl proxyFactory;
    private EntityObject ent1;
    private EntityObject ent2;
    private TransparentReference tref1;
    private TransparentReference tref1b;
    private TransparentReference tref2;
    private Object obj;

    public void create() throws Exception {
        referenceFactory = mock(EntityReferenceFactory.class);
        proxyFactory = new TransparentReferenceFactoryImpl(StubProvider.wrap(referenceFactory));
        ent1 = new DummyEntity();
        ent2 = new DummyEntity();
        checking(referencesMayBeCreatedFor(ent1, new EntityObjectId(1)));
        checking(referencesMayBeCreatedFor(ent2, new EntityObjectId(2)));
        tref1 = proxyFactory.createTransparentReference(ent1);
        tref1b = proxyFactory.createTransparentReference(ent1);
        tref2 = proxyFactory.createTransparentReference(ent2);
        obj = new Object();
        ThreadContext.setUp(new FakeContext().with(EntityReferenceFactory.class, referenceFactory));
    }

    public void destroy() throws Exception {
        ThreadContext.tearDown();
    }

    private Expectations referencesMayBeCreatedFor(final EntityObject entity, final EntityObjectId id) {
        return new Expectations() {{
            allowing(referenceFactory).createReference(entity); will(returnValue(new EntityReferenceImpl<EntityObject>(id, entity)));
        }};
    }


    public class EntityIdentityContractsWhenUsingTransparentReferences {

        public void entityEqualsTheSameEntity() {
            specify(EntityHelper.equals(ent1, ent1));
            specify(EntityHelper.equals(ent1, ent2), should.equal(false));
        }

        public void entityEqualsTransparentReferenceForTheSameEntity() {
            specify(EntityHelper.equals(ent1, tref1));
            specify(EntityHelper.equals(tref1, ent1));
            specify(EntityHelper.equals(ent1, tref2), should.equal(false));
            specify(EntityHelper.equals(tref2, ent1), should.equal(false));
        }

        public void transparentReferenceEqualsTransparentReferenceForTheSameEntity() {
            specify(tref1 != tref1b);
            specify(EntityHelper.equals(tref1, tref1));
            specify(EntityHelper.equals(tref1, tref1b));
            specify(EntityHelper.equals(tref1b, tref1));
            specify(EntityHelper.equals(tref1, tref2), should.equal(false));
        }

        public void entityDoesNotEqualOtherObjects() {
            specify(EntityHelper.equals(ent1, obj), should.equal(false));
            specify(EntityHelper.equals(obj, ent1), should.equal(false));
        }

        public void transparentReferenceDoesNotEqualOtherObjects() {
            specify(EntityHelper.equals(tref1, obj), should.equal(false));
            specify(EntityHelper.equals(obj, tref1), should.equal(false));
        }

        public void entityDoesNotEqualNull() {
            specify(EntityHelper.equals(ent1, null), should.equal(false));
            specify(EntityHelper.equals(null, ent1), should.equal(false));
        }

        public void transparentReferenceDoesNotEqualNull() {
            specify(EntityHelper.equals(tref1, null), should.equal(false));
            specify(EntityHelper.equals(null, tref1), should.equal(false));
        }

        public void differentEntitiesHaveDifferentHashCodes() {
            int hc1 = EntityHelper.hashCode(ent1);
            int hc2 = EntityHelper.hashCode(ent2);
            specify(hc1, should.not().equal(hc2));
        }

        public void transparentReferencesForDifferentEntitiesHaveDifferentHashCodes() {
            int hc1 = EntityHelper.hashCode(tref1);
            int hc2 = EntityHelper.hashCode(tref2);
            specify(hc1, should.not().equal(hc2));
        }

        public void transparentReferencesForTheSameEntityHaveTheSameHashCode() {
            int hc1 = EntityHelper.hashCode(tref1);
            int hc1b = EntityHelper.hashCode(tref1b);
            specify(hc1, should.equal(hc1b));
        }

        public void entitiesAndTheirTransparentReferencesHaveTheSameHashCode() {
            specify(EntityHelper.hashCode(ent1), EntityHelper.hashCode(tref1));
            specify(EntityHelper.hashCode(ent2), EntityHelper.hashCode(tref2));
        }

        public void equalsMethodOnProxyWillNotDelegateToEntity() {
            final EntityObject entity = mock(EntityObject.class);
            checking(referencesMayBeCreatedFor(entity, new EntityObjectId(3)));
            TransparentReference proxy = proxyFactory.createTransparentReference(entity);
            proxy.equals(entity);
        }

        public void hashCodeMethodOnProxyWillNotDelegateToEntity() {
            final EntityObject entity = mock(EntityObject.class);
            checking(referencesMayBeCreatedFor(entity, new EntityObjectId(3)));
            TransparentReference proxy = proxyFactory.createTransparentReference(entity);
            proxy.hashCode();
        }
    }
}
