/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.testdata.model.presenter.update.testcases;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ExecutablesStepsHolderMorphOperation;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTags;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTags;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


public class UserKeywordTagsMorphOperation extends ExecutablesStepsHolderMorphOperation<TestCase> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.USER_KEYWORD_TAGS;
    }

    @Override
    public TestCaseTags insert(final TestCase testCase, final int index, final AModelElement<?> modelElement) {
        final KeywordTags kwTags = (KeywordTags) modelElement;
        
        final TestCaseTags caseTags = testCase.newTags(index);
        caseTags.getDeclaration().setText(kwTags.getDeclaration().getText());

        for (final RobotToken tag : kwTags.getTags()) {
            caseTags.addTag(tag.getText());
        }
        for (final RobotToken comment : kwTags.getComment()) {
            caseTags.addCommentPart(comment);
        }
        return caseTags;
    }
}
