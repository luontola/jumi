/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
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
 *     * Neither the name of the copyright holder nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
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

package net.orfjackal.dimdwarf.tref;

import jdave.Block;
import jdave.Group;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.Entity;
import net.orfjackal.dimdwarf.api.ProxyType;
import net.orfjackal.dimdwarf.api.impl.EntityUtil;
import net.orfjackal.dimdwarf.api.impl.IEntity;
import net.orfjackal.dimdwarf.entities.*;
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

    private EntityManager entityManager;
    private TransparentReferenceFactory factory;
    private MyEntity entity;

    public void create() throws Exception {
        entityManager = mock(EntityManager.class);
        factory = new TransparentReferenceFactoryImpl(entityManager);
        entity = new MyEntity();
    }


    public class AProxyWithConcreteSuperclass {

        private Object proxy;

        public Object create() {
            checking(new Expectations() {{
                one(entityManager).createReference(entity); will(returnValue(new EntityReferenceImpl<IEntity>(BigInteger.ONE, entity)));
            }});
            proxy = factory.createTransparentReference(entity);
            return null;
        }

        public void isATransparentReference() {
            specify(EntityUtil.isTransparentReference(proxy));
        }

        public void isNotAnEntity() {
            specify(EntityUtil.isEntity(proxy), should.equal(false));
        }

        public void isAnInstanceOfTheSameClassAsTheEntity() {
            specify(proxy instanceof MyEntity);
        }

        public void delegatesItsMethodsToTheEntity() {
            entity.value = 42;
            MyEntity proxy = (MyEntity) this.proxy;
            specify(proxy.getValue(), should.equal(42));
            specify(proxy.value, should.equal(0));
        }

        public void entityReferencesCanNotBeCreatedForTheProxy() {
            final EntityManager manager = new EntityManagerImpl(mock(EntityIdFactory.class), mock(EntityStorage.class));
            specify(new Block() {
                public void run() throws Throwable {
                    manager.createReference(proxy);
                }
            }, should.raise(IllegalArgumentException.class));
        }
    }


    @Entity(ProxyType.CLASS)
    public static class MyEntity implements IEntity, Serializable {
        private static final long serialVersionUID = 1L;

        public int value = 0;

        public int getValue() {
            return value;
        }
    }
}
