/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update;

import java.util.List;

import org.rf.ide.core.testdata.model.table.variables.AVariable;

public interface IVariableTableElementOperation<T> {

    boolean ableToHandle(final AVariable dst);

    List<T> convert(final List<?> p);

    void addOrSet(final AVariable dst, final int startIndexForModification, final List<T> toAdd);
}
