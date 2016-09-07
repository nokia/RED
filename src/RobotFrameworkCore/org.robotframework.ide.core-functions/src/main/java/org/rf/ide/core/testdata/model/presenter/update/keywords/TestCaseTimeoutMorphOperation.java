/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.keywords;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ExecutablesStepsHolderMorphOperation;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTimeout;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTimeout;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


public class TestCaseTimeoutMorphOperation extends ExecutablesStepsHolderMorphOperation<UserKeyword> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.TEST_CASE_TIMEOUT;
    }

    @Override
    public AModelElement<?> insert(final UserKeyword keyword, final int index,
            final AModelElement<?> modelElement) {
        final TestCaseTimeout caseTimeout = (TestCaseTimeout) modelElement;

        final KeywordTimeout kwTimeout = keyword.newTimeout();
        kwTimeout.setTimeout(caseTimeout.getTimeout().getText());

        for (final RobotToken msg : caseTimeout.getMessage()) {
            kwTimeout.addMessagePart(msg);
        }
        for (final RobotToken comment : caseTimeout.getComment()) {
            kwTimeout.addCommentPart(comment);
        }
        return kwTimeout;
    }

}
