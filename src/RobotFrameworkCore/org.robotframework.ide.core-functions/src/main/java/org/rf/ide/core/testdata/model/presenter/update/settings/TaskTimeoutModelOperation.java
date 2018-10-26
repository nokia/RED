/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.settings;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ISettingTableElementOperation;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.TaskTimeout;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TaskTimeoutModelOperation implements ISettingTableElementOperation {

    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return elementType == RobotTokenType.SETTING_TASK_TIMEOUT_DECLARATION;
    }

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.SUITE_TASK_TIMEOUT;
    }

    @Override
    public AModelElement<?> create(final SettingTable settingsTable, final int tableIndex, final List<String> args,
            final String comment) {
        final TaskTimeout newTaskTimeout = settingsTable.newTaskTimeout();
        if (!args.isEmpty()) {
            newTaskTimeout.setTimeout(args.get(0));
        }
        for (int i = 1; i < args.size(); i++) {
            newTaskTimeout.addMessageArgument(args.get(i));
        }
        if (comment != null && !comment.isEmpty()) {
            newTaskTimeout.setComment(comment);
        }
        return newTaskTimeout;
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        final TaskTimeout testTimeout = (TaskTimeout) modelElement;
        if (index == 0) {
            testTimeout.setTimeout(value != null ? value : "");
        } else if (index > 0) {
            if (value != null) {
                testTimeout.setMessageArgument(index - 1, value);
            } else {
                testTimeout.removeElementToken(index - 1);
            }
        }
    }

    @Override
    public void insert(final SettingTable settingsTable, final int index, final AModelElement<?> modelElement) {
        settingsTable.addTaskTimeout((TaskTimeout) modelElement);
    }

    @Override
    public void remove(final SettingTable settingsTable, final AModelElement<?> modelElements) {
        settingsTable.removeTaskTimeout();
    }
}
