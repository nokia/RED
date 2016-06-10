/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd;

import static com.google.common.collect.Sets.newHashSet;

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
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;

public class KeywordCallsTransfer extends ByteArrayTransfer {

    public static final String TYPE_PREFIX = "red-keywords-calls-data-transfer-format";

    private static final KeywordCallsTransfer INSTANCE = new KeywordCallsTransfer();

    private static final String TYPE_NAME = TYPE_PREFIX + ":" + System.currentTimeMillis() + ":" + INSTANCE.hashCode();

    private static final int TYPE_ID = registerType(TYPE_NAME);

    public static KeywordCallsTransfer getInstance() {
        return INSTANCE;
    }

    public static boolean hasKeywordCalls(final Clipboard clipboard) {
        return clipboard != null && !clipboard.isDisposed() && clipboardContainKeywordCalls(clipboard)
                && hasKeywordCallsOnly(clipboard.getContents(getInstance()));
    }
    
    public static boolean hasSettings(final Clipboard clipboard) {
        return clipboard != null && !clipboard.isDisposed() && clipboardContainKeywordCalls(clipboard)
                && hasAllGroupSettingsOnly(clipboard.getContents(getInstance()));
    }

    public static boolean hasGeneralSettings(final Clipboard clipboard) {
        return clipboard != null && !clipboard.isDisposed() && clipboardContainKeywordCalls(clipboard)
                && hasSettingsOnly(clipboard.getContents(getInstance()), SettingsGroup.NO_GROUP);
    }

    public static boolean hasMetadataSettings(final Clipboard clipboard) {
        return clipboard != null && !clipboard.isDisposed() && clipboardContainKeywordCalls(clipboard)
                && hasSettingsOnly(clipboard.getContents(getInstance()), SettingsGroup.METADATA);
    }

    public static boolean hasImportSettings(final Clipboard clipboard) {
        return clipboard != null && !clipboard.isDisposed() && clipboardContainKeywordCalls(clipboard)
                && hasSettingsOnly(clipboard.getContents(getInstance()), SettingsGroup.LIBRARIES,
                        SettingsGroup.RESOURCES, SettingsGroup.VARIABLES);
    }

    private static boolean hasSettingsOnly(final Object content, final SettingsGroup... groups) {
        if (content instanceof Object[]) {
            for (final Object item : ((Object[])content)) {
                if (item.getClass() != RobotSetting.class
                        || !newHashSet(groups).contains(((RobotSetting) item).getGroup())) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private static boolean hasAllGroupSettingsOnly(final Object content) {
        if (content instanceof Object[]) {
            for (final Object item : ((Object[]) content)) {
                if (item.getClass() != RobotSetting.class) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean hasKeywordCallsOnly(final Object content) {
        if (content instanceof Object[]) {
            for (final Object item : ((Object[]) content)) {
                if (item.getClass() != RobotKeywordCall.class) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean clipboardContainKeywordCalls(final Clipboard clipboard) {
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
        if (!(data instanceof RobotKeywordCall[])) {
            return;
        }

        final RobotKeywordCall[] objects = (RobotKeywordCall[]) data;

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
    protected RobotKeywordCall[] nativeToJava(final TransferData transferData) { // NOPMD
        final byte[] bytes = (byte[]) super.nativeToJava(transferData);
        if (bytes == null) {
            return new RobotKeywordCall[0];
        }
        try (final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            final int count = in.readInt();
            final RobotKeywordCall[] objects = new RobotKeywordCall[count];
            for (int i = 0; i < count; i++) {
                objects[i] = (RobotKeywordCall) in.readObject();
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
