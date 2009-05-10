// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

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
