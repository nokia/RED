/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.swt.dnd.Clipboard;
import org.rf.ide.core.testdata.model.table.variables.IVariableHolder;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer.PositionCoordinateSerializer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.GeneralSettingsModel;

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
    
    public static PositionCoordinateSerializer[] getPositionsCoordinatesFromClipboard(final Clipboard clipboard) {
        final Object probablyPositions = clipboard.getContents(PositionCoordinateTransfer.getInstance());
        return probablyPositions != null && probablyPositions instanceof PositionCoordinateSerializer[]
                ? (PositionCoordinateSerializer[]) probablyPositions : null;
    }

    public static List<RobotSetting> createSettingsCopy(final List<RobotSetting> settings) {
        final List<RobotSetting> settingsCopy = new ArrayList<>();
        for (final RobotSetting robotSetting : settings) {
            settingsCopy.add(new RobotSetting(null, robotSetting.getGroup(), new String(robotSetting.getName()),
                    new ArrayList<>(robotSetting.getArguments()), new String(robotSetting.getComment())));
        }
        return settingsCopy;
    }
    
    public static List<RobotVariable> createVariablesCopy(final List<RobotVariable> variables) {
        final List<RobotVariable> variablesCopy = new ArrayList<>();
        for (final RobotVariable robotVariable : variables) {
            final IVariableHolder variableHolderCopy = robotVariable.getLinkedElement().copy();
            if (variableHolderCopy != null) {
                variablesCopy.add(new RobotVariable(null, variableHolderCopy));
            }
        }
        return variablesCopy;
    }
    
    public static int findTableIndexOfSelectedSetting(final RobotSettingsSection section,
            final RobotSetting selectedSetting) {
        if (selectedSetting.getGroup() == SettingsGroup.METADATA) {
            return section.getMetadataSettings().indexOf(selectedSetting);
        } else if (selectedSetting.getGroup() == SettingsGroup.NO_GROUP) {
            final Iterator<RobotElement> generalSettingsIterator = GeneralSettingsModel.fillSettingsMapping(section)
                    .values()
                    .iterator();
            int i = 0;
            while (generalSettingsIterator.hasNext()) {
                if (selectedSetting.equals(generalSettingsIterator.next())) {
                    return i;
                }
                i++;
            }
        }
        return section.getImportSettings().indexOf(selectedSetting);
    }

}
