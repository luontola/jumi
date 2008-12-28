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

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.db.Blob;

import javax.annotation.concurrent.Immutable;
import java.io.*;

/**
 * @author Esko Luontola
 * @since 1.9.2008
 */
@Immutable
public class ObjectSerializerImpl implements ObjectSerializer {

    private final SerializationListener[] listeners;
    private final SerializationReplacer[] replacers;

    public ObjectSerializerImpl() {
        this(new SerializationListener[0], new SerializationReplacer[0]);
    }

    @Inject
    public ObjectSerializerImpl(SerializationListener[] listeners, SerializationReplacer[] replacers) {
        this.listeners = listeners;
        this.replacers = replacers;
    }

    public SerializationResult serialize(Object obj) {
        ByteArrayOutputStream serialized = new ByteArrayOutputStream();
        MetadataBuilderImpl meta = new MetadataBuilderImpl();
        serializeToStream(serialized, obj, meta);
        return new SerializationResult(Blob.fromBytes(serialized.toByteArray()), meta.getMetadata());
    }

    public DeserializationResult deserialize(Blob serialized) {
        MetadataBuilderImpl meta = new MetadataBuilderImpl();
        Object deserialized = deserializeFromStream(serialized.getInputStream(), meta);
        return new DeserializationResult(deserialized, meta.getMetadata());
    }

    private void serializeToStream(OutputStream target, Object obj, MetadataBuilder meta) {
        try {
            ObjectOutputStream out = new MyObjectOutputStream(target, obj, meta);
            out.writeObject(obj);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Object deserializeFromStream(InputStream source, MetadataBuilder meta) {
        try {
            ObjectInputStream in = new MyObjectInputStream(source, meta);
            Object obj = in.readObject();
            in.close();
            return obj;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    private class MyObjectOutputStream extends ObjectOutputStream {
        private final Object rootObject;
        private final MetadataBuilder meta;

        public MyObjectOutputStream(OutputStream out, Object rootObject, MetadataBuilder meta) throws IOException {
            super(out);
            this.rootObject = rootObject;
            this.meta = meta;
            enableReplaceObject(true);
        }

        protected Object replaceObject(Object obj) throws IOException {
            for (SerializationListener listener : listeners) {
                listener.beforeReplace(rootObject, obj, meta);
            }
            for (SerializationReplacer replacer : replacers) {
                obj = replacer.replaceSerialized(rootObject, obj, meta);
            }
            for (SerializationListener listener : listeners) {
                listener.beforeSerialize(rootObject, obj, meta);
            }
            return obj;
        }
    }

    private class MyObjectInputStream extends ObjectInputStream {
        private final MetadataBuilder meta;

        public MyObjectInputStream(InputStream in, MetadataBuilder meta) throws IOException {
            super(in);
            this.meta = meta;
            enableResolveObject(true);
        }

        protected Object resolveObject(Object obj) throws IOException {
            for (SerializationListener listener : listeners) {
                listener.afterDeserialize(obj, meta);
            }
            for (SerializationReplacer replacer : replacers) {
                obj = replacer.resolveDeserialized(obj, meta);
            }
            for (SerializationListener listener : listeners) {
                listener.afterResolve(obj, meta);
            }
            return obj;
        }
    }
}
