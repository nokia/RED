/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ModelElementsSerDe {

    public static <T> T copy(final T toCopy) {
        return deserialize(serialize(toCopy));
    }

    @SuppressWarnings("unchecked")
    private static <T> T deserialize(final byte[] toDeserialize) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(toDeserialize);
                ObjectInputStream stream = new ObjectInputStream(bis)) {
            return (T) stream.readObject();
        } catch (final IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Should serialize", e);
        }
    }

    private static <T> byte[] serialize(final T toSerialize) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream stream = new ObjectOutputStream(bos)) {
            stream.writeObject(toSerialize);
            return bos.toByteArray();
        } catch (final IOException e) {
            throw new IllegalStateException("Should serialize", e);
        }
    }
}
