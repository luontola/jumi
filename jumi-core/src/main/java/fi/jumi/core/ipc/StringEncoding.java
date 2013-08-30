// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import fi.jumi.core.ipc.buffer.IpcBuffer;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class StringEncoding {

    public static String readString(IpcBuffer source) {
        String s = readNullableString(source);
        if (s == null) {
            throw new NullPointerException();
        }
        return s;
    }

    public static String readNullableString(IpcBuffer source) {
        int length = source.readInt();
        if (length < 0) {
            return null;
        } else {
            char[] chars = new char[length];
            for (int i = 0; i < chars.length; i++) {
                chars[i] = source.readChar();
            }
            return new String(chars);
        }
    }

    public static void writeString(IpcBuffer target, String s) {
        if (s == null) {
            throw new NullPointerException();
        }
        writeNullableString(target, s);
    }

    public static void writeNullableString(IpcBuffer target, String s) {
        if (s == null) {
            target.writeInt(-1);
        } else {
            int length = s.length();
            target.writeInt(length);
            for (int i = 0; i < length; i++) {
                target.writeChar(s.charAt(i));
            }
        }
    }
}
