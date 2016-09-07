/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.keywords;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ExecutablesStepsHolderMorphOperation;
import org.rf.ide.core.testdata.model.table.keywords.KeywordArguments;
import org.rf.ide.core.testdata.model.table.keywords.KeywordReturn;
import org.rf.ide.core.testdata.model.table.keywords.KeywordUnknownSettings;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseUnknownSettings;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class TestCaseUnknownSettingMorphOperation extends ExecutablesStepsHolderMorphOperation<UserKeyword> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.TEST_CASE_SETTING_UNKNOWN;
    }

    @Override
    public AModelElement<?> insert(final UserKeyword keyword, final int index,
            final AModelElement<?> modelElement) {
        final TestCaseUnknownSettings tcSetting = (TestCaseUnknownSettings) modelElement;

        final RobotTokenType possibleSettingType = RobotTokenType
                .findTypeOfDeclarationForKeywordSettingTable(tcSetting.getDeclaration().getText());

        if (possibleSettingType == RobotTokenType.KEYWORD_SETTING_ARGUMENTS) {

            final KeywordArguments arguments = keyword.newArguments();
            for (final RobotToken argument : tcSetting.getArguments()) {
                arguments.addArgument(argument);
            }
            for (final RobotToken comment : tcSetting.getComment()) {
                arguments.addCommentPart(comment);
            }
            return arguments;
        } else if (possibleSettingType == RobotTokenType.KEYWORD_SETTING_RETURN) {

            final KeywordReturn returnSetting = keyword.newReturn();
            for (final RobotToken argument : tcSetting.getArguments()) {
                returnSetting.addReturnValue(argument);
            }
            for (final RobotToken comment : tcSetting.getComment()) {
                returnSetting.addCommentPart(comment);
            }
            return returnSetting;
        } else {

            final KeywordUnknownSettings unkownSetting = keyword.newUnknownSettings();
            unkownSetting.getDeclaration().setText(tcSetting.getDeclaration().getText());
            for (final RobotToken arg : tcSetting.getArguments()) {
                unkownSetting.addArgument(arg.getText());
            }
            for (final RobotToken comment : tcSetting.getComment()) {
                unkownSetting.addCommentPart(comment);
            }
            return unkownSetting;
        }
    }

}
