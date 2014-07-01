// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.eventizers.dynamic.DynamicEventizer;
import fi.jumi.actors.queue.MessageSender;
import fi.jumi.core.ipc.buffer.IpcBuffer;
import fi.jumi.core.util.*;
import org.junit.Test;

import java.lang.reflect.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public abstract class EncodingContract<T> {

    protected final Class<T> interfaceType;
    private final IpcProtocol.EncodingFactory<T> encodingFactory;

    public EncodingContract(IpcProtocol.EncodingFactory<T> encodingFactory) {
        this.interfaceType = getMyTypeArgument(0);
        this.encodingFactory = encodingFactory;
    }

    @SuppressWarnings("unchecked")
    private Class<T> getMyTypeArgument(int index) {
        ParameterizedType superclass = (ParameterizedType) getClass().getGenericSuperclass();
        assertThat(superclass.getRawType(), is((Type) EncodingContract.class));
        return (Class<T>) superclass.getActualTypeArguments()[index];
    }

    @Test
    public void encodes_and_decodes_all_events() throws Exception {
        SpyListener<T> spy = new SpyListener<>(interfaceType);
        exampleUsage(spy.getListener());
        spy.replay();
        IpcBuffer buffer = TestUtil.newIpcBuffer();

        // encode
        IpcProtocol<T> protocol = newIpcProtocol(buffer);
        protocol.start();
        exampleUsage(sendTo(protocol));
        protocol.close();

        // decode
        buffer.position(0);
        TestUtil.decodeAll(protocol, spy.getListener());

        spy.verify();
    }

    @Test
    public void example_usage_invokes_every_method_in_the_interface() throws Exception {
        MethodInvocationSpy<T> spy = new MethodInvocationSpy<>(new DynamicEventizer<>(interfaceType));

        exampleUsage(spy.getProxy());

        for (Method method : interfaceType.getMethods()) {
            assertThat("invoked methods", spy.methodInvocations.keySet(), hasItem(method));
        }
    }

    protected abstract void exampleUsage(T listener) throws Exception;

    private IpcProtocol<T> newIpcProtocol(IpcBuffer buffer) {
        return new IpcProtocol<>(buffer, encodingFactory);
    }

    private T sendTo(MessageSender<Event<T>> target) {
        return new DynamicEventizer<>(interfaceType).newFrontend(target);
    }
}
