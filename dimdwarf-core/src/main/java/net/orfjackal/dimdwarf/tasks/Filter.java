// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tasks;

/**
 * @author Esko Luontola
 * @since 12.11.2008
 */
public interface Filter {

    void filter(Runnable nextInChain);
}
