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
import org.rf.ide.core.testdata.model.table.variables.ListVariable;
import org.rf.ide.core.testdata.model.table.variables.ScalarVariable;
import org.rf.ide.core.testdata.model.table.variables.UnknownVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateCompoundVariableValueElementCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.MoveDirection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.MoveListVariableValueElementsCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.RemoveListVariableValueElementsCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetListVariableValueElementCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.nattable.edit.DetailCellEditorEditingSupport;
import org.robotframework.red.nattable.edit.DetailCellEditorEntry;


/**
 * @author Michal Anglart
 *
 */
public class ListVariableDetailsEditingSupport implements DetailCellEditorEditingSupport<RobotToken> {

    private final TableTheme theme;

    private final VariablesDataProvider dataProvider;

    private final RobotEditorCommandsStack commandsStack;

    private RobotVariable model;


    public ListVariableDetailsEditingSupport(final TableTheme theme, final VariablesDataProvider dataProvider,
            final RobotEditorCommandsStack commandsStack) {
        this.theme = theme;
        this.dataProvider = dataProvider;
        this.commandsStack = commandsStack;
    }

    @Override
    public List<RobotToken> getInput(final int column, final int row) {
        model = (RobotVariable) dataProvider.getRowObject(row);
        return getDetailElements();
    }

    @Override
    public List<RobotToken> getDetailElements() {
        if (model == null) {
            throw new IllegalStateException("The input for editing support should be taken first");
        }
        if (model.getType() == VariableType.SCALAR_AS_LIST) {
            final ScalarVariable variable = (ScalarVariable) model.getLinkedElement();
            return new ArrayList<>(variable.getValues());

        } else if (model.getType() == VariableType.LIST) {
            final ListVariable variable = (ListVariable) model.getLinkedElement();
            return new ArrayList<>(variable.getItems());

        } else if (model.getType() == VariableType.INVALID) {
            final UnknownVariable variable = (UnknownVariable) model.getLinkedElement();
            return new ArrayList<>(variable.getItems());
        }
        throw new IllegalStateException(
                "Variables of type " + model.getType() + " are not handled by this editing support");
    }

    @Override
    public DetailCellEditorEntry<RobotToken> createDetailEntry(final Composite parent, final RobotToken detail) {
        final Color hoverColor = theme.getBodyHoveredCellBackground();
        final Color selectionColor = theme.getBodySelectedCellBackground();
        final ListVariableDetailCellEditorEntry entry = new ListVariableDetailCellEditorEntry(parent, hoverColor,
                selectionColor);
        entry.update(detail);
        entry.setIndex(getDetailElements().size(), getDetailElements().indexOf(detail));
        return entry;
    }


    @Override
    public void addNewDetailElement(final String newElementContent) {
        commandsStack.execute(new CreateCompoundVariableValueElementCommand(model, newElementContent));
    }

    @Override
    public void removeDetailElements(final List<RobotToken> elements) {
        commandsStack.execute(new RemoveListVariableValueElementsCommand(model, elements));
    }

    @Override
    public void moveLeft(final List<RobotToken> elements) {
        commandsStack.execute(new MoveListVariableValueElementsCommand(model, elements, MoveDirection.UP));
    }

    @Override
    public void moveRight(final List<RobotToken> elements) {
        commandsStack.execute(new MoveListVariableValueElementsCommand(model, elements, MoveDirection.DOWN));
    }

    @Override
    public void setNewValue(final RobotToken oldValue, final String newValue) {
        commandsStack.execute(new SetListVariableValueElementCommand(model, oldValue, newValue));
    }
}
