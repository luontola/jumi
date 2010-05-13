// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db;

/**
 * See:
 * <a href="http://citeseer.ist.psu.edu/old/berenson95critique.html">A Critique of ANSI SQL Isolation Levels (1995)</a>
 */
public enum IsolationLevel {

    /**
     * Prevents: dirty reads, non-repeatable reads, phantom reads, write skew<br>
     * Allows: -
     * <p/>
     * This is the most restrictive isolation level.
     */
    SERIALIZABLE,

    /**
     * Prevents dirty reads, non-repeatable reads, phantom reads<br>
     * Allows: write skew
     */
    SNAPSHOT,

    /**
     * Prevents: dirty reads, non-repeatable reads, write skew<br>
     * Allows: phantom reads
     */
    REPEATABLE_READ,

    /**
     * Prevents: dirty reads<br>
     * Allows: non-repeatable reads, phantom reads, write skew
     */
    READ_COMMITTED,

    /**
     * Prevents: -<br>
     * Allows: dirty reads, non-repeatable reads, phantom reads, write skew
     */
    READ_UNCOMMITTED
}
