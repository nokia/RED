/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotEmptyLine;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer.PositionCoordinateSerializer;

public class RedClipboard {

    private static Map<Class<?>, Transfer> transfers = new HashMap<>();
    static {
        transfers.put(String.class, TextTransfer.getInstance());
        transfers.put(PositionCoordinateSerializer[].class, PositionCoordinateTransfer.getInstance());
        transfers.put(RobotCase[].class, CasesTransfer.getInstance());
        transfers.put(RobotKeywordDefinition[].class, KeywordDefinitionsTransfer.getInstance());
        transfers.put(RobotKeywordCall[].class, KeywordCallsTransfer.getInstance());
        transfers.put(RobotSetting[].class, KeywordCallsTransfer.getInstance());
        transfers.put(RobotVariable[].class, VariablesTransfer.getInstance());
    }

    private final Clipboard clipboard;

    protected RedClipboard() {
        clipboard = null;
    }

    public RedClipboard(final Display display) {
        clipboard = new Clipboard(display);
    }

    private boolean isValid() {
        return clipboard != null && !clipboard.isDisposed();
    }

    public boolean hasPositionsCoordinates() {
        return isValid() && clipboardContainSupportedData(PositionCoordinateTransfer.getInstance());
    }

    public PositionCoordinateSerializer[] getPositionsCoordinates() {
        final Object probablyPositions = clipboard.getContents(PositionCoordinateTransfer.getInstance());
        return probablyPositions instanceof PositionCoordinateSerializer[]
                ? (PositionCoordinateSerializer[]) probablyPositions
                : null;
    }

    public boolean hasVariables() {
        return isValid() && clipboardContainSupportedData(VariablesTransfer.getInstance());
    }

    public RobotVariable[] getVariables() {
        final Object probablyVariables = clipboard.getContents(VariablesTransfer.getInstance());
        return probablyVariables instanceof RobotVariable[] ? (RobotVariable[]) probablyVariables : null;
    }

    public boolean hasKeywordDefinitions() {
        return isValid() && clipboardContainSupportedData(KeywordDefinitionsTransfer.getInstance());
    }

    public RobotKeywordDefinition[] getKeywordDefinitions() {
        final Object probablyDefinitions = clipboard.getContents(KeywordDefinitionsTransfer.getInstance());
        return probablyDefinitions instanceof RobotKeywordDefinition[] ? (RobotKeywordDefinition[]) probablyDefinitions
                : null;
    }

    public boolean hasCases() {
        return isValid() && clipboardContainSupportedData(CasesTransfer.getInstance());
    }

    public RobotCase[] getCases() {
        final Object probablyCases = clipboard.getContents(CasesTransfer.getInstance());
        return probablyCases instanceof RobotCase[] ? (RobotCase[]) probablyCases : null;
    }

    public boolean hasKeywordCalls() {
        return isValid() && clipboardContainSupportedData(KeywordCallsTransfer.getInstance())
                && hasKeywordCallsOnly(getKeywordCalls());
    }

    public RobotKeywordCall[] getKeywordCalls() {
        final Object probablyCalls = clipboard.getContents(KeywordCallsTransfer.getInstance());
        return probablyCalls instanceof RobotKeywordCall[] ? (RobotKeywordCall[]) probablyCalls : null;
    }

    public boolean hasSettings() {
        return isValid() && clipboardContainSupportedData(KeywordCallsTransfer.getInstance())
                && hasSettingsOnly(getKeywordCalls(), EnumSet.allOf(SettingsGroup.class));
    }

    public boolean hasGeneralSettings() {
        return isValid() && clipboardContainSupportedData(KeywordCallsTransfer.getInstance())
                && hasSettingsOnly(getKeywordCalls(), EnumSet.of(SettingsGroup.NO_GROUP));
    }

    public boolean hasMetadataSettings() {
        return isValid() && clipboardContainSupportedData(KeywordCallsTransfer.getInstance())
                && hasSettingsOnly(getKeywordCalls(), EnumSet.of(SettingsGroup.METADATA));
    }

    public boolean hasImportSettings() {
        return isValid() && clipboardContainSupportedData(KeywordCallsTransfer.getInstance())
                && hasSettingsOnly(getKeywordCalls(), SettingsGroup.getImportsGroupsSet());
    }

    protected final boolean hasSettingsOnly(final RobotKeywordCall[] content, final EnumSet<SettingsGroup> groups) {
        if (content != null) {
            for (final Object item : content) {
                if (item.getClass() != RobotSetting.class || !groups.contains(((RobotSetting) item).getGroup())) {
                    return false;
                }
            }
        }
        return true;
    }

    protected final boolean hasKeywordCallsOnly(final RobotKeywordCall[] content) {
        if (content != null) {
            for (final Object item : content) {
                if (item.getClass() != RobotKeywordCall.class && item.getClass() != RobotDefinitionSetting.class
                        && item.getClass() != RobotEmptyLine.class) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean hasText() {
        return isValid() && clipboardContainSupportedData(TextTransfer.getInstance());
    }

    public String getText() {
        final Object probablyText = clipboard.getContents(TextTransfer.getInstance());
        return probablyText instanceof String ? (String) probablyText : null;
    }

    private boolean clipboardContainSupportedData(final Transfer transfer) {
        final TransferData[] availableTypes = clipboard.getAvailableTypes();
        for (final TransferData data : availableTypes) {
            if (transfer.isSupportedType(data)) {
                return true;
            }
        }
        return false;
    }

    public RedClipboard insertContent(final Object... dataToInsert) {
        final Object[] data = new Object[dataToInsert.length];
        final Transfer[] dataTypes = new Transfer[dataToInsert.length];

        for (int i = 0; i < data.length; i++) {
            data[i] = dataToInsert[i];
            dataTypes[i] = transfers.get(dataToInsert[i].getClass());
            if (dataTypes[i] == null) {
                throw new IllegalStateException(
                        "There is no transfer registered for " + dataToInsert.getClass().getName() + " class");
            }
        }
        clipboard.setContents(data, dataTypes);
        return this;
    }

    public void clear() {
        if (!clipboard.isDisposed()) {
            clipboard.clearContents();
        }
    }

    public void dispose() {
        clipboard.dispose();
    }
}
