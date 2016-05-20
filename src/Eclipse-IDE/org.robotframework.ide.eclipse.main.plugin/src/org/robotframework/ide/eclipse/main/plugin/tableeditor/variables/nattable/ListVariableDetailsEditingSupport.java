/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.ListVariable;
import org.rf.ide.core.testdata.model.table.variables.ScalarVariable;
import org.rf.ide.core.testdata.model.table.variables.UnknownVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
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

    private final IEventBroker eventBroker;

    private RobotVariable model;

    public ListVariableDetailsEditingSupport(final TableTheme theme, final VariablesDataProvider dataProvider,
            final IEventBroker eventBroker) {
        this.theme = theme;
        this.dataProvider = dataProvider;
        this.eventBroker = eventBroker;
    }

    @Override
    public List<RobotToken> getInput(final int column, final int row) {
        model = dataProvider.getRowObject(row);
        return getDetailElements();
    }

    @Override
    public List<RobotToken> getDetailElements() {
        if (model.getLinkedElement() instanceof ListVariable) {
            final ListVariable variable = (ListVariable) model.getLinkedElement();
            return new ArrayList<>(variable.getItems());

        } else if (model.getLinkedElement() instanceof ScalarVariable
                && model.getType() == VariableType.SCALAR_AS_LIST) {
            final ScalarVariable variable = (ScalarVariable) model.getLinkedElement();
            return new ArrayList<>(variable.getValues());

        } else if (model.getLinkedElement() instanceof UnknownVariable) {
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
        final RobotToken token = new RobotToken();
        token.setText(newElementContent);

        if (model.getLinkedElement() instanceof ListVariable) {
            final ListVariable variable = (ListVariable) model.getLinkedElement();
            variable.addItem(token);

        } else if (model.getLinkedElement() instanceof ScalarVariable
                && model.getType() == VariableType.SCALAR_AS_LIST) {
            final ScalarVariable variable = (ScalarVariable) model.getLinkedElement();
            variable.addValue(token);

        } else if (model.getLinkedElement() instanceof UnknownVariable) {
            final UnknownVariable variable = (UnknownVariable) model.getLinkedElement();
            variable.addItem(token);
        } else {
            throw new IllegalStateException(
                    "Variables of type " + model.getType() + " are not handled by this editing support");
        }
        
        eventBroker.post(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, model);
    }

    @Override
    public void removeDetailElements(final List<RobotToken> elements) {
        for (final RobotToken token : elements) {

            if (model.getLinkedElement() instanceof ListVariable) {
                final ListVariable variable = (ListVariable) model.getLinkedElement();
                variable.removeItem(token);

            } else if (model.getLinkedElement() instanceof ScalarVariable
                    && model.getType() == VariableType.SCALAR_AS_LIST) {
                final ScalarVariable variable = (ScalarVariable) model.getLinkedElement();
                variable.removeValue(token);

            } else if (model.getLinkedElement() instanceof UnknownVariable) {
                final UnknownVariable variable = (UnknownVariable) model.getLinkedElement();
                variable.removeItem(token);
            } else {
                throw new IllegalStateException(
                        "Variables of type " + model.getType() + " are not handled by this editing support");
            }
        }

        eventBroker.post(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, model);
    }

    @Override
    public void moveLeft(final List<RobotToken> elements) {
        for (final RobotToken token : elements) {

            if (model.getLinkedElement() instanceof ListVariable) {
                final ListVariable variable = (ListVariable) model.getLinkedElement();
                variable.moveLeftItem(token);

            } else if (model.getLinkedElement() instanceof ScalarVariable
                    && model.getType() == VariableType.SCALAR_AS_LIST) {
                final ScalarVariable variable = (ScalarVariable) model.getLinkedElement();
                variable.moveLeftValue(token);

            } else if (model.getLinkedElement() instanceof UnknownVariable) {
                final UnknownVariable variable = (UnknownVariable) model.getLinkedElement();
                variable.moveLeftItem(token);
            } else {
                throw new IllegalStateException(
                        "Variables of type " + model.getType() + " are not handled by this editing support");
            }
        }

        eventBroker.post(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, model);
    }

    @Override
    public void moveRight(final List<RobotToken> elements) {
        for (final RobotToken token : elements) {

            if (model.getLinkedElement() instanceof ListVariable) {
                final ListVariable variable = (ListVariable) model.getLinkedElement();
                variable.moveRightItem(token);

            } else if (model.getLinkedElement() instanceof ScalarVariable
                    && model.getType() == VariableType.SCALAR_AS_LIST) {
                final ScalarVariable variable = (ScalarVariable) model.getLinkedElement();
                variable.moveRightValue(token);

            } else if (model.getLinkedElement() instanceof UnknownVariable) {
                final UnknownVariable variable = (UnknownVariable) model.getLinkedElement();
                variable.moveRightItem(token);
            } else {
                throw new IllegalStateException(
                        "Variables of type " + model.getType() + " are not handled by this editing support");
            }
        }

        eventBroker.post(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, model);
    }

    @Override
    public void setNewValue(final RobotToken oldValue, final RobotToken newValue) {
        if (!oldValue.getText().equals(newValue.getText())) {
            oldValue.setText(newValue.getText());

            eventBroker.post(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, model);
        }
    }
}
