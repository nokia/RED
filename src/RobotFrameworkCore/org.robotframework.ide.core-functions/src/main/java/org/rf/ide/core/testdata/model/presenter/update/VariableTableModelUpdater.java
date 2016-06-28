/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.presenter.update.variables.DictionaryVariableModelUpdater;
import org.rf.ide.core.testdata.model.presenter.update.variables.ListVariableModelUpdater;
import org.rf.ide.core.testdata.model.presenter.update.variables.ScalarVariableModelUpdater;
import org.rf.ide.core.testdata.model.table.variables.AVariable;

import com.google.common.annotations.VisibleForTesting;

public class VariableTableModelUpdater {

    private final static List<IVariableTableElementOperation<?>> handlers = new ArrayList<>();

    static {
        handlers.add(new ScalarVariableModelUpdater());
        handlers.add(new ListVariableModelUpdater());
        handlers.add(new DictionaryVariableModelUpdater());
    }

    @SuppressWarnings({ "unchecked" })
    public <T> void addOrSet(final AVariable dst, final int startIndexForModification, final List<?> toAdd) {
        final IVariableTableElementOperation<T> handler = findHandler(dst);
        List<T> converted = handler.convert(toAdd);
        handler.addOrSet(dst, startIndexForModification, converted);
    }

    @SuppressWarnings("rawtypes")
    @VisibleForTesting
    protected IVariableTableElementOperation findHandler(final AVariable dst) {
        IVariableTableElementOperation v = null;
        for (IVariableTableElementOperation c : handlers) {
            if (c.ableToHandle(dst)) {
                v = c;
                break;
            }
        }

        return v;
    }

    @VisibleForTesting
    protected List<IVariableTableElementOperation<?>> getHandlers() {
        return handlers;
    }
}
