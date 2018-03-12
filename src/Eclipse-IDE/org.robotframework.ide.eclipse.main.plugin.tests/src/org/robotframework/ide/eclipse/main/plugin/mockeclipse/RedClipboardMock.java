/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.mockeclipse;

import java.util.EnumSet;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer.PositionCoordinateSerializer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;

public class RedClipboardMock extends RedClipboard {

    private String text;
    private RobotCase[] cases;
    private RobotKeywordDefinition[] keywordDefinitions;
    private RobotKeywordCall[] calls;
    private RobotVariable[] variables;
    private PositionCoordinateSerializer[] positions;

    public RedClipboardMock() {
        super();
    }

    public boolean isEmpty() {
        return !hasText() && !hasCases() && !hasKeywordDefinitions() && !hasKeywordCalls() && !hasVariables()
                && !hasPositionsCoordinates();
    }

    @Override
    public boolean hasPositionsCoordinates() {
        return positions != null;
    }

    @Override
    public PositionCoordinateSerializer[] getPositionsCoordinates() {
        return positions;
    }

    @Override
    public boolean hasVariables() {
        return variables != null;
    }

    @Override
    public RobotVariable[] getVariables() {
        return variables;
    }

    @Override
    public boolean hasKeywordDefinitions() {
        return keywordDefinitions != null;
    }

    @Override
    public RobotKeywordDefinition[] getKeywordDefinitions() {
        return keywordDefinitions;
    }

    @Override
    public boolean hasCases() {
        return cases != null;
    }

    @Override
    public RobotCase[] getCases() {
        return cases;
    }

    @Override
    public boolean hasKeywordCalls() {
        return calls != null && hasKeywordCallsOnly(calls);
    }

    @Override
    public RobotKeywordCall[] getKeywordCalls() {
        return calls;
    }

    @Override
    public boolean hasSettings() {
        return calls != null && hasSettingsOnly(calls, EnumSet.allOf(SettingsGroup.class));
    }

    @Override
    public boolean hasGeneralSettings() {
        return calls != null && hasSettingsOnly(calls, EnumSet.of(SettingsGroup.NO_GROUP));
    }

    @Override
    public boolean hasMetadataSettings() {
        return calls != null && hasSettingsOnly(calls, EnumSet.of(SettingsGroup.METADATA));
    }

    @Override
    public boolean hasImportSettings() {
        return calls != null && hasSettingsOnly(calls, SettingsGroup.getImportsGroupsSet());
    }

    @Override
    public boolean hasText() {
        return text != null;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public RedClipboard insertContent(final Object... dataToInsert) {
        text = null;
        cases = null;
        keywordDefinitions = null;
        calls = null;
        variables = null;
        positions = null;

        for (final Object data : dataToInsert) {
            if (data instanceof String) {
                text = (String) data;
            } else if (data instanceof RobotCase[]) {
                cases = (RobotCase[]) data;
            } else if (data instanceof RobotKeywordDefinition[]) {
                keywordDefinitions = (RobotKeywordDefinition[]) data;
            } else if (data instanceof RobotKeywordCall[]) {
                calls = (RobotKeywordCall[]) data;
            } else if (data instanceof RobotVariable[]) {
                variables = (RobotVariable[]) data;
            } else if (data instanceof PositionCoordinateSerializer[]) {
                positions = (PositionCoordinateSerializer[]) data;
            }
        }
        return this;
    }

    @Override
    public void clear() {
        text = null;
        cases = null;
        keywordDefinitions = null;
        calls = null;
        variables = null;
        positions = null;
    }
}
