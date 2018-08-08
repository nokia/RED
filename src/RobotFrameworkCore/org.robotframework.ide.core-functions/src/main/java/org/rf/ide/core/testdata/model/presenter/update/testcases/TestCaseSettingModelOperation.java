/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.testcases;

import java.util.List;
import java.util.function.BiFunction;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.IExecutablesStepsHolderElementOperation;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.LocalSettingTokenTypes;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;


public class TestCaseSettingModelOperation implements IExecutablesStepsHolderElementOperation<TestCase> {

    private final ModelType handledType;

    public TestCaseSettingModelOperation(final ModelType handledType) {
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
    public AModelElement<TestCase> create(final TestCase executablesHolder, final int index, final String action,
            final List<String> args, final String comment) {
        throw new IllegalStateException();
    }

    public final AModelElement<TestCase> create(
            final BiFunction<Integer, String, LocalSetting<TestCase>> settingCreator, final int index,
            final String settingName, final List<String> args, final String comment) {

        final LocalSetting<TestCase> newSetting = settingCreator.apply(index, settingName);
        for (final String arg : args) {
            newSetting.addToken(arg);
        }
        if (comment != null && !comment.isEmpty()) {
            newSetting.setComment(comment);
        }
        return newSetting;
    }

    @Override
    public final AModelElement<?> insert(final TestCase testCase, final int index,
            final AModelElement<?> modelElement) {
        return testCase.addElement(index, modelElement);
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
