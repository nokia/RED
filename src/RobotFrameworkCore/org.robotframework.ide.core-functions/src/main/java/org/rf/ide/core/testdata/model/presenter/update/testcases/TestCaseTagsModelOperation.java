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
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTags;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestCaseTagsModelOperation implements IExecutablesStepsHolderElementOperation<TestCase> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.TEST_CASE_TAGS;
    }

    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return elementType == RobotTokenType.TEST_CASE_SETTING_TAGS_DECLARATION;
    }

    @Override
    public AModelElement<TestCase> create(final TestCase testCase, final int index, final String settingName,
            final List<String> args, final String comment) {
        final TestCaseTags tags = testCase.newTags(index);
        tags.getDeclaration().setText(settingName);

        for (final String tag : args) {
            tags.addTag(tag);
        }
        if (comment != null && !comment.isEmpty()) {
            tags.setComment(comment);
        }
        return tags;
    }

    @Override
    public AModelElement<?> insert(final TestCase testCase, final int index, final AModelElement<?> modelElement) {
        testCase.addElement((TestCaseTags) modelElement, index);
        return modelElement;
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        final TestCaseTags tags = (TestCaseTags) modelElement;
        if (value != null) {
            tags.setTag(index, value);
        } else {
            tags.removeElementToken(index);
        }
    }

    @Override
    public void update(final AModelElement<?> modelElement, final List<String> newArguments) {
        final TestCaseTags tags = (TestCaseTags) modelElement;

        final int elementsToRemove = tags.getTags().size();
        for (int i = 0; i < elementsToRemove; i++) {
            tags.removeElementToken(0);
        }
        for (int i = 0; i < newArguments.size(); i++) {
            tags.setTag(i, newArguments.get(i));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(final TestCase testCase, final AModelElement<?> modelElement) {
        testCase.removeElement((AModelElement<TestCase>) modelElement);
    }
}
