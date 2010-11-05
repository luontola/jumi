// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package end2endtests;

import end2endtests.apps.echo.EchoApp;
import end2endtests.runner.*;
import org.junit.*;

public class ClientConnectionTest {

    private final ServerRunner server = new ServerRunner();
    private final ClientRunner client = new ClientRunner(server).withUsername("user1");
    private final ClientRunner client2 = new ClientRunner(server).withUsername("user2");

    @After
    public void shutdownServer() {
        try {
            server.assertIsRunning();
        } finally {
            client2.disconnect();
            client.disconnect();
            server.shutdown();
        }
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

    // TODO: test session messages in a different class
    //@Test
    public void send_and_receive_messages() throws Exception {
        server.startApplication(EchoApp.class);

        client.loginToServer();
        client.isLoggedIn();

        client.sendMessage("hello");
        client.receivesMessage("hello");
    }

    @Test
    public void multiple_clients_can_be_connected_to_the_server() throws Exception {
        server.startApplication(EchoApp.class);

        client.loginToServer();
        client2.loginToServer();

        client.isLoggedIn();
        client2.isLoggedIn();

        client.logout();
        client2.logout();

        client.isLoggedOut();
        client2.isLoggedOut();
    }
}
