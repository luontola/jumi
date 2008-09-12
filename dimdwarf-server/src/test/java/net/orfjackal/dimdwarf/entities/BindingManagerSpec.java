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
import net.orfjackal.dimdwarf.db.*;
import net.orfjackal.dimdwarf.db.inmemory.InMemoryDatabase;
import net.orfjackal.dimdwarf.serial.ObjectSerializerImpl;
import net.orfjackal.dimdwarf.tx.TransactionCoordinator;
import net.orfjackal.dimdwarf.tx.TransactionImpl;
import org.junit.runner.RunWith;

import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 12.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class BindingManagerSpec extends Specification<Object> {

    private BindingManager bindingManager;
    private InMemoryDatabase dbms;
    private TransactionCoordinator tx;

    @SuppressWarnings({"unchecked"})
    public void create() throws Exception {
        dbms = new InMemoryDatabase("bindings", "entities");
        bindingManager = createBindingManager(newDatabaseConnection());
    }

    private Database<Blob, Blob> newDatabaseConnection() {
        tx = new TransactionImpl();
        return dbms.openConnection(tx.getTransaction());
    }

    private BindingManagerImpl createBindingManager(Database<Blob, Blob> db) {
        DatabaseTable<Blob, Blob> bindings = db.openTable("bindings");
        DatabaseTable<Blob, Blob> entities = db.openTable("entities");

        EntityManagerImpl entityManager =
                new EntityManagerImpl(
                        new EntityIdFactoryImpl(BigInteger.ZERO),
                        new EntityStorageImpl(
                                entities, new BigIntegerConverter(), new EntityConverter(new ObjectSerializerImpl())));

        DatabaseTableAdapter<String, BigInteger, Blob, Blob> bindingsTable =
                new DatabaseTableAdapter<String, BigInteger, Blob, Blob>(
                        bindings, new StringConverter(), new BigIntegerConverter());

        return new BindingManagerImpl(
                bindingsTable, new NullConverter<String>(), new EntityIdConverter(entityManager, entityManager));
    }


    public class BrowsingBindings {

        public Object create() {
            DummyEntity foo = new DummyEntity();
            foo.setOther("foo");
            bindingManager.update("foo", foo);
            bindingManager.update("foo.2", new DummyEntity());
            bindingManager.update("foo.1", new DummyEntity());
            bindingManager.update("bar.x", new DummyEntity());
            bindingManager.update("bar.y", new DummyEntity());
            tx.prepareAndCommit();
            bindingManager = createBindingManager(newDatabaseConnection());
            return null;
        }

        public void bindingsAreInAlphabeticalOrder() {
            specify(bindingManager.firstKey(), should.equal("bar.x"));
        }

        public void whenBindingsHaveTheSamePrefixTheShortestBindingIsFirst() {
            specify(bindingManager.nextKeyAfter("foo"), should.equal("foo.1"));
            specify(bindingManager.nextKeyAfter("foo.1"), should.equal("foo.2"));
            specify(bindingManager.nextKeyAfter("foo.2"), should.equal(null));
        }

        // TODO
//        public void entitiesCanBeAccessedByTheBindingName() {
//            DummyEntity entity = (DummyEntity) bindingManager.read("foo");
//            specify(entity.getOther(), should.equal("foo"));
//        }
    }
}
