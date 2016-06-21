/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import java.util.List;

import org.eclipse.swt.dnd.Clipboard;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.variables.SetScalarValueCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.variables.SetVariableCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.variables.SetVariableNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.VariablesTransfer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.PasteRobotElementCellsCommandsCollector;

/**
 * @author mmarzec
 *
 */
public class PasteVariablesCellsCommandsCollector extends PasteRobotElementCellsCommandsCollector {

    @Override
    protected boolean hasRobotElementsInClipboard(final Clipboard clipboard) {
        return VariablesTransfer.hasVariables(clipboard);
    }

    @Override
    protected RobotElement[] getRobotElementsFromClipboard(final Clipboard clipboard) {
        final Object probablyVariables = clipboard.getContents(VariablesTransfer.getInstance());
        return probablyVariables != null && probablyVariables instanceof RobotVariable[]
                ? (RobotVariable[]) probablyVariables : null;
    }

    @Override
    protected int findSelectedElementTableIndex(final RobotElement section, final RobotElement selectedElement) {
        return section instanceof RobotVariablesSection
                ? ((RobotVariablesSection) section).getChildren().indexOf(selectedElement) : -1;
    }

    @Override
    protected String findValueToPaste(final RobotElement elementFromClipboard, final int clipboardVariableColumnIndex,
            final int tableColumnsCount) {
        String valueToPaste = "";
        final RobotVariable variableFromClipboard = (RobotVariable) elementFromClipboard;
        if (clipboardVariableColumnIndex == 0) {
            valueToPaste = variableFromClipboard.getLinkedElement().getDeclaration().getText();
        } else if (clipboardVariableColumnIndex == 1) {
            if (variableFromClipboard.getType() == VariableType.SCALAR) {
                valueToPaste = variableFromClipboard.getValue();
            } else if (variableFromClipboard.getType() == VariableType.LIST) {

            } else if (variableFromClipboard.getType() == VariableType.DICTIONARY) {

            }
        } else {
            valueToPaste = variableFromClipboard.getComment();
        }

        return valueToPaste;
    }

    @Override
    protected void collectPasteCommandsForSelectedElement(final RobotElement selectedElement,
            final int selectedElementColumnIndex, final String valueToPaste, final int tableColumnsCount,
            final List<EditorCommand> pasteCommands) {

        if (selectedElement instanceof RobotVariable) {
            final RobotVariable selectedVariable = (RobotVariable) selectedElement;
            if (selectedElementColumnIndex == 0) {
                pasteCommands.add(new SetVariableNameCommand(selectedVariable, valueToPaste));
            } else if (selectedElementColumnIndex == 1) {
                if (selectedVariable.getType() == VariableType.SCALAR) {
                    pasteCommands.add(new SetScalarValueCommand(selectedVariable, valueToPaste));
                } else if (selectedVariable.getType() == VariableType.LIST) {
                    // TODO: copy list items
                } else if (selectedVariable.getType() == VariableType.DICTIONARY) {
                    // TODO: copy dict pairs
                }
            } else {
                pasteCommands.add(new SetVariableCommentCommand(selectedVariable, valueToPaste));
            }
        }
    }

}
