/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.statushandlers.StatusManager;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

public class PositionCoordinateTransfer extends ByteArrayTransfer {

    public static final String TYPE_PREFIX = "red-position-coordinate-data-transfer-format";

    private static final PositionCoordinateTransfer INSTANCE = new PositionCoordinateTransfer();

    private static final String TYPE_NAME = TYPE_PREFIX + ":" + System.currentTimeMillis() + ":" + INSTANCE.hashCode();

    private static final int TYPE_ID = registerType(TYPE_NAME);

    public static PositionCoordinateTransfer getInstance() {
        return INSTANCE;
    }

    public static boolean hasPositionsCoordinates(final Clipboard clipboard) {
        return clipboard != null && !clipboard.isDisposed() && clipboardContainPositionsCoordinates(clipboard);
    }

    private static boolean clipboardContainPositionsCoordinates(final Clipboard clipboard) {
        final TransferData[] availableTypes = clipboard.getAvailableTypes();
        for (final TransferData data : availableTypes) {
            if (getInstance().isSupportedType(data)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected int[] getTypeIds() {
        return new int[] { TYPE_ID };
    }

    @Override
    protected String[] getTypeNames() {
        return new String[] { TYPE_NAME };
    }

    @Override
    protected void javaToNative(final Object data, final TransferData transferData) {
        if (!(data instanceof PositionCoordinateSerializer[])) {
            return;
        }
        final PositionCoordinateSerializer[] objects = (PositionCoordinateSerializer[]) data;

        try (final ByteArrayOutputStream out = new ByteArrayOutputStream();
                final ObjectOutputStream objectOut = new ObjectOutputStream(out)) {

            objectOut.writeInt(objects.length);
            for (int i = 0; i < objects.length; i++) {
                objectOut.writeObject(objects[i]);
            }

            super.javaToNative(out.toByteArray(), transferData);
        } catch (final IOException e) {
            StatusManager.getManager().handle(
                    new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID,
                            "Failed to convert from java to native. Reason: " + e.getMessage(), e),
                    StatusManager.LOG | StatusManager.BLOCK);
            throw new IllegalStateException(e);
        }

    }

    @Override
    protected PositionCoordinateSerializer[] nativeToJava(final TransferData transferData) { // NOPMD
        final byte[] bytes = (byte[]) super.nativeToJava(transferData);
        if (bytes == null) {
            return new PositionCoordinateSerializer[0];
        }

        try (final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            final int count = in.readInt();
            final PositionCoordinateSerializer[] objects = new PositionCoordinateSerializer[count];
            for (int i = 0; i < count; i++) {
                objects[i] = (PositionCoordinateSerializer) in.readObject();
            }
            return objects;
        } catch (ClassNotFoundException | IOException e) {
            StatusManager.getManager().handle(
                    new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, "Failed to copy item data. Reason: "
                            + e.getMessage(), e), StatusManager.LOG);
        }
        // it has to return null, as this is part of the contract for this method;
        // otherwise e.g. drag source will be notified that drag was finished successfully
        return null;
    }

    public static class PositionCoordinateSerializer implements Serializable {

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
