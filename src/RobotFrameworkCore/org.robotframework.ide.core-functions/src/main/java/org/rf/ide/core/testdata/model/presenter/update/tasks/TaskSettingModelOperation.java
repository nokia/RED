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

    public static LocalSetting<Task> create(final BiFunction<Integer, String, LocalSetting<Task>> settingCreator,
            final int index, final String settingName, final List<String> args, final List<String> comments) {

        final LocalSetting<Task> newSetting = settingCreator.apply(index, settingName);
        for (final String arg : args) {
            newSetting.addToken(arg);
        }
        for (final String comment : comments) {
            newSetting.addCommentPart(comment);
        }
        return newSetting;
    }

    @Override
    public final AModelElement<?> insert(final Task task, final int index, final AModelElement<?> modelElement) {
        return task.addElement(index, modelElement);
    }
}
