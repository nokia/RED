/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.presenter.update.variables.VariablesValueConverter;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable;
import org.rf.ide.core.testdata.model.table.variables.ListVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.variables.SetDictItemsCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.variables.SetListItemsCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.variables.SetScalarValueCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.variables.SetVariableCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.variables.SetVariableNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.PasteRobotElementCellsCommandsCollector;

/**
 * @author mmarzec
 *
 */
public class PasteVariablesCellsCommandsCollector extends PasteRobotElementCellsCommandsCollector {

    @Override
    protected boolean hasRobotElementsInClipboard(final RedClipboard clipboard) {
        return clipboard.hasVariables();
    }

    @Override
    protected RobotElement[] getRobotElementsFromClipboard(final RedClipboard clipboard) {
        return clipboard.getVariables();
    }

    @Override
    protected int findSelectedElementTableIndex(final RobotElement section, final RobotElement selectedElement) {
        return section instanceof RobotVariablesSection
                ? ((RobotVariablesSection) section).getChildren().indexOf(selectedElement) : -1;
    }

    @Override
    protected List<String> findValuesToPaste(final RobotElement elementFromClipboard, final int clipboardVariableColumnIndex,
            final int tableColumnsCount) {
        final RobotVariable variableFromClipboard = (RobotVariable) elementFromClipboard;
        if (clipboardVariableColumnIndex == 0) {
            return newArrayList(variableFromClipboard.getLinkedElement().getDeclaration().getText());
        } else if (clipboardVariableColumnIndex == 1) {
            if (variableFromClipboard.getType() == VariableType.SCALAR) {
                return newArrayList(variableFromClipboard.getValue());
            } else if (variableFromClipboard.getType() == VariableType.LIST) {
                final ListVariable listVariable = (ListVariable) variableFromClipboard.getLinkedElement();
                return VariablesValueConverter.convert(listVariable.getItems(), String.class);
            } else if (variableFromClipboard.getType() == VariableType.DICTIONARY) {
                final DictionaryVariable dictVariable = (DictionaryVariable) variableFromClipboard.getLinkedElement();
                return VariablesValueConverter.convert(dictVariable.getItems(), String.class);
            }
        } else {
            return newArrayList(variableFromClipboard.getComment());
        }

        return newArrayList();
    }

    @Override
    protected List<EditorCommand> collectPasteCommandsForSelectedElement(final RobotElement selectedElement,
            final List<String> valuesToPaste, final int selectedElementColumnIndex, final int tableColumnsCount) {

        final List<EditorCommand> pasteCommands = new ArrayList<>();
        if (selectedElement instanceof RobotVariable && !valuesToPaste.isEmpty()) {
            final RobotVariable selectedVariable = (RobotVariable) selectedElement;
            if (selectedElementColumnIndex == 0) {
                pasteCommands.add(new SetVariableNameCommand(selectedVariable, valuesToPaste.get(0)));
            } else if (selectedElementColumnIndex == 1) {
                if (selectedVariable.getType() == VariableType.SCALAR) {
                    pasteCommands.add(new SetScalarValueCommand(selectedVariable, valuesToPaste.get(0)));
                } else if (selectedVariable.getType() == VariableType.LIST) {
                    pasteCommands.add(new SetListItemsCommand(selectedVariable, valuesToPaste));
                } else if (selectedVariable.getType() == VariableType.DICTIONARY) {
                    pasteCommands.add(new SetDictItemsCommand(selectedVariable, valuesToPaste));
                }
            } else {
                pasteCommands.add(new SetVariableCommentCommand(selectedVariable, valuesToPaste.get(0)));
            }
        }
        return pasteCommands;
    }
}
