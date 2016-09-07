/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.keywords;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ExecutablesStepsHolderMorphOperation;
import org.rf.ide.core.testdata.model.table.keywords.KeywordUnknownSettings;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseSetup;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


public class TestCaseSetupMorphOperation extends ExecutablesStepsHolderMorphOperation<UserKeyword> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.TEST_CASE_SETUP;
    }

    @Override
    public AModelElement<?> insert(final UserKeyword keyword, final int index,
            final AModelElement<?> modelElement) {
        final TestCaseSetup setup = (TestCaseSetup) modelElement;

        final KeywordUnknownSettings unknownSetting = keyword.newUnknownSettings();
        unknownSetting.getDeclaration().setText(setup.getDeclaration().getText());
        unknownSetting.addArgument(setup.getKeywordName());
        for (final RobotToken arg : setup.getArguments()) {
            unknownSetting.addArgument(arg.getText());
        }
        for (final RobotToken comment : setup.getComment()) {
            unknownSetting.addCommentPart(comment);
        }
        return unknownSetting;
    }

}
