/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.keywords;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ExecutablesStepsHolderMorphOperation;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTags;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTags;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


public class TestCaseTagsMorphOperation extends ExecutablesStepsHolderMorphOperation<UserKeyword> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.TEST_CASE_TAGS;
    }

    @Override
    public AModelElement<?> insert(final UserKeyword keyword, final int index, final AModelElement<?> modelElement) {
        final TestCaseTags caseTags = (TestCaseTags) modelElement;

        final KeywordTags kwTags = keyword.newTags();
        for (final RobotToken tag : caseTags.getTags()) {
            kwTags.addTag(tag.getText());
        }
        for (final RobotToken comment : caseTags.getComment()) {
            kwTags.addCommentPart(comment);
        }
        return kwTags;
    }

}
