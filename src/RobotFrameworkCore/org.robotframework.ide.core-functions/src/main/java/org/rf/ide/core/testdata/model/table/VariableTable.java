/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable;
import org.rf.ide.core.testdata.model.table.variables.ListVariable;
import org.rf.ide.core.testdata.model.table.variables.ScalarVariable;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.base.Optional;

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

    public ScalarVariable createScalarVariable(final int index, final String name, final List<String> values) {
        RobotToken dec = new RobotToken();
        dec.setRaw(name);
        dec.setText(name);
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
        RobotToken dec = new RobotToken();
        dec.setRaw(name);
        dec.setText(name);
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
            final Map<String, String> items) {
        RobotToken dec = new RobotToken();
        dec.setRaw(name);
        dec.setText(name);
        dec.setType(RobotTokenType.VARIABLES_DICTIONARY_DECLARATION);
        final DictionaryVariable dict = new DictionaryVariable(name, dec, VariableScope.TEST_SUITE);
        final Set<String> keySet = items.keySet();
        for (final String key : keySet) {
            final RobotToken keyT = new RobotToken();
            keyT.setText(key);
            keyT.setType(RobotTokenType.VARIABLES_DICTIONARY_KEY);

            final String value = items.get(key);
            final RobotToken valueT = new RobotToken();
            valueT.setText(value);
            valueT.setType(RobotTokenType.VARIABLES_DICTIONARY_VALUE);

            RobotToken decKey = new RobotToken();
            dict.put(decKey, keyT, valueT);
        }

        dict.setParent(this);
        variables.add(index, dict);

        return dict;
    }

    public void removeVariable(final AVariable variable) {
        variables.remove(variable);
    }

    public boolean moveUpVariable(final AVariable variable) {
        return getMoveHelper().moveUp(variables, variable);
    }

    public boolean moveDownVariable(final AVariable variable) {
        return getMoveHelper().moveDown(variables, variable);
    }

    public boolean isEmpty() {
        return (variables.isEmpty());
    }

    public Optional<AVariable> findVariable(final IRobotLineElement partOfVariable) {
        Optional<AVariable> res = Optional.absent();

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
