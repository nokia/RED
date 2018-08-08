/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.tasks;

import java.util.List;
import java.util.function.BiFunction;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.IExecutablesStepsHolderElementOperation;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.LocalSettingTokenTypes;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;


public class TaskSettingModelOperation implements IExecutablesStepsHolderElementOperation<Task> {

    private final ModelType handledType;

    public TaskSettingModelOperation(final ModelType handledType) {
        this.handledType = handledType;
    }

    @Override
    public final boolean isApplicable(final ModelType elementType) {
        return handledType == elementType;
    }

    @Override
    public final boolean isApplicable(final IRobotTokenType elementType) {
        return LocalSettingTokenTypes.getTokenType(handledType, 0) == elementType;
    }

    @Override
    public final AModelElement<Task> create(final Task executablesHolder, final int index, final String action,
            final List<String> args, final String comment) {
        throw new IllegalStateException();
    }

    public final AModelElement<Task> create(final BiFunction<Integer, String, LocalSetting<Task>> settingCreator,
            final int index, final String settingName, final List<String> args, final String comment) {

        final LocalSetting<Task> tags = settingCreator.apply(index, settingName);
        for (final String arg : args) {
            tags.addToken(arg);
        }
        if (comment != null && !comment.isEmpty()) {
            tags.setComment(comment);
        }
        return tags;
    }

    @Override
    public final AModelElement<?> insert(final Task task, final int index, final AModelElement<?> modelElement) {
        return task.addElement(index, modelElement);
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        final LocalSetting<?> setting = (LocalSetting<?>) modelElement;
        setting.setToken(value, index + 1);
    }

    @Override
    public void update(final AModelElement<?> modelElement, final List<String> newArguments) {
        final LocalSetting<?> setting = (LocalSetting<?>) modelElement;
        setting.setTokens(newArguments);
    }
}
