// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.test.runner;

import com.sun.sgs.client.*;
import com.sun.sgs.client.simple.*;

import java.io.IOException;
import java.net.PasswordAuthentication;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.concurrent.*;

import static net.orfjackal.dimdwarf.test.runner.ClientRunner.EventType.*;
import static org.junit.Assert.*;

public class ClientRunner {

    private static final Charset MESSAGE_CHARSET = Charset.forName("UTF-8");

    private final SimpleClient client;
    private final String host;
    private final int port;
    private final BlockingQueue<Event> events = new LinkedBlockingQueue<Event>();

    public ClientRunner(ServerRunner server) {
        host = server.getHost();
        port = server.getPort();
        client = new SimpleClient(new MySimpleClientListener());
    }

    public void loginToServer() throws IOException {
        Properties p = new Properties();
        p.setProperty("host", host);
        p.setProperty("port", Integer.toString(port));
        client.login(p);
    }

    public void isLoggedIn() {
        expectEvent(LOGGED_IN);
    }

    public void failsToLogin() {
        expectEvent(LOGIN_FAILED);
    }

    public void logout() {
        client.logout(false);
    }

    public void isLoggedOut() {
        expectEvent(DISCONNECTED);
    }

    public void sendMessage(String message) throws IOException {
        byte[] bytes = message.getBytes(MESSAGE_CHARSET);
        client.send(ByteBuffer.wrap(bytes));
    }

    public void receivesMessage(String expectedMessage) {
        Event event = expectEvent(RECEIVED_MESSAGE);
        assertEquals("message", expectedMessage, event.arg);
    }

    public void disconnect() {
        try {
            if (client.isConnected()) {
                client.logout(true);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private Event expectEvent(EventType expectedType) {
        try {
            Event event = events.poll(5, TimeUnit.SECONDS);
            assertNotNull("timed out while expecting event " + expectedType, event);
            assertEquals("event", expectedType, event.type);
            return event;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    enum EventType {
        LOGGED_IN, LOGIN_FAILED,
        JOINED_CHANNEL, RECEIVED_MESSAGE,
        RECONNECTING, RECONNECTED, DISCONNECTED
    }

    private static class Event {
        public final EventType type;
        public final Object arg;

        private Event(EventType type) {
            this(type, null);
        }

        private Event(EventType type, Object arg) {
            this.type = type;
            this.arg = arg;
        }
    }

    private class MySimpleClientListener implements SimpleClientListener {

        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication("johndoe", "secret".toCharArray());
        }

        public void loggedIn() {
            events.add(new Event(LOGGED_IN));
        }

        public void loginFailed(String reason) {
            events.add(new Event(LOGIN_FAILED, reason));
        }

        public ClientChannelListener joinedChannel(ClientChannel channel) {
            events.add(new Event(JOINED_CHANNEL, channel));
            return null;
        }

        public void receivedMessage(ByteBuffer message) {
            byte[] bytes = new byte[message.remaining()];
            message.get(bytes);
            events.add(new Event(RECEIVED_MESSAGE, new String(bytes, MESSAGE_CHARSET)));
        }

        public void reconnecting() {
            events.add(new Event(RECONNECTING));
        }

        public void reconnected() {
            events.add(new Event(RECONNECTED));
        }

        public void disconnected(boolean graceful, String reason) {
            events.add(new Event(DISCONNECTED, reason));
        }
    }
}
