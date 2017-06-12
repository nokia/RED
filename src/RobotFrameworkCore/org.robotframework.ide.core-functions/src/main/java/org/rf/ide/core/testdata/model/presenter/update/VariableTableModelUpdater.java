/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.rf.ide.core.testdata.model.presenter.update.variables.DictionaryVariableModelUpdater;
import org.rf.ide.core.testdata.model.presenter.update.variables.ListVariableModelUpdater;
import org.rf.ide.core.testdata.model.presenter.update.variables.ScalarVariableModelUpdater;
import org.rf.ide.core.testdata.model.table.variables.AVariable;

import com.google.common.annotations.VisibleForTesting;

public class VariableTableModelUpdater {

    private final static List<IVariableTableElementOperation<?>> ELEMENT_OPERATIONS = newArrayList(
            new ScalarVariableModelUpdater(), new ListVariableModelUpdater(), new DictionaryVariableModelUpdater());

    @SuppressWarnings({ "unchecked" })
    public <T> void addOrSet(final AVariable dst, final int startIndexForModification, final List<?> toAdd) {
        final IVariableTableElementOperation<T> handler = findHandler(dst);
        final List<T> converted = handler.convert(toAdd);
        handler.addOrSet(dst, startIndexForModification, converted);
    }

    @SuppressWarnings("rawtypes")
    @VisibleForTesting
    IVariableTableElementOperation findHandler(final AVariable dst) {
        for (final IVariableTableElementOperation operation : ELEMENT_OPERATIONS) {
            if (operation.ableToHandle(dst)) {
                return operation;
            }
        }
        return null;
    }

    @VisibleForTesting
    List<IVariableTableElementOperation<?>> getHandlers() {
        return ELEMENT_OPERATIONS;
    }
}
