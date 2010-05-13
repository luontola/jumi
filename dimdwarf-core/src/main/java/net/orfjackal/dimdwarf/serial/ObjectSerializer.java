// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

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
public class ObjectSerializer {

    private final SerializationListener[] listeners;
    private final SerializationReplacer[] replacers;

    public ObjectSerializer() {
        this(new SerializationListener[0], new SerializationReplacer[0]);
    }

    @Inject
    public ObjectSerializer(SerializationListener[] listeners, SerializationReplacer[] replacers) {
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
