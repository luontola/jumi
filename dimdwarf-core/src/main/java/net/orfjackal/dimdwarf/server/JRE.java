// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.server;

import java.net.URLClassLoader;

public class JRE {

    public static boolean isJava7() {
        try {
            URLClassLoader.class.getMethod("close");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * @see java.net.URLClassLoader#close()
     */
    public static void closeClassLoader(URLClassLoader cl) {
        try {
            URLClassLoader.class.getMethod("close").invoke(cl);
        } catch (Exception e) {
            throw new RuntimeException("Cannot invoke URLClassLoader.close()", e);
        }
    }
}
