/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.variables.IVariableHolder;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer.PositionCoordinateSerializer;

/**
 * @author mmarzec
 *
 */
public class TableHandlersSupport {

    private TableHandlersSupport() {
    }
    
    public static PositionCoordinateSerializer[] createSerializablePositionsCoordinates(
            final PositionCoordinate[] selectedCellPositions) {
        final PositionCoordinateSerializer[] serializablePositions = new PositionCoordinateSerializer[selectedCellPositions.length];
        for (int i = 0; i < selectedCellPositions.length; i++) {
            serializablePositions[i] = new PositionCoordinateSerializer(selectedCellPositions[i]);
        }
        return serializablePositions;
    }
    
    public static RobotSetting[] createSettingsCopy(final List<RobotSetting> settings) {
        final List<RobotSetting> settingsCopy = new ArrayList<>();
        for (final RobotSetting robotSetting : settings) {
            if (robotSetting.getName() != null && robotSetting.getArguments() != null
                    && robotSetting.getComment() != null) {
                settingsCopy.add(new RobotSetting(null, robotSetting.getGroup(), new String(robotSetting.getName()),
                        new ArrayList<>(robotSetting.getArguments()), new String(robotSetting.getComment())));
            }
        }
        return settingsCopy.toArray(new RobotSetting[0]);
    }
    
    public static RobotVariable[] createVariablesCopy(final List<RobotVariable> variables) {
        final List<RobotVariable> variablesCopy = new ArrayList<>();
        for (final RobotVariable robotVariable : variables) {
            final IVariableHolder variableHolderCopy = robotVariable.getLinkedElement().copy();
            if (variableHolderCopy != null) {
                variablesCopy.add(new RobotVariable(null, variableHolderCopy));
            }
        }
        return variablesCopy.toArray(new RobotVariable[0]);
    }
    
    @SuppressWarnings("unchecked")
    public static List<RobotKeywordCall>  createKeywordCallsCopy(final List<RobotKeywordCall> keywordCalls) {
        final List<RobotKeywordCall> keywordCallsCopy = new ArrayList<>();
        for (final RobotKeywordCall keywordCall : keywordCalls) {
            final RobotKeywordCall keywordCallCopy = new RobotKeywordCall(null, new String(keywordCall.getName()),
                    new ArrayList<>(keywordCall.getArguments()), new String(keywordCall.getComment()));
            if (keywordCall.getLinkedElement().getModelType() == ModelType.USER_KEYWORD_EXECUTABLE_ROW) {
                final RobotExecutableRow<UserKeyword> robotExecutableRow = ((RobotExecutableRow<UserKeyword>) keywordCall
                        .getLinkedElement()).copy();
                keywordCallCopy.link(robotExecutableRow);
            }
            keywordCallsCopy.add(keywordCallCopy);
        }
        return keywordCallsCopy;
    }
    
    public static List<RobotKeywordDefinition> createKeywordDefsCopy(final List<RobotKeywordDefinition> keywordDefs) {
        final List<RobotKeywordDefinition> keywordDefsCopy = new ArrayList<>();
        for (final RobotKeywordDefinition keywordDef : keywordDefs) {
            final RobotKeywordDefinition newDef = new RobotKeywordDefinition(null, new String(keywordDef.getName()),
                    new String(keywordDef.getComment()));
            final RobotDefinitionSetting argumentsSetting = keywordDef.getArgumentsSetting();
            if (argumentsSetting != null) {
                newDef.createDefinitionSetting(new String(argumentsSetting.getName()),
                        new ArrayList<>(argumentsSetting.getArguments()), new String(argumentsSetting.getComment()));
            }
            keywordDefsCopy.add(newDef);
        }
        return keywordDefsCopy;
    }

    public static int findNextSelectedElementRowIndex(final int initialIndex, final SelectionLayer selectionLayer) {
        final PositionCoordinate[] selectedCellPositions = selectionLayer.getSelectedCellPositions();
        for (int i = 0; i < selectedCellPositions.length; i++) {
            if(selectedCellPositions[i].rowPosition > initialIndex) {
                return selectedCellPositions[i].rowPosition;
            }
        }
        return initialIndex;
    }
    
    public static List<Integer> findSelectedColumnsIndexesByRowIndex(final int selectedElementRowIndex,
            final SelectionLayer selectionLayer) {

        final List<Integer> columnsIndexes = new ArrayList<>();
        final PositionCoordinate[] selectedCellPositions = selectionLayer.getSelectedCellPositions();
        for (int i = 0; i < selectedCellPositions.length; i++) {
            if (selectedCellPositions[i].rowPosition == selectedElementRowIndex) {
                columnsIndexes.add(selectedCellPositions[i].columnPosition);
            }
        }
        return columnsIndexes;
    }
}
