/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.variables;

import java.util.List;

import org.rf.ide.core.testdata.model.presenter.update.IVariableTableElementOperation;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.model.table.variables.ScalarVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class ScalarVariableModelUpdater implements IVariableTableElementOperation<RobotToken> {

    @Override
    public boolean ableToHandle(final AVariable dst) {
        return (dst instanceof ScalarVariable);
    }

    @Override
    public List<RobotToken> convert(final List<?> p) {
        return VariablesValueConverter.convert(p, RobotToken.class);
    }

    @Override
    public void addOrSet(final AVariable dst, final int startIndexForModification, final List<RobotToken> toAdd) {
        final ScalarVariable var = (ScalarVariable) dst;

        int size = toAdd.size();
        for (int i = startIndexForModification; i < size; i++) {
            var.addValue(toAdd.get(i), i);
        }
    }
}
