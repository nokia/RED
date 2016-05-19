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
import org.rf.ide.core.testdata.model.table.setting.TestTimeout;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestTimeoutModelOperation implements ISettingTableElementOperation {

    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return (elementType == RobotTokenType.SETTING_TEST_TIMEOUT_DECLARATION);
    }

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return (elementType == ModelType.SUITE_TEST_TIMEOUT);
    }

    @Override
    public AModelElement<?> create(final SettingTable settingsTable, final List<String> args, final String comment) {
        final TestTimeout newTestTimeout = settingsTable.newTestTimeout();
        if (!args.isEmpty()) {
            newTestTimeout.setTimeout(args.get(0));
        }
        for (int i = 1; i < args.size(); i++) {
            newTestTimeout.addMessageArgument(args.get(i));
        }
        if (comment != null && !comment.isEmpty()) {
            newTestTimeout.setComment(comment);
        }
        return newTestTimeout;
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        final TestTimeout testTimeout = (TestTimeout) modelElement;
        if (index == 0) {
            testTimeout.setTimeout(value);
        } else if (index > 0) {
            testTimeout.setMessageArgument(index - 1, value);
        } else {
            testTimeout.setComment(value);
        }
    }

    @Override
    public void remove(final SettingTable settingsTable, final AModelElement<?> modelElements) {
        settingsTable.removeTestTimeout();;
    }
}
