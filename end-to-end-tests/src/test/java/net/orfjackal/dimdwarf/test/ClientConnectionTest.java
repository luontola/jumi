// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.test;

import net.orfjackal.dimdwarf.test.apps.echo.EchoApp;
import net.orfjackal.dimdwarf.test.runner.*;
import org.junit.*;

public class ClientConnectionTest {

    private final ServerRunner server = new ServerRunner();
    private final ClientRunner client = new ClientRunner(server);
    private final ClientRunner client2 = new ClientRunner(server);

    @After
    public void shutdownServer() {
        client2.disconnect();
        client.disconnect();
        server.shutdown();
    }

    @Test
    public void login_with_wrong_password_fails() throws Exception {
        server.startApplication(EchoApp.class);

        client.setPassword("wrong-password");
        client.loginToServer();
        client.failsToLogin();
    }

    @Test
    public void login_and_logout_successfully() throws Exception {
        server.startApplication(EchoApp.class);

        client.loginToServer();
        client.isLoggedIn();

        client.logout();
        client.isLoggedOut();
    }

    //@Test
    public void send_and_receive_messages() throws Exception {
        server.startApplication(EchoApp.class);

        client.loginToServer();
        client.isLoggedIn();

        client.sendMessage("hello");
        client.receivesMessage("hello");
    }

    //@Test
    public void multiple_clients_can_be_connected_to_the_server() throws Exception {
        server.startApplication(EchoApp.class);

        client.loginToServer();
        client2.loginToServer();
        client.isLoggedIn();
        client2.isLoggedIn();

        client.sendMessage("hello 1");
        client2.sendMessage("hello 2");
        client.receivesMessage("hello 1");
        client2.receivesMessage("hello 2");

        client.logout();
        client2.logout();
        client.isLoggedOut();
        client2.isLoggedOut();
    }
}
