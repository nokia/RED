/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.function.BiFunction;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.tasks.ExecRowToTaskExecRowMorphOperation;
import org.rf.ide.core.testdata.model.presenter.update.tasks.LocalSettingToTaskSettingMorphOperation;
import org.rf.ide.core.testdata.model.presenter.update.tasks.TaskEmptyLineModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.tasks.TaskExecutableRowModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.tasks.TaskSettingModelOperation;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.RobotEmptyRow;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;

public class TaskTableModelUpdater implements IExecutablesTableModelUpdater<Task> {

    private static final List<IExecutablesStepsHolderElementOperation<Task>> ELEMENT_OPERATIONS = newArrayList(
            new TaskEmptyLineModelOperation(), new TaskExecutableRowModelOperation(),
            new TaskSettingModelOperation(ModelType.TASK_DOCUMENTATION),
            new TaskSettingModelOperation(ModelType.TASK_SETUP),
            new TaskSettingModelOperation(ModelType.TASK_TAGS), new TaskSettingModelOperation(ModelType.TASK_TEARDOWN),
            new TaskSettingModelOperation(ModelType.TASK_TEMPLATE),
            new TaskSettingModelOperation(ModelType.TASK_TIMEOUT),
            new TaskSettingModelOperation(ModelType.TASK_SETTING_UNKNOWN),

            new ExecRowToTaskExecRowMorphOperation(),
            new LocalSettingToTaskSettingMorphOperation());

    @SuppressWarnings("unchecked")
    @Override
    public AModelElement<Task> createExecutableRow(final Task task, final int index, final List<String> cells) {
        final RobotExecutableRow<?> row = TestCaseTableModelUpdater.createExecutableRow(cells);
        task.addElement(index, row);
        fixTemplateArguments(task, row);
        return (RobotExecutableRow<Task>) row;
    }

    @Override
    public AModelElement<Task> createSetting(final Task task, final int index, final List<String> cells) {
        if (cells.isEmpty()) {
            throw new IllegalArgumentException("Unable to create empty setting. There is no setting name given");
        }
        final String settingName = cells.get(0);
        if (task == null) {
            throw new IllegalArgumentException(
                    "Unable to create " + settingName + " setting. There is no parent given");
        }
        final BiFunction<Integer, String, LocalSetting<Task>> creator = getSettingCreateOperation(task, settingName);
        if (creator == null) {
            throw new IllegalArgumentException(
                    "Unable to create " + settingName + " setting. Operation handler is missing");
        }
        final int cmtIndex = TestCaseTableModelUpdater.indexOfCommentStart(cells);

        final LocalSetting<Task> newSetting = creator.apply(index, settingName);
        for (int i = 1; i < cells.size(); i++) {
            if (i < cmtIndex) {
                newSetting.addToken(RobotToken.create(cells.get(i)));
            } else {
                newSetting.addCommentPart(RobotToken.create(cells.get(i)));
            }
        }
        return newSetting;
    }

    @SuppressWarnings("unchecked")
    @Override
    public AModelElement<Task> createEmptyLine(final Task task, final int index, final List<String> cells) {
        final RobotEmptyRow<?> row = TestCaseTableModelUpdater.createEmptyLine(cells);
        task.addElement(index, row);
        return (RobotEmptyRow<Task>) row;
    }

    @Override
    public AModelElement<?> insert(final Task task, final int index, final AModelElement<?> modelElement) {

        // morph operations enables inserting settings taken from keywords elements
        final IExecutablesStepsHolderElementOperation<Task> operationHandler = getOperationHandler(
                modelElement.getModelType());
        if (operationHandler == null) {
            throw new IllegalArgumentException("Unable to insert " + modelElement + " into "
                    + task.getName().getText() + " task. Operation handler is missing");
        }
        final AModelElement<?> inserted = operationHandler.insert(task, index, modelElement);
        fixTemplateArguments(task, inserted);
        return inserted;
    }

    private BiFunction<Integer, String, LocalSetting<Task>> getSettingCreateOperation(final Task task,
            final String settingName) {
        final RobotTokenType type = RobotTokenType.findTypeOfDeclarationForTaskSettingTable(settingName);
        switch (type) {
            case TASK_SETTING_SETUP: return task::newSetup;
            case TASK_SETTING_TEARDOWN: return task::newTeardown;
            case TASK_SETTING_DOCUMENTATION: return task::newDocumentation;
            case TASK_SETTING_TAGS_DECLARATION: return task::newTags;
            case TASK_SETTING_TIMEOUT: return task::newTimeout;
            case TASK_SETTING_TEMPLATE: return task::newTemplate;
            default: return task::newUnknownSetting;
        }
    }

    @VisibleForTesting
    IExecutablesStepsHolderElementOperation<Task> getOperationHandler(final ModelType type) {
        return ELEMENT_OPERATIONS.stream().filter(op -> op.isApplicable(type)).findFirst().orElse(null);
    }

    @VisibleForTesting
    IExecutablesStepsHolderElementOperation<Task> getOperationHandler(final IRobotTokenType type) {
        return ELEMENT_OPERATIONS.stream().filter(op -> op.isApplicable(type)).findFirst().orElse(null);
    }

    private void fixTemplateArguments(final Task task, final AModelElement<?> modelElement) {
        if (modelElement instanceof RobotExecutableRow<?>) {
            final RobotExecutableRow<?> row = (RobotExecutableRow<?>) modelElement;
            row.fixTemplateArgumentsTypes(task.getTemplateKeywordName().isPresent(),
                    RobotTokenType.TASK_TEMPLATE_ARGUMENT);
        }
    }
}
