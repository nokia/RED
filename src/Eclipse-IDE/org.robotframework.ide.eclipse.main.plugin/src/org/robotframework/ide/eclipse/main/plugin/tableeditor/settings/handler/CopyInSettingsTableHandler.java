/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.ArraysSerializerDeserializer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer.PositionCoordinateSerializer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.TableHandlersSupport;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.CopyInSettingsTableHandler.E4CopyInSettingsTableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CopyInSettingsTableHandler extends DIParameterizedHandler<E4CopyInSettingsTableHandler> {

    public CopyInSettingsTableHandler() {
        super(E4CopyInSettingsTableHandler.class);
    }

    public static class E4CopyInSettingsTableHandler {

        @Execute
        public boolean copy(@Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection, final RedClipboard clipboard) {

            final RobotSetting[] settings = Selections.getElementsArray(selection, RobotSetting.class);
            final PositionCoordinate[] selectedCellPositions = editor.getSelectionLayerAccessor()
                    .getSelectionLayer()
                    .getSelectedCellPositions();

            if (selectedCellPositions.length > 0 && settings.length > 0) {
                final PositionCoordinateSerializer[] serializablePositions = TableHandlersSupport
                        .createSerializablePositionsCoordinates(selectedCellPositions);
                final RobotSetting[] settingsCopy = ArraysSerializerDeserializer.copy(RobotSetting.class, settings);

                clipboard.insertContent(serializablePositions, settingsCopy);

                return true;
            }
            return false;
        }

    }
}
