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
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable.DictionaryKeyValuePair;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.nattable.edit.DetailCellEditorEditingSupport;
import org.robotframework.red.nattable.edit.DetailCellEditorEntry;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

/**
 * @author Michal Anglart
 *
 */
public class DictVariableDetailsEditingSupport implements DetailCellEditorEditingSupport<DictionaryKeyValuePair> {

    private final TableTheme theme;

    private final VariablesDataProvider dataProvider;

    private final IEventBroker eventBroker;

    private RobotVariable model;

    public DictVariableDetailsEditingSupport(final TableTheme theme, final VariablesDataProvider dataProvider,
            final IEventBroker eventBroker) {
        this.theme = theme;
        this.dataProvider = dataProvider;
        this.eventBroker = eventBroker;
    }

    @Override
    public List<DictionaryKeyValuePair> getInput(final int column, final int row) {
        model = dataProvider.getRowObject(row);
        return getDetailElements();
    }

    @Override
    public List<DictionaryKeyValuePair> getDetailElements() {
        final List<DictionaryKeyValuePair> tokens = ((DictionaryVariable) model.getLinkedElement()).getItems();
        return new ArrayList<>(tokens);
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
        final DictionaryVariable dictVariable = (DictionaryVariable) model.getLinkedElement();

        final List<String> splittedContent = Splitter.on('=').splitToList(newElementContent);
        final String key = splittedContent.get(0);
        final String value = Joiner.on('=').join(splittedContent.subList(1, splittedContent.size()));

        final RobotToken rawToken = new RobotToken();
        rawToken.setRaw(newElementContent);
        rawToken.setText(newElementContent);

        final RobotToken keyToken = new RobotToken();
        keyToken.setRaw(key);
        keyToken.setText(key);

        final RobotToken valueToken = new RobotToken();
        valueToken.setRaw(value);
        valueToken.setText(value);

        dictVariable.put(rawToken, keyToken, valueToken);

        eventBroker.post(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, model);
    }

    @Override
    public void removeDetailElements(final List<DictionaryKeyValuePair> elements) {
        final DictionaryVariable dictVariable = (DictionaryVariable) model.getLinkedElement();
        for (final DictionaryKeyValuePair pair : elements) {
            dictVariable.removeKeyValuePair(pair);
        }

        eventBroker.post(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, model);
    }

    @Override
    public void moveLeft(final List<DictionaryKeyValuePair> detailsToMove) {
        final DictionaryVariable dictVariable = (DictionaryVariable) model.getLinkedElement();
        for (final DictionaryKeyValuePair detailToMove : detailsToMove) {
            dictVariable.moveLeftKeyValuePair(detailToMove);
        }

        eventBroker.post(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, model);
    }

    @Override
    public void moveRight(final List<DictionaryKeyValuePair> detailsToMove) {
        final DictionaryVariable dictVariable = (DictionaryVariable) model.getLinkedElement();
        for (final DictionaryKeyValuePair detailToMove : detailsToMove) {
            dictVariable.moveRightKeyValuePair(detailToMove);
        }

        eventBroker.post(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, model);
    }

    @Override
    public void setNewValue(final DictionaryKeyValuePair oldValue, final DictionaryKeyValuePair newValue) {
        boolean thereIsAChange = false;
        if (!oldValue.getRaw().getText().equals(newValue.getRaw().getText())) {
            oldValue.setRaw(newValue.getRaw());

            thereIsAChange = true;
        }
        if (!oldValue.getKey().getText().equals(newValue.getKey().getText())) {
            oldValue.setKey(newValue.getKey());

            thereIsAChange = true;
        }
        if (!oldValue.getValue().getText().equals(newValue.getValue().getText())) {
            oldValue.setValue(newValue.getValue());

            thereIsAChange = true;
        }

        if (thereIsAChange) {
            eventBroker.post(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, model);
        }
    }
}
