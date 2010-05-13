// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db;

import javax.annotation.concurrent.Immutable;
import java.nio.charset.Charset;

@Immutable
public class ConvertStringToBytes implements Converter<String, Blob> {

    private static final Charset CHARSET = Charset.forName("UTF-8");

    public String back(Blob value) {
        if (value == null) {
            return null;
        }
        return new String(value.getByteArray(), CHARSET);
    }

    public Blob forth(String value) {
        if (value == null) {
            return null;
        }
        return Blob.fromBytes(value.getBytes(CHARSET));
    }
}
