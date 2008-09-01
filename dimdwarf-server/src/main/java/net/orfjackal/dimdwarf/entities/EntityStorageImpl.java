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

import net.orfjackal.dimdwarf.api.Entity;
import net.orfjackal.dimdwarf.db.Blob;
import net.orfjackal.dimdwarf.db.DatabaseConnection;
import net.orfjackal.dimdwarf.serial.ObjectSerializer;

import java.math.BigInteger;

/**
 * This class is stateless. Thread-safeness depends on the injected dependencies.
 *
 * @author Esko Luontola
 * @since 1.9.2008
 */
public class EntityStorageImpl implements EntityStorage {

    private final DatabaseConnection db;
    private final ObjectSerializer serializer;

    public EntityStorageImpl(DatabaseConnection db, ObjectSerializer serializer) {
        this.db = db;
        this.serializer = serializer;
    }

    public Entity read(BigInteger id) throws EntityNotFoundException {
        Blob serialized = db.read(asBytes(id));
        if (serialized.equals(Blob.EMPTY_BLOB)) {
            throw new EntityNotFoundException("id=" + id);
        }
        return (Entity) serializer.deserialize(serialized);
    }

    public void update(BigInteger id, Entity entity) {
        Blob serialized = serializer.serialize(entity);
        db.update(asBytes(id), serialized);
    }

    public void delete(BigInteger id) {
        db.delete(asBytes(id));
    }

    private static Blob asBytes(BigInteger id) {
        return Blob.fromBytes(id.toByteArray());
    }
}
