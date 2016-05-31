/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable.DictionaryKeyValuePair;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateCompoundVariableValueElementCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.MoveDictVariableValueElementsCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.MoveDirection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.RemoveDictVariableValueElementsCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetDictVariableValueElementCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.nattable.edit.DetailCellEditorEditingSupport;
import org.robotframework.red.nattable.edit.DetailCellEditorEntry;

/**
 * @author Michal Anglart
 *
 */
public class DictVariableDetailsEditingSupport implements DetailCellEditorEditingSupport<DictionaryKeyValuePair> {

    private final TableTheme theme;

    private final VariablesDataProvider dataProvider;

    private final RobotEditorCommandsStack commandsStack;

    private RobotVariable model;

    public DictVariableDetailsEditingSupport(final TableTheme theme, final VariablesDataProvider dataProvider,
            final RobotEditorCommandsStack commandsStack) {
        this.theme = theme;
        this.dataProvider = dataProvider;
        this.commandsStack = commandsStack;
    }

    @Override
    public List<DictionaryKeyValuePair> getInput(final int column, final int row) {
        model = dataProvider.getRowObject(row);
        return getDetailElements();
    }

    @Override
    public List<DictionaryKeyValuePair> getDetailElements() {
        if (model == null) {
            throw new IllegalStateException("The input for editing support should be taken first");
        }
        if (model.getType() == VariableType.DICTIONARY) {
            final List<DictionaryKeyValuePair> tokens = ((DictionaryVariable) model.getLinkedElement()).getItems();
            return new ArrayList<>(tokens);
        }
        throw new IllegalStateException(
                "Variables of type " + model.getType() + " are not handled by this editing support");
    }

    @Override
    public DetailCellEditorEntry<DictionaryKeyValuePair> createDetailEntry(final Composite parent,
            final DictionaryKeyValuePair detail) {
        final Color hoverColor = theme.getBodyHoveredCellBackground();
        final Color selectionColor = theme.getBodySelectedCellBackground();
        final DictVariableDetailCellEditorEntry entry = new DictVariableDetailCellEditorEntry(parent, hoverColor,
                selectionColor);
        entry.update(detail);
        return entry;
    }

    @Override
    public void addNewDetailElement(final String newElementContent) {
        commandsStack.execute(new CreateCompoundVariableValueElementCommand(model, newElementContent));
    }

    @Override
    public void removeDetailElements(final List<DictionaryKeyValuePair> elements) {
        commandsStack.execute(new RemoveDictVariableValueElementsCommand(model, elements));
    }

    @Override
    public void moveLeft(final List<DictionaryKeyValuePair> detailsToMove) {
        commandsStack.execute(new MoveDictVariableValueElementsCommand(model, detailsToMove, MoveDirection.UP));
    }

    @Override
    public void moveRight(final List<DictionaryKeyValuePair> detailsToMove) {
        commandsStack.execute(new MoveDictVariableValueElementsCommand(model, detailsToMove, MoveDirection.DOWN));
    }

    @Override
    public void setNewValue(final DictionaryKeyValuePair oldValue, final String newValue) {
        commandsStack.execute(new SetDictVariableValueElementCommand(model, oldValue, newValue));
    }
}
