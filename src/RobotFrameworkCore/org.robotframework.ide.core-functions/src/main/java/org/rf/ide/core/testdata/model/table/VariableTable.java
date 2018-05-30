/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.presenter.MoveElementHelper;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable;
import org.rf.ide.core.testdata.model.table.variables.ListVariable;
import org.rf.ide.core.testdata.model.table.variables.ScalarVariable;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

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

    public void addVariable(final int index, final AVariable variable) {
        variable.setParent(this);
        variables.add(index, variable);
    }

    public ScalarVariable createScalarVariable(final int index, final String name, final List<String> values) {
        final RobotToken dec = new RobotToken();
        dec.setText(VariableType.SCALAR.getIdentificator() + "{" + name + "}");
        dec.setType(RobotTokenType.VARIABLES_SCALAR_DECLARATION);

        final ScalarVariable scalar = new ScalarVariable(name, dec, VariableScope.TEST_SUITE);
        for (final String v : values) {
            final RobotToken t = new RobotToken();
            t.setText(v);
            t.setType(RobotTokenType.VARIABLES_VARIABLE_VALUE);
            scalar.addValue(t);
        }
        scalar.setParent(this);
        variables.add(index, scalar);

        return scalar;
    }

    public ListVariable createListVariable(final int index, final String name, final List<String> values) {
        final RobotToken dec = new RobotToken();
        dec.setText(VariableType.LIST.getIdentificator() + "{" + name + "}");
        dec.setType(RobotTokenType.VARIABLES_LIST_DECLARATION);
        final ListVariable list = new ListVariable(name, dec, VariableScope.TEST_SUITE);
        for (final String v : values) {
            final RobotToken t = new RobotToken();
            t.setText(v);
            t.setType(RobotTokenType.VARIABLES_VARIABLE_VALUE);
            list.addItem(t);
        }

        list.setParent(this);
        variables.add(index, list);

        return list;
    }

    public DictionaryVariable createDictionaryVariable(final int index, final String name,
            final List<Entry<String, String>> items) {
        final RobotToken dec = new RobotToken();
        dec.setText(VariableType.DICTIONARY.getIdentificator() + "{" + name + "}");
        dec.setType(RobotTokenType.VARIABLES_DICTIONARY_DECLARATION);
        final DictionaryVariable dict = new DictionaryVariable(name, dec, VariableScope.TEST_SUITE);
        for (final Entry<String, String> e : items) {
            final RobotToken keyT = new RobotToken();
            keyT.setText(e.getKey());
            keyT.setType(RobotTokenType.VARIABLES_DICTIONARY_KEY);

            final String value = e.getValue();
            final RobotToken valueT = new RobotToken();
            valueT.setText(value);
            valueT.setType(RobotTokenType.VARIABLES_DICTIONARY_VALUE);

            final RobotToken decKey = new RobotToken();
            dict.put(decKey, keyT, valueT);
        }

        dict.setParent(this);
        variables.add(index, dict);

        return dict;
    }

    public Entry<String, String> createEntry(final String key, final String value) {
        return new DictionaryEntry(key, value);
    }

    private static class DictionaryEntry implements Entry<String, String> {

        private final String key;

        private String value;

        public DictionaryEntry(final String key, final String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String setValue(final String value) {
            this.value = value;
            return value;
        }
    }

    public void removeVariable(final AVariable variable) {
        variables.remove(variable);
    }

    public boolean moveUpVariable(final AVariable variable) {
        return MoveElementHelper.moveUp(variables, variable);
    }

    public boolean moveDownVariable(final AVariable variable) {
        return MoveElementHelper.moveDown(variables, variable);
    }

    public boolean isEmpty() {
        return (variables.isEmpty());
    }

    public Optional<AVariable> findVariable(final IRobotLineElement partOfVariable) {
        Optional<AVariable> res = Optional.empty();

        for (final AVariable var : variables) {
            final List<RobotToken> elems = var.getElementTokens();
            for (final IRobotLineElement e : elems) {
                if (e == partOfVariable) {
                    res = Optional.of(var);
                    break;
                }
            }
        }

        return res;
    }
}
