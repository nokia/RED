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
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTimeout;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestCaseTimeoutModelOperation implements ITestCaseTableElementOperation {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.TEST_CASE_TIMEOUT;
    }
    
    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return elementType == RobotTokenType.TEST_CASE_SETTING_TIMEOUT;
    }

    @Override
    public AModelElement<?> create(final TestCase testCase, final String settingName, final List<String> args,
            final String comment) {
        final TestCaseTimeout timeout = testCase.newTimeout();
        if (!args.isEmpty()) {
            timeout.setTimeout(args.get(0));
            for (int i = 1; i < args.size(); i++) {
                timeout.addMessagePart(i - 1, args.get(i));
            }
        }
        if (comment != null && !comment.isEmpty()) {
            timeout.setComment(comment);
        }
        return timeout;
    }

    @Override
    public AModelElement<?> insert(final TestCase testCase, final int index, final AModelElement<?> modelElement) {
        testCase.addTimeout(0, (TestCaseTimeout) modelElement);
        return modelElement;
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        final TestCaseTimeout timeout = (TestCaseTimeout) modelElement;

        if (index == 0) {
            timeout.setTimeout(value != null ? value : "");
        } else if (index > 0) {
            if (value != null) {
                timeout.addMessagePart(index - 1, value);
            } else {
                timeout.removeElementToken(index - 1);
            }
        }
    }

    @Override
    public void update(final AModelElement<?> modelElement, final List<String> newValues) {
        final TestCaseTimeout timeout = (TestCaseTimeout) modelElement;

        timeout.setTimeout(newValues.isEmpty() ? "" : newValues.get(0));
        for (int i = 0; i < timeout.getMessage().size(); i++) {
            timeout.removeElementToken(0);
        }
        for (int i = 1; i < newValues.size(); i++) {
            timeout.addMessagePart(i - 1, newValues.get(i));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(final TestCase testCase, final AModelElement<?> modelElement) {
        testCase.removeUnitSettings((AModelElement<TestCase>) modelElement);
    }
}
