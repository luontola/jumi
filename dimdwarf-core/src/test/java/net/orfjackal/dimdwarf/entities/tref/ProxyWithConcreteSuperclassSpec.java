// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities.tref;

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.*;
import net.orfjackal.dimdwarf.api.internal.*;
import net.orfjackal.dimdwarf.entities.*;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.io.Serializable;

import static net.orfjackal.dimdwarf.util.StubProvider.providerOf;

@RunWith(JDaveRunner.class)
@Group({"fast"})
public class ProxyWithConcreteSuperclassSpec extends Specification<Object> {

    private static final EntityObjectId ID1 = new EntityObjectId(1);

    private EntityReferenceFactory referenceFactory;
    private TransparentReferenceFactory proxyFactory;
    private EntityApi entityApi = new DimdwarfEntityApi();

    public void create() throws Exception {
        referenceFactory = mock(EntityReferenceFactory.class);
        proxyFactory = new TransparentReferenceFactory(providerOf(referenceFactory));
    }


    public class AProxyWithConcreteSuperclass {
        private MyEntity entity;
        private Object proxy;

        public void create() {
            entity = new MyEntity(42);
            checking(new Expectations() {{
                one(referenceFactory).createReference(entity);
                will(returnValue(new EntityReferenceImpl<EntityObject>(ID1, entity)));
            }});
            proxy = proxyFactory.createTransparentReference(entity);
        }

        public void isATransparentReference() {
            specify(entityApi.isTransparentReference(proxy));
        }

        public void isNotAnEntity() {
            specify(entityApi.isEntity(proxy), should.equal(false));
        }

        public void isAnInstanceOfTheSameClassAsTheEntity() {
            specify(proxy instanceof MyEntity);
        }

        public void delegatesItsMethodsToTheEntity() {
            MyEntity proxy = (MyEntity) this.proxy;
            specify(proxy.getValue(), should.equal(42));
            specify(proxy.value, should.equal(0));
        }

        public void entityReferencesCanNotBeCreatedForTheProxy() {
            final EntityReferenceFactory factory = new EntityReferenceFactoryImpl(new EntityManager(null, null, entityApi));
            specify(new Block() {
                public void run() throws Throwable {
                    factory.createReference(proxy);
                }
            }, should.raise(IllegalArgumentException.class));
        }

        public void doesNotRequireTheEntityToHaveAnAccessibleDefaultConstructor() {
            specify(new Block() {
                public void run() throws Throwable {
                    entity.getClass().getConstructor();
                }
            }, should.raise(NoSuchMethodException.class));
        }
    }


    @Entity(ProxyType.CLASS)
    public static class MyEntity implements EntityObject, Serializable {
        private static final long serialVersionUID = 1L;

        public final int value;

        public MyEntity(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
