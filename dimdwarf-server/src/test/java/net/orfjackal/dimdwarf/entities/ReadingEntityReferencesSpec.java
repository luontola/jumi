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

package net.orfjackal.dimdwarf.entities;

import jdave.Group;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.internal.EntityReference;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.io.*;
import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 25.8.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class ReadingEntityReferencesSpec extends Specification<Object> {

    private static final BigInteger ENTITY_ID = BigInteger.valueOf(42);

    private EntityIdFactory idFactory;
    private EntityLoader loader;
    private EntityManager manager;
    private DummyEntity entity;

    public void create() throws Exception {
        idFactory = mock(EntityIdFactory.class);
        loader = mock(EntityLoader.class);
        manager = new EntityManager(idFactory, null, loader);
        entity = new DummyEntity();
    }

    private static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bytes);
        out.writeObject(obj);
        out.close();
        return bytes.toByteArray();
    }

    private static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Object obj = in.readObject();
        in.close();
        return obj;
    }


    public class WhenTheReferenceWasJustCreated {
        private EntityReference<DummyEntity> ref;

        public Object create() {
            checking(new Expectations() {{
                one(idFactory).newId(); will(returnValue(ENTITY_ID));
            }});
            ref = manager.createReference(entity);
            return null;
        }

        public void theReferenceHasTheEntityId() {
            specify(ref.getId(), should.equal(ENTITY_ID));
        }

        public void theEntityIsCachedLocally() {
            specify(ref.get(), should.equal(entity));
        }
    }

    public class WhenAReferenceIsDeserialized {
        private EntityReferenceImpl<DummyEntity> ref;

        @SuppressWarnings({"unchecked"})
        public Object create() throws IOException, ClassNotFoundException {
            byte[] bytes = serialize(new EntityReferenceImpl<DummyEntity>(ENTITY_ID, entity));
            ref = (EntityReferenceImpl<DummyEntity>) deserialize(bytes);
            ref.setEntityLoader(loader);
            return null;
        }

        public void theReferenceHasTheEntityId() {
            specify(ref.getId(), should.equal(ENTITY_ID));
        }

        public void theEntityIsLazyLoadedFromDatabase() {
            checking(new Expectations() {{
                one(loader).readEntity(ENTITY_ID); will(returnValue(entity));
            }});
            specify(ref.get(), should.equal(entity));
        }
    }
}
