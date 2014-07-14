// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc.encoding;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.eventizers.dynamic.DynamicEventizer;
import fi.jumi.actors.queue.MessageSender;
import fi.jumi.core.ipc.TestUtil;
import fi.jumi.core.ipc.buffer.IpcBuffer;
import fi.jumi.core.ipc.channel.*;
import fi.jumi.core.util.*;
import org.junit.Test;

import java.lang.reflect.*;
import java.util.*;

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
        IpcReaders.decodeAll(protocol, spy.getListener());

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

    private static final Map<Class<?>, MessageEncoding<?>> encodingsByType = new HashMap<>();

    @Test
    public void the_interface_name_and_version_combination_is_unique() {
        MessageEncoding<T> actual = encodingFactory.create(null);

        for (MessageEncoding<?> other : encodingsByType.values()) {
            if (actual.getInterfaceName().equals(other.getInterfaceName()) &&
                    actual.getInterfaceVersion() == other.getInterfaceVersion()) {
                throw new AssertionError(actual.getClass().getName()
                        + " had the same interface name and version as " + other.getClass().getName()
                        + ": " + actual.getInterfaceName() + " v" + actual.getInterfaceVersion());
            }
        }

        encodingsByType.put(actual.getClass(), actual);
    }

    /**
     * NOTE: It might be desirable for backward compatibility reasons to loosen this restriction in the future, because
     * otherwise we will not be able to rename the Java classes without breaking backward compatibility.
     */
    @Test
    public void the_interface_name_is_in_sync_with_the_actual_Java_interface() throws ClassNotFoundException {
        MessageEncoding<T> encoding = encodingFactory.create(null);

        String interfaceName = encoding.getInterfaceName();

        assertThat(encoding, is(instanceOf(Class.forName(interfaceName))));
    }

    @Test
    public void interface_version_starts_from_1() {
        MessageEncoding<T> encoding = encodingFactory.create(null);

        int interfaceVersion = encoding.getInterfaceVersion();

        assertThat(interfaceVersion, is(greaterThanOrEqualTo(1)));
    }

    private IpcProtocol<T> newIpcProtocol(IpcBuffer buffer) {
        return new IpcProtocol<>(buffer, encodingFactory);
    }

    private T sendTo(MessageSender<Event<T>> target) {
        return new DynamicEventizer<>(interfaceType).newFrontend(target);
    }
}
