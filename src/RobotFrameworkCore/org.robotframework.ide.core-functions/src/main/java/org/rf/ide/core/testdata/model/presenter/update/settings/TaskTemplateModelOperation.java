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
import org.rf.ide.core.testdata.model.table.setting.TaskTemplate;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TaskTemplateModelOperation implements ISettingTableElementOperation {

    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return elementType == RobotTokenType.SETTING_TASK_TEMPLATE_DECLARATION;
    }

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.SUITE_TASK_TEMPLATE;
    }

    @Override
    public AModelElement<?> create(final SettingTable settingsTable, final int tableIndex, final List<String> args, final String comment) {
        final TaskTemplate newTaskTemplate = settingsTable.newTaskTemplate();
        if (!args.isEmpty()) {
            newTaskTemplate.setKeywordName(args.get(0));
        }
        for (int i = 1; i < args.size(); i++) {
            newTaskTemplate.addUnexpectedTrashArgument(args.get(i));
        }
        if (comment != null && !comment.isEmpty()) {
            newTaskTemplate.setComment(comment);
        }
        return newTaskTemplate;
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        final TaskTemplate testTemplate = (TaskTemplate) modelElement;
        if (index == 0) {
            testTemplate.setKeywordName(value != null ? value : "");
        } else if (index > 0) {
            if (value != null) {
                testTemplate.setUnexpectedTrashArgument(index - 1, value);
            } else {
                testTemplate.removeElementToken(index - 1);
            }
        }
    }

    @Override
    public void insert(final SettingTable settingsTable, final int index, final AModelElement<?> modelElement) {
        settingsTable.addTaskTemplate((TaskTemplate) modelElement);
    }

    @Override
    public void remove(final SettingTable settingsTable, final AModelElement<?> modelElements) {
        settingsTable.removeTaskTemplate();
    }
}
