/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd;

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.statushandlers.StatusManager;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;


public abstract class RedTransfer<T> extends ByteArrayTransfer {

    private final int typeId;

    private final String typeName;

    public RedTransfer(final String typeName) {
        this.typeId = registerType(typeName);
        this.typeName = typeName;
    }

    @Override
    protected int[] getTypeIds() {
        return new int[] { typeId };
    }

    @Override
    protected String[] getTypeNames() {
        return new String[] { typeName };
    }

    @Override
    protected void javaToNative(final Object data, final TransferData transferData) {
        try {
            if (canHandleSerialization(data)) {
                super.javaToNative(javaToBytes(data), transferData);
            }

        } catch (final IOException e) {
            StatusManager.getManager()
                    .handle(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID,
                            "Failed to convert from java to native. Reason: " + e.getMessage(), e),
                            StatusManager.LOG | StatusManager.BLOCK);
            throw new IllegalStateException(e);
        }
    }

    protected abstract boolean canHandleSerialization(final Object data);

    protected abstract byte[] javaToBytes(final Object data) throws IOException;

    @Override
    protected T[] nativeToJava(final TransferData transferData) { // NOPMD
        try {
            final byte[] bytes = (byte[]) super.nativeToJava(transferData);
            return bytesToJava(bytes);

        } catch (ClassNotFoundException | IOException e) {
            StatusManager.getManager().handle(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID,
                    "Failed to copy item data. Reason: " + e.getMessage(), e), StatusManager.LOG);
        }
        // it has to return null, as this is part of the contract for this method;
        // otherwise e.g. drag source will be notified that drag was finished successfully
        return null;
    }

    protected abstract T[] bytesToJava(final byte[] bytes) throws ClassNotFoundException, IOException;

}
