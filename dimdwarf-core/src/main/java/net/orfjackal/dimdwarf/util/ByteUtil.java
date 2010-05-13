// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

import java.io.*;
import java.nio.ByteBuffer;

public class ByteUtil {

    private ByteUtil() {
    }

    public static byte[] asByteArray(InputStream in) throws IOException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(in.available());
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > -1) {
                out.write(buf, 0, len);
            }
            return out.toByteArray();
        } finally {
            in.close();
        }
    }

    public static byte[] asByteArray(ByteBuffer buf) {
        byte[] dest = new byte[buf.remaining()];
        buf.duplicate().get(dest);
        return dest;
    }
}
