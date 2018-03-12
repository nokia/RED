/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.testdata.model.presenter.update.testcases;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ExecutablesStepsHolderMorphOperation;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTeardown;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTeardown;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


public class UserKeywordTeardownMorphOperation extends ExecutablesStepsHolderMorphOperation<TestCase> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.USER_KEYWORD_TEARDOWN;
    }

    @Override
    public TestCaseTeardown insert(final TestCase testCase, final int index, final AModelElement<?> modelElement) {
        final KeywordTeardown kwTeardown = (KeywordTeardown) modelElement;
        
        final TestCaseTeardown caseTeardown = testCase.newTeardown(index);
        caseTeardown.getDeclaration().setText(kwTeardown.getDeclaration().getText());

        caseTeardown.setKeywordName(kwTeardown.getKeywordName());
        for (final RobotToken arg : kwTeardown.getArguments()) {
            caseTeardown.addArgument(arg);
        }
        for (final RobotToken comment : kwTeardown.getComment()) {
            caseTeardown.addCommentPart(comment);
        }
        return caseTeardown;
    }
}
