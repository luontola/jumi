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

package net.orfjackal.dimdwarf.entities.tref;

import jdave.Block;
import jdave.Group;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.impl.IEntity;
import net.orfjackal.dimdwarf.context.SimpleContext;
import net.orfjackal.dimdwarf.context.ThreadContext;
import net.orfjackal.dimdwarf.entities.DummyEntity;
import net.orfjackal.dimdwarf.entities.EntityReferenceImpl;
import net.orfjackal.dimdwarf.entities.ReferenceFactory;
import net.orfjackal.dimdwarf.util.DummyProvider;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 9.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class AccessingEntityIdSpec extends Specification<Object> {

    private static final BigInteger ENTITY_ID = BigInteger.valueOf(42);

    private ReferenceFactory referenceFactory;
    private IEntity entity;
    private Object proxy;

    public void create() throws Exception {
        referenceFactory = mock(ReferenceFactory.class);
        TransparentReferenceFactory proxyFactory = new TransparentReferenceFactoryImpl(DummyProvider.with(referenceFactory));
        entity = new DummyEntity();
        checking(new Expectations() {{
            allowing(referenceFactory).createReference(entity); will(returnValue(new EntityReferenceImpl<IEntity>(ENTITY_ID, entity)));
        }});
        proxy = proxyFactory.createTransparentReference(entity);
        ThreadContext.setUp(new SimpleContext(ReferenceFactory.class, referenceFactory));
    }

    public void destroy() throws Exception {
        ThreadContext.tearDown();
    }


    public class TheEntityId {

        public Object create() {
            return null;
        }

        public void canBeGetFromEntity() {
            specify(EntityHelper.getId(entity), should.equal(ENTITY_ID));
        }

        public void canBeGetFromTransparentReferenceProxy() {
            specify(EntityHelper.getId(proxy), should.equal(ENTITY_ID));
        }

        public void normalObjectsDoNotHaveAnEntityId() {
            specify(new Block() {
                public void run() throws Throwable {
                    EntityHelper.getId(new Object());
                }
            }, should.raise(IllegalArgumentException.class));
        }
    }
}
