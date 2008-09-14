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
public class BindingStorageSpec extends Specification<Object> {

    private BindingStorage bindingStorage;
    private DatabaseManager dbms;
    private TransactionCoordinator tx;

    @SuppressWarnings({"unchecked"})
    public void create() throws Exception {
        dbms = new InMemoryDatabase();
        bindingStorage = createBindingStorage(newDatabaseConnection());
    }

    private Database<Blob, Blob> newDatabaseConnection() {
        tx = new TransactionImpl();
        return dbms.openConnection(tx.getTransaction());
    }

    private BindingStorageImpl createBindingStorage(Database<Blob, Blob> db) {
        DatabaseTable<Blob, Blob> bindings = db.openTable("bindings");
        DatabaseTable<Blob, Blob> entities = db.openTable("entities");

        EntityManagerImpl entityManager =
                new EntityManagerImpl(
                        new EntityIdFactoryImpl(BigInteger.ZERO),
                        new EntityStorageImpl(
                                entities,
                                new ConvertBigIntegerToBytes(),
                                new ConvertEntityToBytes(new ObjectSerializerImpl())),
                        tx.getTransaction());

        return new BindingStorageImpl(
                bindings,
                new NoConversion<String>(),
                new ConvertStringToBytes(),
                new ConvertEntityToEntityId(entityManager, entityManager),
                new ConvertBigIntegerToBytes());
    }


    public class BrowsingBindings {

        public Object create() {
            DummyEntity foo = new DummyEntity();
            foo.setOther("foo");
            bindingStorage.update("foo", foo);
            bindingStorage.update("foo.2", new DummyEntity());
            bindingStorage.update("foo.1", new DummyEntity());
            bindingStorage.update("bar.x", new DummyEntity());
            bindingStorage.update("bar.y", new DummyEntity());
            tx.prepareAndCommit();
            bindingStorage = createBindingStorage(newDatabaseConnection());
            return null;
        }

        public void bindingsAreInAlphabeticalOrder() {
            specify(bindingStorage.firstKey(), should.equal("bar.x"));
        }

        public void whenBindingsHaveTheSamePrefixTheShortestBindingIsFirst() {
            specify(bindingStorage.nextKeyAfter("foo"), should.equal("foo.1"));
            specify(bindingStorage.nextKeyAfter("foo.1"), should.equal("foo.2"));
            specify(bindingStorage.nextKeyAfter("foo.2"), should.equal(null));
        }

        public void entitiesCanBeAccessedByTheBindingName() {
            DummyEntity entity = (DummyEntity) bindingStorage.read("foo");
            specify(entity.getOther(), should.equal("foo"));
        }
    }
}
