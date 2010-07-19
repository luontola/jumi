// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.serial;

import net.orfjackal.dimdwarf.db.Blob;

import javax.annotation.concurrent.Immutable;
import java.io.*;

@Immutable
public class ObjectSerializer {

    // TODO: Make ObjectSerializer an interface, rename impl to JavaSerializationObjectSerializer?

    public Blob serialize(Object obj, SerializationFilter filter) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        serializeToStream(result, obj, filter);
        return Blob.fromBytes(result.toByteArray());
    }

    public Object deserialize(Blob serialized, SerializationFilter filter) {
        return deserializeFromStream(serialized.getInputStream(), filter);
    }

    private void serializeToStream(OutputStream target, Object obj, SerializationFilter filter) {
        try {
            ObjectOutputStream out = new MyObjectOutputStream(target, obj, filter);
            out.writeObject(obj);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Object deserializeFromStream(InputStream source, SerializationFilter filter) {
        try {
            ObjectInputStream in = new MyObjectInputStream(source, filter);
            Object obj = in.readObject();
            in.close();
            return obj;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    private static class MyObjectOutputStream extends ObjectOutputStream {
        private final Object rootObject;
        private final SerializationFilter filter;

        public MyObjectOutputStream(OutputStream out, Object rootObject, SerializationFilter filter) throws IOException {
            super(out);
            this.rootObject = rootObject;
            this.filter = filter;
            enableReplaceObject(true);
        }

        protected Object replaceObject(Object obj) throws IOException {
            return filter.replaceSerialized(rootObject, obj);
        }
    }

    private static class MyObjectInputStream extends ObjectInputStream {
        private final SerializationFilter filter;

        public MyObjectInputStream(InputStream in, SerializationFilter filter) throws IOException {
            super(in);
            this.filter = filter;
            enableResolveObject(true);
        }

        protected Object resolveObject(Object obj) throws IOException {
            return filter.resolveDeserialized(obj);
        }
    }
}
