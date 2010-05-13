// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tx;

public enum TransactionStatus {
    ACTIVE,
    PREPARING, PREPARED, PREPARE_FAILED,
    COMMITTING, COMMITTED,
    ROLLING_BACK, ROLLED_BACK,
}
