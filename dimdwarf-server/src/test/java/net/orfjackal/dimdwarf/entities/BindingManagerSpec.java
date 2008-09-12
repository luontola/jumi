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
import net.orfjackal.dimdwarf.db.DatabaseTable;
import net.orfjackal.dimdwarf.db.NullConverter;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 12.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class BindingManagerSpec extends Specification<Object> {

    private EntityManager entityManager;
    private EntityLoader entityLoader;
    private DatabaseTable<String, BigInteger> bindingTable;
    private BindingManagerImpl bindingManager;

    private DummyEntity entity = new DummyEntity();
    private String binding = "binding";
    private BigInteger id = BigInteger.ONE;

    @SuppressWarnings({"unchecked"})
    public void create() throws Exception {
        entityManager = mock(EntityManager.class);
        entityLoader = mock(EntityLoader.class);
        bindingTable = mock(DatabaseTable.class);
        bindingManager = new BindingManagerImpl(bindingTable, new NullConverter<String>(), new EntityIdConverter(entityManager, entityLoader));

        checking(new Expectations() {{
            allowing(entityManager).createReference((Object) entity); will(returnValue(new EntityReferenceImpl<Object>(id, entity)));
            allowing(entityLoader).loadEntity(id); will(returnValue(entity));
        }});
    }

    public class ABindingManager {

        public Object create() {
            return null;
        }

        public void createsBindingsForEntities() throws UnsupportedEncodingException {
            checking(new Expectations() {{
                one(bindingTable).update(binding, id);
            }});
            bindingManager.update(binding, entity);
        }

        public void readsEntitiesFromBindings() {
            checking(new Expectations() {{
                one(bindingTable).read(binding); will(returnValue(id));
            }});
            specify(bindingManager.read(binding), should.equal(entity));
        }

        public void deletesBindings() {
            checking(new Expectations() {{
                one(bindingTable).delete(binding);
            }});
            bindingManager.delete(binding);
        }

        public void findsTheFirstBinding() {
            checking(new Expectations() {{
                one(bindingTable).firstKey(); will(returnValue(binding));
            }});
            specify(bindingManager.firstKey(), should.equal(binding));
        }

        public void findsTheNextBinding() {
            checking(new Expectations() {{
                one(bindingTable).nextKeyAfter(binding); will(returnValue("binding2"));
            }});
            specify(bindingManager.nextKeyAfter(binding), should.equal("binding2"));
        }
    }
}
