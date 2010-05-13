// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.modules;

import com.google.inject.*;
import net.orfjackal.dimdwarf.db.*;
import net.orfjackal.dimdwarf.db.inmemory.InMemoryDatabaseManager;
import net.orfjackal.dimdwarf.tx.Transaction;

public class DatabaseModule extends AbstractModule {

    protected void configure() {
        bind(DatabaseManager.class).to(InMemoryDatabaseManager.class);
    }

    @Provides
    Database<Blob, Blob> database(DatabaseManager dbms, Transaction tx) {
        return dbms.openConnection(tx);
    }

    public static TypeLiteral<DatabaseTable<Blob, Blob>> databaseTableConnection() {
        return new TypeLiteral<DatabaseTable<Blob, Blob>>() {};
    }

    public static Provider<DatabaseTable<Blob, Blob>> databaseTable(final String name) {
        return new Provider<DatabaseTable<Blob, Blob>>() {
            @Inject public Provider<Database<Blob, Blob>> db;

            public DatabaseTable<Blob, Blob> get() {
                return db.get().openTable(name);
            }
        };
    }
}
