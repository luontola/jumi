// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

/**
 * @author Esko Luontola
 * @since 24.11.2008
 */
public interface Clock {

    long currentTimeMillis();

    // TODO: implement ApplicationClock - https://sgs-server.dev.java.net/issues/show_bug.cgi?id=40
}
