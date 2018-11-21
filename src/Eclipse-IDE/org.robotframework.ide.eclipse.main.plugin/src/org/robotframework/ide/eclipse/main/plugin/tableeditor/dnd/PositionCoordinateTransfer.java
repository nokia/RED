/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer.SerializablePositionCoordinate;

public class PositionCoordinateTransfer extends RedTransfer<SerializablePositionCoordinate> {

    private static final String TYPE_NAME = "red-position-coordinate-data-transfer-format";
    private static final PositionCoordinateTransfer INSTANCE = new PositionCoordinateTransfer(TYPE_NAME);

    public PositionCoordinateTransfer(final String typeName) {
        super(typeName);
    }

    public static PositionCoordinateTransfer getInstance() {
        return INSTANCE;
    }

    @Override
    protected boolean canHandleSerialization(final Object data) {
        return data instanceof SerializablePositionCoordinate[];
    }

    @Override
    protected byte[] javaToBytes(final Object data) throws IOException {
        return ArraysSerializerDeserializer.serialize((SerializablePositionCoordinate[]) data);
    }

    @Override
    protected SerializablePositionCoordinate[] bytesToJava(final byte[] bytes)
            throws ClassNotFoundException, IOException {
        return ArraysSerializerDeserializer.deserialize(SerializablePositionCoordinate.class, bytes);
    }

    public static class SerializablePositionCoordinate implements Serializable {

        public static SerializablePositionCoordinate[] createFrom(final PositionCoordinate[] coordinates) {
            final SerializablePositionCoordinate[] serializableCoordinates = new SerializablePositionCoordinate[coordinates.length];
            for (int i = 0; i < coordinates.length; i++) {
                serializableCoordinates[i] = new SerializablePositionCoordinate(coordinates[i]);
            }
            return serializableCoordinates;
        }

        private static final long serialVersionUID = 1L;

        private final int columnPosition;

        private final int rowPosition;

        public SerializablePositionCoordinate(final PositionCoordinate positionCoordinate) {
            this(positionCoordinate.columnPosition, positionCoordinate.rowPosition);
        }

        public SerializablePositionCoordinate(final int columnPosition, final int rowPosition) {
            this.columnPosition = columnPosition;
            this.rowPosition = rowPosition;
        }

        public int getColumnPosition() {
            return columnPosition;
        }

        public int getRowPosition() {
            return rowPosition;
        }
        
        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            } else if (obj.getClass() == getClass()) {
                final SerializablePositionCoordinate other = (SerializablePositionCoordinate) obj;
                return Objects.equals(rowPosition, other.rowPosition) && Objects.equals(columnPosition, other.columnPosition);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(rowPosition, columnPosition);
        }

        @Override
        public String toString() {
            // debugging or junit reports purposes only
            return "[" + columnPosition + ", " + rowPosition + "]";
        }
    }
}
