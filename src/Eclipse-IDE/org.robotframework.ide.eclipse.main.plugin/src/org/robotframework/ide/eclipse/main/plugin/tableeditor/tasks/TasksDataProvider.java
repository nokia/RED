/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTasksSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.CodeElementsColumnsPropertyAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.CodeElementsDataProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.ExecutablesRowView;

class TasksDataProvider extends CodeElementsDataProvider<RobotTasksSection> {

    TasksDataProvider(final CodeElementsColumnsPropertyAccessor propertyAccessor,
            final RobotTasksSection robotTasksSection) {
        super(robotTasksSection, propertyAccessor, TasksAdderState.TASK, TasksAdderState.CALL);
    }

    @Override
    protected boolean shouldAddSetting(final RobotKeywordCall setting) {
        @SuppressWarnings("unchecked")
        final AModelElement<Task> linkedSetting = (AModelElement<Task>) setting.getLinkedElement();
        final Task task = linkedSetting.getParent();
        return !task.isDuplicatedSetting(linkedSetting);
    }

    @Override
    protected int numberOfColumns(final Object element) {
        if (element instanceof RobotKeywordCall) {
            final RobotKeywordCall call = (RobotKeywordCall) element;

            if (!call.isDocumentationSetting()) {
                return ExecutablesRowView.rowTokens((RobotKeywordCall) element).size() + 1;
            }
        }
        return 0;
    }
}
