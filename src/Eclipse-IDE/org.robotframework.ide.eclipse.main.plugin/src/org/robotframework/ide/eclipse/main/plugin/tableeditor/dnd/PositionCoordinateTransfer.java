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
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer.PositionCoordinateSerializer;

public class PositionCoordinateTransfer extends RedTransfer<PositionCoordinateSerializer> {

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
        return data instanceof PositionCoordinateSerializer[];
    }

    @Override
    protected byte[] javaToBytes(final Object data) throws IOException {
        return ArraysSerializerDeserializer.serialize((PositionCoordinateSerializer[]) data);
    }

    @Override
    protected PositionCoordinateSerializer[] bytesToJava(final byte[] bytes)
            throws ClassNotFoundException, IOException {
        return ArraysSerializerDeserializer.deserialize(PositionCoordinateSerializer.class, bytes);
    }

    public static class PositionCoordinateSerializer implements Serializable {

        public static PositionCoordinateSerializer[] createFrom(final PositionCoordinate[] coordinates) {
            final PositionCoordinateSerializer[] serializableCoordinates = new PositionCoordinateSerializer[coordinates.length];
            for (int i = 0; i < coordinates.length; i++) {
                serializableCoordinates[i] = new PositionCoordinateSerializer(coordinates[i]);
            }
            return serializableCoordinates;
        }

        private static final long serialVersionUID = 1L;

        private final int columnPosition;

        private final int rowPosition;

        public PositionCoordinateSerializer(final PositionCoordinate positionCoordinate) {
            this.columnPosition = positionCoordinate.columnPosition;
            this.rowPosition = positionCoordinate.rowPosition;
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
                final PositionCoordinateSerializer other = (PositionCoordinateSerializer) obj;
                return Objects.equals(rowPosition, other.rowPosition) && Objects.equals(columnPosition, other.columnPosition);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(rowPosition, columnPosition);
        }
    }
}
