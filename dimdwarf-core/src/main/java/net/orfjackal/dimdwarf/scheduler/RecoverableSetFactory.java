// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.scheduler;

/**
 * @author Esko Luontola
 * @since 26.11.2008
 */
public interface RecoverableSetFactory {

    <T> RecoverableSet<T> create(String prefix);
}
