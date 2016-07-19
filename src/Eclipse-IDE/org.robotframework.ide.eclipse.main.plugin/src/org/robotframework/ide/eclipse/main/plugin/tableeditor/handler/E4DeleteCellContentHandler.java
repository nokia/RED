/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.ui.ISources;
import org.rf.ide.core.testdata.model.ModelType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallNameCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordDefinitionArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordDefinitionNameCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordSettingArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordSettingCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.red.viewers.Selections;

public class E4DeleteCellContentHandler {

    @Execute
    public Object deleteCellContent(@Named(Selections.SELECTION) final IStructuredSelection selection,
            @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor, final RobotEditorCommandsStack commandsStack) {

        final List<RobotElement> elements = Selections.getElements(selection, RobotElement.class);
        if (!elements.isEmpty()) {
            final List<EditorCommand> detailsDeletingCommands = createCommandsForDetailsRemoval(elements, editor.getSelectionLayerAccessor().getSelectionLayer());
            Collections.reverse(detailsDeletingCommands); // deleting must be started from the biggest column index

            for (final EditorCommand command : detailsDeletingCommands) {
                commandsStack.execute(command);
            }
        }

        return null;
    }

    private List<EditorCommand> createCommandsForDetailsRemoval(final List<RobotElement> elements, SelectionLayer selectionLayer) {
        final List<EditorCommand> commands = new ArrayList<>();
        final PositionCoordinate[] selectedCellPositions = selectionLayer.getSelectedCellPositions();
        if (selectedCellPositions.length == 0) {
            return commands;
        }
        final int tableColumnCount = selectionLayer.getColumnCount();
        
        int currentElementRowIndex = 0;
        for (RobotElement element : elements) {
            currentElementRowIndex = TableHandlersSupport.findNextSelectedElementRowIndex(currentElementRowIndex, selectionLayer);
            List<Integer> selectedColumnsIndexes = TableHandlersSupport.findSelectedColumnsIndexesByRowIndex(currentElementRowIndex, selectionLayer);
            for (int i = 0; i < selectedColumnsIndexes.size(); i++) {
                final EditorCommand command = getCommandForSelectedElement(element, selectedColumnsIndexes.get(i), tableColumnCount);
                if(command != null) {
                    commands.add(command);
                }
            }
        }
        
        return commands;
    }
    
    protected EditorCommand getCommandForSelectedElement(final RobotElement selectedElement, final int columnIndex, final int tableColumnCount) {
        if (selectedElement instanceof RobotKeywordCall) {
            final RobotKeywordCall keywordCall = (RobotKeywordCall) selectedElement;
            if (keywordCall.getLinkedElement().getModelType() == ModelType.USER_KEYWORD_EXECUTABLE_ROW) {
                if (columnIndex == 0) {
                    return new SetKeywordCallNameCommand(keywordCall, "");
                } else if (columnIndex > 0 && columnIndex < tableColumnCount - 1) {
                    return new SetKeywordCallArgumentCommand(keywordCall, columnIndex - 1, null);
                } else if (columnIndex == tableColumnCount - 1) {
                    return new SetKeywordCallCommentCommand(keywordCall, null);
                }
            } else {
                if (columnIndex > 0 && columnIndex < tableColumnCount - 1) {
                    return new SetKeywordSettingArgumentCommand(keywordCall, columnIndex - 1, null);
                } else if(columnIndex == tableColumnCount - 1) {
                    return new SetKeywordSettingCommentCommand(keywordCall, null);
                }
            }
        } else if (selectedElement instanceof RobotKeywordDefinition) {
            RobotKeywordDefinition keywordDef = (RobotKeywordDefinition) selectedElement;
            if (columnIndex == 0) {
                return new SetKeywordDefinitionNameCommand(keywordDef, "");
            } else if (columnIndex > 0 && columnIndex < tableColumnCount - 1) {
                final RobotDefinitionSetting argumentsSetting = keywordDef.getArgumentsSetting();
                if (argumentsSetting != null && columnIndex - 1 < argumentsSetting.getArguments().size()) {
                    return new SetKeywordDefinitionArgumentCommand(keywordDef, columnIndex - 1, null);
                }
            }
        }
        return null;
    }

}
