/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008-2009, Esko Luontola. All Rights Reserved.
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

package net.orfjackal.dimdwarf.entities.tref;

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.*;
import net.orfjackal.dimdwarf.api.internal.*;
import net.orfjackal.dimdwarf.entities.*;
import net.orfjackal.dimdwarf.util.StubProvider;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 9.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class ProxyWithConcreteSuperclassSpec extends Specification<Object> {

    private ReferenceFactory referenceFactory;
    private TransparentReferenceFactory proxyFactory;

    public void create() throws Exception {
        referenceFactory = mock(ReferenceFactory.class);
        proxyFactory = new TransparentReferenceFactoryImpl(StubProvider.wrap(referenceFactory));
    }


    public class AProxyWithConcreteSuperclass {
        private MyEntity entity;
        private Object proxy;

        public void create() {
            entity = new MyEntity(42);
            checking(new Expectations() {{
                one(referenceFactory).createReference(entity);
                will(returnValue(new EntityReferenceImpl<EntityObject>(BigInteger.ONE, entity)));
            }});
            proxy = proxyFactory.createTransparentReference(entity);
        }

        public void isATransparentReference() {
            specify(Entities.isTransparentReference(proxy));
        }

        public void isNotAnEntity() {
            specify(Entities.isEntity(proxy), should.equal(false));
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
            final ReferenceFactory factory =
                    new ReferenceFactoryImpl(
                            new EntityManagerImpl(
                                    mock(EntityIdFactory.class),
                                    mock(EntityRepository.class)));
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
