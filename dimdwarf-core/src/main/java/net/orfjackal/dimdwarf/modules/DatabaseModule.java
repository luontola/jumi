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

package net.orfjackal.dimdwarf.modules;

import com.google.inject.*;
import net.orfjackal.dimdwarf.db.*;
import net.orfjackal.dimdwarf.db.inmemory.InMemoryDatabaseManager;
import net.orfjackal.dimdwarf.tx.Transaction;

/**
 * @author Esko Luontola
 * @since 13.9.2008
 */
public class DatabaseModule extends AbstractModule {

    protected void configure() {
        bind(DatabaseManager.class).to(InMemoryDatabaseManager.class);
        bind(new TypeLiteral<Database<Blob, Blob>>() {}).toProvider(DatabaseConnectionProvider.class);
    }

    private static class DatabaseConnectionProvider implements Provider<Database<Blob, Blob>> {
        @Inject public DatabaseManager dbms;
        @Inject public Transaction tx;

        public Database<Blob, Blob> get() {
            return dbms.openConnection(tx);
        }
    }

    public static TypeLiteral<DatabaseTable<Blob, Blob>> databaseTableConnection() {
        return new TypeLiteral<DatabaseTable<Blob, Blob>>() {};
    }

    public static Provider<DatabaseTable<Blob, Blob>> databaseTable(final String name) {
        assert !name.contains(DatabaseTableWithMetadata.META_SEPARATOR);
        return new Provider<DatabaseTable<Blob, Blob>>() {
            @Inject public Provider<Database<Blob, Blob>> db;

            public DatabaseTable<Blob, Blob> get() {
                return db.get().openTable(name);
            }
        };
    }

    public static TypeLiteral<DatabaseTableWithMetadata<Blob, Blob>> databaseTableConnectionWithMetadata() {
        return new TypeLiteral<DatabaseTableWithMetadata<Blob, Blob>>() {};
    }

    public static Provider<DatabaseTableWithMetadata<Blob, Blob>> databaseTableWithMetadata(final String name) {
        assert !name.contains(DatabaseTableWithMetadata.META_SEPARATOR);
        return new Provider<DatabaseTableWithMetadata<Blob, Blob>>() {
            @Inject public Provider<Database<Blob, Blob>> db;

            public DatabaseTableWithMetadata<Blob, Blob> get() {
                return new DatabaseTableWithMetadataImpl<Blob, Blob>(db.get(), name);
            }
        };
    }
}
