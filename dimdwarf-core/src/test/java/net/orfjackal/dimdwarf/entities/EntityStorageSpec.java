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

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.internal.EntityObject;
import net.orfjackal.dimdwarf.db.*;
import net.orfjackal.dimdwarf.serial.ObjectSerializer;
import static net.orfjackal.dimdwarf.util.Objects.uncheckedCast;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 1.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class EntityStorageSpec extends Specification<Object> {

    private static final BigInteger ENTITY_ID = BigInteger.valueOf(42);

    private DatabaseTableWithMetadata<Blob, Blob> db;
    private ObjectSerializer serializer;
    private EntityStorageImpl storage;
    private EntityObject entity;
    private Blob serialized;

    public void create() throws Exception {
        db = uncheckedCast(mock(DatabaseTableWithMetadata.class));
        serializer = mock(ObjectSerializer.class);
        storage = new EntityStorageImpl(
                new EntityDatabaseTable(db, new ConvertBigIntegerToBytes(), new NoConversion<Blob>()),
                new NoConversion<BigInteger>(),
                new ConvertEntityToBytes(serializer));
        entity = new DummyEntity();
        serialized = Blob.fromBytes(new byte[]{1, 2, 3});
    }

    private static Blob asBytes(BigInteger id) {
        return new ConvertBigIntegerToBytes().forth(id);
    }


    public class AnEntityStorage {

        public void readsEntitiesFromDatabase() {
            checking(new Expectations() {{
                one(db).read(asBytes(ENTITY_ID)); will(returnValue(serialized));
                one(serializer).deserialize(serialized); will(returnValue(entity));
            }});
            specify(storage.read(ENTITY_ID), should.equal(entity));
        }

        public void canNotReadNonexistentEntities() {
            checking(new Expectations() {{
                one(db).read(asBytes(ENTITY_ID)); will(returnValue(Blob.EMPTY_BLOB));
            }});
            specify(new Block() {
                public void run() throws Throwable {
                    storage.read(ENTITY_ID);
                }
            }, should.raise(EntityNotFoundException.class));
        }

        public void writesEntitiesToDatabase() {
            checking(new Expectations() {{
                one(serializer).serialize(entity); will(returnValue(serialized));
                one(db).update(asBytes(ENTITY_ID), serialized);
            }});
            storage.update(ENTITY_ID, entity);
        }

        public void deletesEntitiesFromDatabase() {
            checking(new Expectations() {{
                one(db).delete(asBytes(ENTITY_ID));
            }});
            storage.delete(ENTITY_ID);
        }
    }

    // TODO: do not update database if the entity has not changed
}
