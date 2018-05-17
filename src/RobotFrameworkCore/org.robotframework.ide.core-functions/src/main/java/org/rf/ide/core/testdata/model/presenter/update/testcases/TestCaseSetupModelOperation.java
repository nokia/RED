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
import org.rf.ide.core.testdata.model.table.testcases.TestCaseSetup;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestCaseSetupModelOperation implements IExecutablesStepsHolderElementOperation<TestCase> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.TEST_CASE_SETUP;
    }

    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return elementType == RobotTokenType.TEST_CASE_SETTING_SETUP;
    }

    @Override
    public AModelElement<TestCase> create(final TestCase testCase, final int index, final String settingName,
            final List<String> args, final String comment) {
        final TestCaseSetup setup = testCase.newSetup(index);
        setup.getDeclaration().setText(settingName);

        if (!args.isEmpty()) {
            setup.setKeywordName(args.get(0));
            for (int i = 1; i < args.size(); i++) {
                setup.addArgument(args.get(i));
            }
        }
        if (comment != null && !comment.isEmpty()) {
            setup.setComment(comment);
        }
        return setup;
    }

    @Override
    public AModelElement<?> insert(final TestCase testCase, final int index, final AModelElement<?> modelElement) {
        testCase.addElement((TestCaseSetup) modelElement, index);
        return modelElement;
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        final TestCaseSetup setup = (TestCaseSetup) modelElement;
        if (index == 0) {
            setup.setKeywordName(value != null ? value : "");
        } else if (index > 0) {
            if (value != null) {
                setup.setArgument(index - 1, value);
            } else {
                setup.removeElementToken(index - 1);
            }
        }
    }

    @Override
    public void update(final AModelElement<?> modelElement, final List<String> newArguments) {
        final TestCaseSetup setup = (TestCaseSetup) modelElement;

        setup.setKeywordName(newArguments.isEmpty() ? null : RobotToken.create(newArguments.get(0)));
        final int elementsToRemove = setup.getArguments().size();
        for (int i = 0; i < elementsToRemove; i++) {
            setup.removeElementToken(0);
        }
        for (int i = 1; i < newArguments.size(); i++) {
            setup.setArgument(i - 1, newArguments.get(i));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(final TestCase testCase, final AModelElement<?> modelElement) {
        testCase.removeElement((AModelElement<TestCase>) modelElement);
    }
}
