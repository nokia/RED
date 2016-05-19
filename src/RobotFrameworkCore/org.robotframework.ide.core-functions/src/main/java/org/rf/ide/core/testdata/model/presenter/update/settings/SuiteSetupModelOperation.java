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
import org.rf.ide.core.testdata.model.table.setting.SuiteSetup;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class SuiteSetupModelOperation extends KeywordBaseModelOperations implements ISettingTableElementOperation {

    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return (elementType == RobotTokenType.SETTING_SUITE_SETUP_DECLARATION);
    }

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return (elementType == ModelType.SUITE_SETUP);
    }

    @Override
    public AModelElement<?> create(SettingTable settingsTable, List<String> args, String comment) {
        return super.create(settingsTable.newSuiteSetup(), args, comment);
    }

    @Override
    public void update(AModelElement<?> modelElement, int index, String value) {
        super.update((SuiteSetup) modelElement, index, value);
    }

    @Override
    public void remove(SettingTable settingsTable, AModelElement<?> modelElements) {
        settingsTable.removeSuiteSetup();
    }
}
