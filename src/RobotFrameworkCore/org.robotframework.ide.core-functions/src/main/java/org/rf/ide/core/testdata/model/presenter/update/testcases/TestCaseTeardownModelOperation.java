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
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTeardown;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestCaseTeardownModelOperation implements IExecutablesStepsHolderElementOperation<TestCase> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.TEST_CASE_TEARDOWN;
    }

    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return elementType == RobotTokenType.TEST_CASE_SETTING_TEARDOWN;
    }

    @Override
    public AModelElement<TestCase> create(final TestCase testCase, final int index, final String settingName,
            final List<String> args, final String comment) {
        final TestCaseTeardown teardown = testCase.newTeardown(index);
        teardown.getDeclaration().setText(settingName);

        if (!args.isEmpty()) {
            teardown.setKeywordName(args.get(0));
            for (int i = 1; i < args.size(); i++) {
                teardown.addArgument(args.get(i));
            }
        }
        if (comment != null && !comment.isEmpty()) {
            teardown.setComment(comment);
        }
        return teardown;
    }

    @Override
    public AModelElement<?> insert(final TestCase testCase, final int index, final AModelElement<?> modelElement) {
        testCase.addElement((TestCaseTeardown) modelElement, index);
        return modelElement;
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        final TestCaseTeardown teardown = (TestCaseTeardown) modelElement;

        if (index == 0) {
            teardown.setKeywordName(value != null ? value : "");
        } else if (index > 0) {
            if (value != null) {
                teardown.setArgument(index - 1, value);
            } else {
                teardown.removeElementToken(index - 1);
            }
        }
    }

    @Override
    public void update(final AModelElement<?> modelElement, final List<String> newArguments) {
        final TestCaseTeardown teardown = (TestCaseTeardown) modelElement;

        teardown.setKeywordName(newArguments.isEmpty() ? null : RobotToken.create(newArguments.get(0)));
        final int elementsToRemove = teardown.getArguments().size();
        for (int i = 0; i < elementsToRemove; i++) {
            teardown.removeElementToken(0);
        }
        for (int i = 1; i < newArguments.size(); i++) {
            teardown.setArgument(i - 1, newArguments.get(i));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(final TestCase testCase, final AModelElement<?> modelElement) {
        testCase.removeElement((AModelElement<TestCase>) modelElement);
    }
}
