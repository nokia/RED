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
import org.rf.ide.core.testdata.model.ICommentHolder;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler.ETokenSeparator;
import org.rf.ide.core.testdata.model.presenter.update.tasks.ExecRowToTaskExecRowMorphOperation;
import org.rf.ide.core.testdata.model.presenter.update.tasks.LocalSettingToTaskSettingMorphOperation;
import org.rf.ide.core.testdata.model.presenter.update.tasks.TaskDocumentationModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.tasks.TaskEmptyLineModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.tasks.TaskExecutableRowModelOperation;
import org.rf.ide.core.testdata.model.presenter.update.tasks.TaskSettingModelOperation;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;

public class TaskTableModelUpdater implements IExecutablesTableModelUpdater<Task> {

    private static final List<IExecutablesStepsHolderElementOperation<Task>> ELEMENT_OPERATIONS = newArrayList(
            new TaskEmptyLineModelOperation(), new TaskExecutableRowModelOperation(),
            new TaskDocumentationModelOperation(), new TaskSettingModelOperation(ModelType.TASK_SETUP),
            new TaskSettingModelOperation(ModelType.TASK_TAGS), new TaskSettingModelOperation(ModelType.TASK_TEARDOWN),
            new TaskSettingModelOperation(ModelType.TASK_TEMPLATE),
            new TaskSettingModelOperation(ModelType.TASK_TIMEOUT),
            new TaskSettingModelOperation(ModelType.TASK_SETTING_UNKNOWN),

            new ExecRowToTaskExecRowMorphOperation(),
            new LocalSettingToTaskSettingMorphOperation());

    @Override
    public AModelElement<Task> createEmptyLine(final Task task, final int index, final String name) {
        final IExecutablesStepsHolderElementOperation<Task> operationHandler = getOperationHandler(
                ModelType.EMPTY_LINE);
        if (operationHandler == null || task == null) {
            throw new IllegalArgumentException("Unable to create empty line. Operation handler is missing");
        }
        return task.addElement(index, operationHandler.create(task, index, name, null, null));
    }

    @Override
    public AModelElement<Task> createSetting(final Task task, final int index, final String settingName,
            final String comment, final List<String> args) {

        if (task == null) {
            throw new IllegalArgumentException("Unable to create " + settingName + " setting. There is no task given");
        }
        final BiFunction<Integer, String, LocalSetting<Task>> creator = getSettingCreateOperation(task, settingName);
        if (creator == null) {
            throw new IllegalArgumentException(
                    "Unable to create " + settingName + " setting. Operation handler is missing");
        }
        return new TaskSettingModelOperation(null).create(creator, index, settingName, args, comment);
    }

    @Override
    public AModelElement<Task> createExecutableRow(final Task task, final int index, final String action,
            final String comment, final List<String> args) {

        final IExecutablesStepsHolderElementOperation<Task> operationHandler = getOperationHandler(
                ModelType.TASK_EXECUTABLE_ROW);
        if (operationHandler == null || task == null) {
            throw new IllegalArgumentException(
                    "Unable to create " + action + " executable row. Operation handler is missing");
        }
        return task.addElement(index, operationHandler.create(task, index, action, args, comment));
    }

    @Override
    public void updateArgument(final AModelElement<?> modelElement, final int index, final String value) {
        final IExecutablesStepsHolderElementOperation<Task> operationHandler = getOperationHandler(
                modelElement.getModelType());

        if (operationHandler == null) {
            throw new IllegalArgumentException(
                    "Unable to update arguments of " + modelElement + ". Operation handler is missing");
        }
        operationHandler.update(modelElement, index, value);
    }

    @Override
    public void setArguments(final AModelElement<?> modelElement, final List<String> arguments) {
        final IExecutablesStepsHolderElementOperation<Task> operationHandler = getOperationHandler(
                modelElement.getModelType());

        if (operationHandler == null) {
            throw new IllegalArgumentException(
                    "Unable to set arguments of " + modelElement + ". Operation handler is missing");
        }
        operationHandler.update(modelElement, arguments);
    }

    @Override
    public void updateComment(final AModelElement<?> modelElement, final String value) {
        CommentServiceHandler.update((ICommentHolder) modelElement, ETokenSeparator.PIPE_WRAPPED_WITH_SPACE, value);
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
        return operationHandler.insert(task, index, modelElement);
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
}
