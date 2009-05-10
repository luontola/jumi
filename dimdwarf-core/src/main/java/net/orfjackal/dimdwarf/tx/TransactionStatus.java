// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tx;

/**
 * @author Esko Luontola
 * @since 18.8.2008
 */
public enum TransactionStatus {
    ACTIVE,
    PREPARING, PREPARED, PREPARE_FAILED,
    COMMITTING, COMMITTED,
    ROLLING_BACK, ROLLED_BACK,
}
