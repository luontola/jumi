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

package net.orfjackal.dimdwarf.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import net.orfjackal.dimdwarf.db.Blob;
import net.orfjackal.dimdwarf.db.Database;
import net.orfjackal.dimdwarf.db.DatabaseTable;
import net.orfjackal.dimdwarf.entities.*;
import net.orfjackal.dimdwarf.serial.ObjectSerializer;
import net.orfjackal.dimdwarf.serial.ObjectSerializerImpl;

import java.lang.annotation.Annotation;
import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 13.9.2008
 */
public class EntityModule extends AbstractModule {
    protected void configure() {
        bind(ReferenceFactory.class).to(EntityManagerImpl.class);
        bind(EntityLoader.class).to(EntityManagerImpl.class);
        bind(EntityIdFactory.class).toProvider(new Provider<EntityIdFactory>() {
            public EntityIdFactory get() {
                return new EntityIdFactoryImpl(BigInteger.ZERO); // TODO: import from database
            }
        });
        bind(EntityStorage.class).to(EntityStorageImpl.class);

        bindDatabaseTable("bindings", BindingsTable.class);
        bindDatabaseTable("entities", EntitiesTable.class);

        bind(ObjectSerializer.class).to(ObjectSerializerImpl.class);
    }

    private void bindDatabaseTable(final String databaseName, Class<? extends Annotation> databaseAnno) {
        bind(new TypeLiteral<DatabaseTable<Blob, Blob>>() {})
                .annotatedWith(databaseAnno)
                .toProvider(new Provider<DatabaseTable<Blob, Blob>>() {
                    @Inject Provider<Database<Blob, Blob>> db;

                    public DatabaseTable<Blob, Blob> get() {
                        return db.get().openTable(databaseName);
                    }
                });
    }
}
