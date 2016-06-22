/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.variables;

import java.util.List;

import org.rf.ide.core.testdata.model.presenter.update.IVariableTableElementOperation;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.model.table.variables.ListVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class ListVariableModelUpdater implements IVariableTableElementOperation<RobotToken> {

    @Override
    public boolean ableToHandle(final AVariable dst) {
        return (dst instanceof ListVariable);
    }

    @Override
    public List<RobotToken> convert(final List<?> p) {
        return VariablesValueConverter.convert(p, RobotToken.class);
    }

    @Override
    public void addOrSet(final AVariable dst, final int startIndexForModification, final List<RobotToken> toAdd) {
        final ListVariable var = (ListVariable) dst;

        int size = toAdd.size();
        for (int i = 0; i < size; i++) {
            var.addItem(toAdd.get(i), startIndexForModification + i);
        }
    }
}
