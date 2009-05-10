// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db;

import javax.annotation.concurrent.Immutable;
import java.nio.charset.Charset;

/**
 * @author Esko Luontola
 * @since 12.9.2008
 */
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
