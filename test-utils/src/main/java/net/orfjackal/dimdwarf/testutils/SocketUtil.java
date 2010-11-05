// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.testutils;

import java.io.IOException;
import java.net.ServerSocket;

public class SocketUtil {

    public static int anyFreePort() {
        try {
            ServerSocket socket = new ServerSocket(0);
            int port = socket.getLocalPort();
            socket.close();
            assert port > 0;
            return port;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
