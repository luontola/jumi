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

package net.orfjackal.dimdwarf.entities;

import net.orfjackal.dimdwarf.api.Entity;
import net.orfjackal.dimdwarf.db.Blob;

import javax.swing.event.EventListenerList;
import java.io.*;

/**
 * This class is NOT thread-safe.
 *
 * @author Esko Luontola
 * @since 1.9.2008
 */
public class EntitySerializerImpl implements EntitySerializer {

    private final EventListenerList listeners = new EventListenerList();

    public void addSerializationListener(SerializationListener listener) {
        listeners.add(SerializationListener.class, listener);
    }

    public Blob serialize(Entity entity) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        serializeToStream(bytes, entity);
        return Blob.fromBytes(bytes.toByteArray());
    }

    public Entity deserialize(Blob serialized) {
        return (Entity) deserializeFromStream(serialized.getInputStream());
    }

    private void serializeToStream(OutputStream target, Entity entity) {
        try {
            ObjectOutputStream out = new MyObjectOutputStream(target, entity,
                    listeners.getListeners(SerializationListener.class));
            out.writeObject(entity);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Object deserializeFromStream(InputStream source) {
        try {
            ObjectInputStream out = new ObjectInputStream(source);
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

        private final Entity rootObject;
        private final SerializationListener[] listeners;

        public MyObjectOutputStream(OutputStream out, Entity rootObject, SerializationListener[] listeners) throws IOException {
            super(out);
            this.rootObject = rootObject;
            this.listeners = listeners;
            enableReplaceObject(true);
        }

        protected Object replaceObject(Object obj) throws IOException {
            for (SerializationListener listener : listeners) {
                listener.serialized(rootObject, obj);
            }
            return obj;
        }
    }
}
