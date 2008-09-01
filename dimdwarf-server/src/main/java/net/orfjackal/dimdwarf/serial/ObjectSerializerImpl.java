/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 *     * Neither the name of the copyright holder nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.orfjackal.dimdwarf.serial;

import net.orfjackal.dimdwarf.db.Blob;

import javax.swing.event.EventListenerList;
import java.io.*;

/**
 * This class is NOT thread-safe.
 *
 * @author Esko Luontola
 * @since 1.9.2008
 */
@SuppressWarnings({"ForLoopReplaceableByForEach"})
public class ObjectSerializerImpl implements ObjectSerializer {

    private final EventListenerList listeners = new EventListenerList();

    public void addSerializationListener(SerializationListener listener) {
        listeners.add(SerializationListener.class, listener);
    }

    public void addSerializationReplacer(SerializationReplacer replacer) {
        listeners.add(SerializationReplacer.class, replacer);
    }

    public Blob serialize(Object obj) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        serializeToStream(bytes, obj);
        return Blob.fromBytes(bytes.toByteArray());
    }

    public Object deserialize(Blob serialized) {
        return deserializeFromStream(serialized.getInputStream());
    }

    private void serializeToStream(OutputStream target, Object obj) {
        try {
            ObjectOutputStream out = new MyObjectOutputStream(target, obj,
                    listeners.getListeners(SerializationListener.class),
                    listeners.getListeners(SerializationReplacer.class));
            out.writeObject(obj);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Object deserializeFromStream(InputStream source) {
        try {
            ObjectInputStream out = new MyObjectInputStream(source,
                    listeners.getListeners(SerializationListener.class),
                    listeners.getListeners(SerializationReplacer.class));
            Object obj = out.readObject();
            out.close();
            return obj;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    private static class MyObjectOutputStream extends ObjectOutputStream {
        private final Object rootObject;
        private final SerializationListener[] listeners;
        private final SerializationReplacer[] replacers;

        public MyObjectOutputStream(OutputStream out, Object rootObject,
                                    SerializationListener[] listeners,
                                    SerializationReplacer[] replacers) throws IOException {
            super(out);
            this.rootObject = rootObject;
            this.listeners = listeners;
            this.replacers = replacers;
            enableReplaceObject(true);
        }

        protected Object replaceObject(Object obj) throws IOException {
            for (int i = 0; i < listeners.length; i++) {
                listeners[i].beforeSerialized(rootObject, obj);
            }
            for (int i = 0; i < replacers.length; i++) {
                // TODO: replacement should be done before listeners, because otherwise entities
                // can not be proxied before check for direct entity reference will fail serialization
                // -> write a test when doing transparent references (also change order in SerializationAdapter)
                obj = replacers[i].replaceSerialized(rootObject, obj);
            }
            return obj;
        }
    }

    private static class MyObjectInputStream extends ObjectInputStream {
        private final SerializationListener[] listeners;
        private final SerializationReplacer[] replacers;

        public MyObjectInputStream(InputStream in,
                                   SerializationListener[] listeners,
                                   SerializationReplacer[] replacers) throws IOException {
            super(in);
            this.listeners = listeners;
            this.replacers = replacers;
            enableResolveObject(true);
        }

        protected Object resolveObject(Object obj) throws IOException {
            for (int i = 0; i < replacers.length; i++) {
                obj = replacers[i].resolveDeserialized(obj);
            }
            for (int i = 0; i < listeners.length; i++) {
                listeners[i].afterDeserialized(obj);
            }
            return obj;
        }
    }
}
