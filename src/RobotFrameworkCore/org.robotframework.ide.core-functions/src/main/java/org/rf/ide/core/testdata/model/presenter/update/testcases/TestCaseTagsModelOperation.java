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
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTags;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestCaseTagsModelOperation implements ITestCaseTableElementOperation {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.TEST_CASE_TAGS;
    }
    
    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return elementType == RobotTokenType.TEST_CASE_SETTING_TAGS;
    }

    @Override
    public AModelElement<TestCase> create(final TestCase testCase, final List<String> args, final String comment) {
        final TestCaseTags tags = testCase.newTags();
        for (final String tag : args) {
            tags.addTag(tag);
        }
        if (comment != null && !comment.isEmpty()) {
            tags.setComment(comment);
        }
        return tags;
    }
    
    @Override
    public void update(final AModelElement<TestCase> modelElement, final int index, final String value) {
        final TestCaseTags tags = (TestCaseTags) modelElement;
        if (value != null) {
            tags.setTag(index, value);
        } else {
            tags.removeElementToken(index);
        }
    }
}
