// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db;

import net.orfjackal.dimdwarf.tx.Transaction;

public interface DatabaseManager {

    Database<Blob, Blob> openConnection(Transaction tx);
}
