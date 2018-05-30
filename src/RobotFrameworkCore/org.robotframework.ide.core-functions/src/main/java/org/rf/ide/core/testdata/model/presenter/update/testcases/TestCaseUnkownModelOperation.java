/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.testcases;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.IExecutablesStepsHolderElementOperation;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseUnknownSettings;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

/**
 * @author Michal Anglart
 *
 */
public class TestCaseUnkownModelOperation implements IExecutablesStepsHolderElementOperation<TestCase> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.TEST_CASE_SETTING_UNKNOWN;
    }

    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return elementType == RobotTokenType.TEST_CASE_SETTING_UNKNOWN_DECLARATION;
    }

    @Override
    public AModelElement<TestCase> create(final TestCase testCase, final int index, final String settingName,
            final List<String> args, final String comment) {
        final TestCaseUnknownSettings unknown = testCase.newUnknownSettings(index);
        unknown.getDeclaration().setText(settingName);

        for (final String arg : args) {
            unknown.addArgument(arg);
        }
        if (comment != null && !comment.isEmpty()) {
            unknown.setComment(comment);
        }
        return unknown;
    }

    @Override
    public AModelElement<?> insert(final TestCase testCase, final int index, final AModelElement<?> modelElement) {
        testCase.addElement((TestCaseUnknownSettings) modelElement, index);
        return modelElement;
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        final TestCaseUnknownSettings unknown = (TestCaseUnknownSettings) modelElement;

        if (value != null) {
            unknown.addArgument(index, value);
        } else {
            unknown.removeElementToken(index);
        }
    }

    @Override
    public void update(final AModelElement<?> modelElement, final List<String> newArguments) {
        final TestCaseUnknownSettings unknown = (TestCaseUnknownSettings) modelElement;

        final int elementsToRemove = unknown.getArguments().size();
        for (int i = 0; i < elementsToRemove; i++) {
            unknown.removeElementToken(0);
        }
        for (int i = 0; i < newArguments.size(); i++) {
            unknown.addArgument(i, newArguments.get(i));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(final TestCase testCase, final AModelElement<?> modelElement) {
        testCase.removeElement((AModelElement<TestCase>) modelElement);
    }
}
