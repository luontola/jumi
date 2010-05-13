// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

public interface Clock {

    long currentTimeMillis();

    // TODO: implement ApplicationClock - https://sgs-server.dev.java.net/issues/show_bug.cgi?id=40
}
