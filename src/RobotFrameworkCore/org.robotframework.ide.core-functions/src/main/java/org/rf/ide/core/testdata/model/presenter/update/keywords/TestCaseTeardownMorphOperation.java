/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.keywords;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ExecutablesStepsHolderMorphOperation;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTeardown;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTeardown;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


public class TestCaseTeardownMorphOperation extends ExecutablesStepsHolderMorphOperation<UserKeyword> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.TEST_CASE_TEARDOWN;
    }

    @Override
    public AModelElement<?> insert(final UserKeyword keyword, final int index,
            final AModelElement<?> modelElement) {
        final TestCaseTeardown caseTeardown = (TestCaseTeardown) modelElement;

        final KeywordTeardown kwTeardown = keyword.newTeardown();
        kwTeardown.getDeclaration().setText(caseTeardown.getDeclaration().getText());

        kwTeardown.setKeywordName(caseTeardown.getKeywordName());
        for (final RobotToken arg : caseTeardown.getArguments()) {
            kwTeardown.addArgument(arg);
        }
        for (final RobotToken comment : caseTeardown.getComment()) {
            kwTeardown.addCommentPart(comment);
        }
        return kwTeardown;
    }

}
