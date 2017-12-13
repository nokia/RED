/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.presenter.update.variables.VariablesValueConverter;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable;
import org.rf.ide.core.testdata.model.table.variables.ListVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
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
    protected List<String> findValuesToPaste(final RobotElement elementFromClipboard, final int columnIndex,
            final int tableColumnsCount) {
        final RobotVariable variable = (RobotVariable) elementFromClipboard;
        if (columnIndex == 0) {
            return newArrayList(variable.getLinkedElement().getDeclaration().getText());

        } else if (columnIndex == 1) {
            if (variable.getType() == VariableType.SCALAR) {
                return newArrayList(variable.getValue());

            } else if (variable.getType() == VariableType.LIST) {
                final ListVariable listVariable = (ListVariable) variable.getLinkedElement();
                return extractVariableValues(listVariable.getItems());

            } else if (variable.getType() == VariableType.DICTIONARY) {
                final DictionaryVariable dictVariable = (DictionaryVariable) variable.getLinkedElement();
                return extractVariableValues(dictVariable.getItems());
            }
        } else {
            return newArrayList(variable.getComment());
        }
        return new ArrayList<>();
    }

    private List<String> extractVariableValues(final List<?> values) {
        return VariablesValueConverter.convert(values, RobotToken.class).stream().map(RobotToken::getText).collect(
                toList());
    }

    @Override
    protected List<EditorCommand> collectPasteCommandsForSelectedElement(final RobotElement selectedElement,
            final List<String> valuesToPaste, final int selectedColumnIndex, final int tableColumnsCount) {

        if (selectedElement instanceof RobotVariable && !valuesToPaste.isEmpty()) {
            final RobotVariable selectedVariable = (RobotVariable) selectedElement;

            if (selectedColumnIndex == 0) {
                return newArrayList(new SetVariableNameCommand(selectedVariable, valuesToPaste.get(0)));

            } else if (selectedColumnIndex == 1) {
                if (selectedVariable.getType() == VariableType.SCALAR) {
                    return newArrayList(new SetScalarValueCommand(selectedVariable, valuesToPaste.get(0)));

                } else if (selectedVariable.getType() == VariableType.LIST) {
                    return newArrayList(new SetListItemsCommand(selectedVariable, valuesToPaste));

                } else if (selectedVariable.getType() == VariableType.DICTIONARY) {
                    return newArrayList(new SetDictItemsCommand(selectedVariable, valuesToPaste));
                }
            } else {
                return newArrayList(new SetVariableCommentCommand(selectedVariable, valuesToPaste.get(0)));
            }
        }
        return new ArrayList<>();
    }
}
