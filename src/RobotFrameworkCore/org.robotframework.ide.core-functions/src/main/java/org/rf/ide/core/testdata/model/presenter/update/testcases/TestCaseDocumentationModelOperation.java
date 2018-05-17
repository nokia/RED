/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.testcases;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.DocumentationServiceHandler;
import org.rf.ide.core.testdata.model.presenter.update.IExecutablesStepsHolderElementOperation;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestDocumentation;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestCaseDocumentationModelOperation implements IExecutablesStepsHolderElementOperation<TestCase> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.TEST_CASE_DOCUMENTATION;
    }

    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return elementType == RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION;
    }

    @Override
    public AModelElement<TestCase> create(final TestCase testCase, final int index, final String settingName,
            final List<String> args, final String comment) {
        final TestDocumentation testDoc = testCase.newDocumentation(index);
        testDoc.getDeclaration().setText(settingName);

        for (int i = 0; i < args.size(); i++) {
            testDoc.addDocumentationText(i, args.get(i));
        }
        if (comment != null && !comment.isEmpty()) {
            testDoc.setComment(comment);
        }
        return testDoc;
    }

    @Override
    public AModelElement<?> insert(final TestCase testCase, final int index, final AModelElement<?> modelElement) {
        testCase.addDocumentation(index, (TestDocumentation) modelElement);
        return modelElement;
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        final TestDocumentation testDoc = (TestDocumentation) modelElement;
        if (value != null) {
            if (index == 0) {
                DocumentationServiceHandler.update(testDoc, value);
            }
        } else {
            testDoc.clearDocumentation();
        }
    }

    @Override
    public void update(final AModelElement<?> modelElement, final List<String> newArguments) {
        final TestDocumentation testDoc = (TestDocumentation) modelElement;

        if (newArguments.isEmpty()) {
            testDoc.clearDocumentation();
        } else {
            DocumentationServiceHandler.update(testDoc, newArguments.get(0));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(final TestCase testCase, final AModelElement<?> modelElement) {
        testCase.removeElement((AModelElement<TestCase>) modelElement);
    }
}
