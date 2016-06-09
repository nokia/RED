/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.settings;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ISettingTableElementOperation;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.TestSetup;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestSetupModelOperation extends KeywordBaseModelOperations implements ISettingTableElementOperation {

    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return (elementType == RobotTokenType.SETTING_TEST_SETUP_DECLARATION);
    }

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return (elementType == ModelType.SUITE_TEST_SETUP);
    }

    @Override
    public AModelElement<?> create(final SettingTable settingsTable, final int tableIndex, final List<String> args, final String comment) {
        return super.create(settingsTable.newTestSetup(), args, comment);
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        super.update((TestSetup) modelElement, index, value);
    }

    @Override
    public void remove(final SettingTable settingsTable, final AModelElement<?> modelElements) {
        settingsTable.removeTestSetup();;
    }
}
