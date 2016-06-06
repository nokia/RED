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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.statushandlers.StatusManager;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;

public class CasesTransfer extends ByteArrayTransfer {

    public static final String TYPE_PREFIX = "red-cases-data-transfer-format";

    private static final CasesTransfer INSTANCE = new CasesTransfer();

    private static final String TYPE_NAME = TYPE_PREFIX + ":" + System.currentTimeMillis() + ":" + INSTANCE.hashCode();

    private static final int TYPE_ID = registerType(TYPE_NAME);

    public static CasesTransfer getInstance() {
        return INSTANCE;
    }

    public static boolean hasCases(final Clipboard clipboard) {
        return clipboard != null && !clipboard.isDisposed() && clipboardContainCases(clipboard);
    }

    private static boolean clipboardContainCases(final Clipboard clipboard) {
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
        if (!(data instanceof RobotCase[])) {
            return;
        }

        final RobotCase[] objects = (RobotCase[]) data;

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
    protected RobotCase[] nativeToJava(final TransferData transferData) { // NOPMD
        final byte[] bytes = (byte[]) super.nativeToJava(transferData);
        if (bytes == null) {
            return new RobotCase[0];
        }

        try (final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            final int count = in.readInt();
            final RobotCase[] objects = new RobotCase[count];
            for (int i = 0; i < count; i++) {
                objects[i] = (RobotCase) in.readObject();
                objects[i].fixParents();
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

}
