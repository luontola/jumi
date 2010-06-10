// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.test;

import net.orfjackal.dimdwarf.test.apps.echo.EchoApp;
import net.orfjackal.dimdwarf.test.runner.*;
import org.junit.*;

import java.io.IOException;

public class ClientConnectionTest {

    private final ServerRunner server = new ServerRunner();
    private final ClientRunner client = new ClientRunner(server);

    @After
    public void shutdownServer() {
        client.disconnect();
        server.shutdown();
    }

    @Test
    public void login_with_wrong_password_fails() throws IOException {
        server.startApplication(EchoApp.class);

        client.setPassword("wrong-password");
        client.loginToServer();
        client.failsToLogin();
    }

    //@Test
    public void login_and_logout_successfully() throws IOException {
        server.startApplication(EchoApp.class);

        client.loginToServer();
        client.isLoggedIn();

        client.logout();
        client.isLoggedOut();
    }

    //@Test
    public void send_and_receive_messages() throws IOException {
        server.startApplication(EchoApp.class);

        client.loginToServer();
        client.isLoggedIn();

        client.sendMessage("hello");
        client.receivesMessage("hello");
    }
}
