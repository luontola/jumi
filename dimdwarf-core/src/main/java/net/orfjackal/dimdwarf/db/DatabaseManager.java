// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db;

import net.orfjackal.dimdwarf.tx.Transaction;

public interface DatabaseManager {

    Database<Blob, Blob> openConnection(Transaction tx);
}
