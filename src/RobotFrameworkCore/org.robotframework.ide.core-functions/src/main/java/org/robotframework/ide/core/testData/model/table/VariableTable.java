/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.table.variables.AVariable;
import org.robotframework.ide.core.testData.model.table.variables.AVariable.VariableScope;
import org.robotframework.ide.core.testData.model.table.variables.DictionaryVariable;
import org.robotframework.ide.core.testData.model.table.variables.ListVariable;
import org.robotframework.ide.core.testData.model.table.variables.ScalarVariable;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class VariableTable extends ARobotSectionTable {

    private final List<AVariable> variables = new ArrayList<>();


    public VariableTable(final RobotFile parent) {
        super(parent);
    }


    public List<AVariable> getVariables() {
        return Collections.unmodifiableList(variables);
    }


    public void addVariable(final AVariable variable) {
        variable.setParent(this);
        variables.add(variable);
    }


    public void createScalarVariable(final int index, final String name,
            final List<String> values, final String comment) {
        final ScalarVariable scalar = new ScalarVariable(name, null,
                VariableScope.TEST_SUITE);
        for (final String v : values) {
            final RobotToken t = new RobotToken();
            t.setText(v);
            t.setType(RobotTokenType.VARIABLES_VARIABLE_VALUE);
            scalar.addValue(t);
        }

        scalar.setParent(this);
        variables.add(index, scalar);
    }


    public void createListVariable(final int index, final String name,
            final List<String> values, final String comment) {
        final ListVariable list = new ListVariable(name, null,
                VariableScope.TEST_SUITE);
        for (final String v : values) {
            final RobotToken t = new RobotToken();
            t.setText(v);
            t.setType(RobotTokenType.VARIABLES_VARIABLE_VALUE);
            list.addItem(t);
        }

        list.setParent(this);
        variables.add(index, list);
    }


    public void createDictionaryVariable(final int index, final String name,
            final Map<String, String> items, final String comment) {
        final DictionaryVariable dict = new DictionaryVariable(name, null,
                VariableScope.TEST_SUITE);
        final Set<String> keySet = items.keySet();
        for (final String key : keySet) {
            final RobotToken keyT = new RobotToken();
            keyT.setText(key);
            keyT.setType(RobotTokenType.VARIABLES_DICTIONARY_KEY);

            final String value = items.get(key);
            final RobotToken valueT = new RobotToken();
            valueT.setText(value);
            valueT.setType(RobotTokenType.VARIABLES_DICTIONARY_VALUE);

            dict.put(null, keyT, valueT);
        }

        dict.setParent(this);
        variables.add(index, dict);
    }


    public boolean isEmpty() {
        return (variables.isEmpty());
    }
}
