/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;

public class ArraysSerializerDeserializer {

    @SuppressWarnings("unchecked")
    public static <T> T[] copy(final Class<? extends T> clazz, final T[] toCopy) {
        if (toCopy.length == 0) {
            return (T[]) Array.newInstance(clazz, 0);
        }
        try {
            return deserialize(clazz, serialize(toCopy));
        } catch (ClassNotFoundException | IOException e) {
            throw new IllegalStateException("Unable to copy array", e);
        }
    }

    public static <T> byte[] serialize(final T[] toSerialize) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream stream = new ObjectOutputStream(bos)) {

            stream.writeInt(toSerialize.length);
            for (int i = 0; i < toSerialize.length; i++) {
                stream.writeObject(toSerialize[i]);
            }
            return bos.toByteArray();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] deserialize(final Class<T> clazz, final byte[] toDeserialize)
            throws ClassNotFoundException, IOException {
        if (toDeserialize == null) {
            return (T[]) Array.newInstance(clazz, 0);
        }
        try (ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(toDeserialize))) {

            final int count = stream.readInt();
            final T[] objects = (T[]) Array.newInstance(clazz, count);
            for (int i = 0; i < count; i++) {
                objects[i] = clazz.cast(stream.readObject());
            }
            return objects;
        }
    }
}
