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
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTemplate;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestCaseTemplateModelOperation implements ITestCaseTableElementOperation {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.TEST_CASE_TEMPLATE;
    }
    
    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return elementType == RobotTokenType.TEST_CASE_SETTING_TEMPLATE;
    }
    
    @Override
    public AModelElement<?> create(final TestCase testCase, final String settingName, final List<String> args,
            final String comment) {
        final TestCaseTemplate template = testCase.newTemplate();
        if (!args.isEmpty()) {
            template.setKeywordName(args.get(0));
            for (int i = 1; i < args.size(); i++) {
                template.addUnexpectedTrashArgument(args.get(i));
            }
        }
        if (comment != null && !comment.isEmpty()) {
            template.setComment(comment);
        }
        return template;
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        final TestCaseTemplate template = (TestCaseTemplate) modelElement;
        if (index == 0) {
            template.setKeywordName(value != null ? value : "");
        } else if (index > 0) {
            if (value != null) {
                template.setUnexpectedTrashArguments(index - 1, value);
            } else {
                template.removeElementToken(index - 1);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(final TestCase testCase, final AModelElement<?> modelElement) {
        testCase.removeUnitSettings((AModelElement<TestCase>) modelElement);
    }

    @Override
    public void insert(final TestCase testCase, final int index, final AModelElement<?> modelElement) {
        testCase.addTemplate(0, (TestCaseTemplate) modelElement);
    }
}
