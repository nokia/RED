/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;

public interface ISettingTableElementOperation {

    boolean isApplicable(final IRobotTokenType elementType);

    boolean isApplicable(final ModelType elementType);

    AModelElement<?> create(final SettingTable settingsTable, final int tableIndex, final List<String> args, final String comment);

    void update(final AModelElement<?> modelElement, final int index, final String value);

    void remove(final SettingTable settingsTable, final AModelElement<?> modelElements);
}
