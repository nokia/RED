/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.testcases;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ITestCaseTableElementOperation;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTeardown;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestCaseTeardownModelOperation implements ITestCaseTableElementOperation {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.TEST_CASE_TEARDOWN;
    }
    
    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return elementType == RobotTokenType.TEST_CASE_SETTING_TEARDOWN;
    }

    @Override
    public AModelElement<?> create(final TestCase testCase, final String settingName, final List<String> args,
            final String comment) {
        final TestCaseTeardown teardown = testCase.newTeardown();
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
    public void insert(final TestCase testCase, final int index, final AModelElement<?> modelElement) {
        testCase.addTeardown(0, (TestCaseTeardown) modelElement);
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
    public void update(final AModelElement<?> modelElement, final List<String> newValues) {
        final TestCaseTeardown teardown = (TestCaseTeardown) modelElement;

        teardown.setKeywordName(newValues.isEmpty() ? "" : newValues.get(0));
        for (int i = 0; i < teardown.getArguments().size(); i++) {
            teardown.removeElementToken(0);
        }
        for (int i = 1; i < newValues.size(); i++) {
            teardown.setArgument(i - 1, newValues.get(i));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(final TestCase testCase, final AModelElement<?> modelElement) {
        testCase.removeUnitSettings((AModelElement<TestCase>) modelElement);
    }
}
