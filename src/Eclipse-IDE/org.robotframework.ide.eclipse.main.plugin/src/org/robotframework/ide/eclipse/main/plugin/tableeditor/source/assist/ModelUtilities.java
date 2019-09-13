/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

class ModelUtilities {

    static boolean isTemplateLocalSetting(final RobotSuiteFile model, final int offset) {
        return firstTokenInLineContains(model, offset, RobotTokenType.TEST_CASE_SETTING_TEMPLATE,
                RobotTokenType.TASK_SETTING_TEMPLATE);
    }

    static boolean isKeywordBasedGeneralSetting(final RobotSuiteFile model, final int offset) {
        return firstTokenInLineContains(model, offset, RobotTokenType.SETTING_SUITE_SETUP_DECLARATION,
                RobotTokenType.SETTING_SUITE_TEARDOWN_DECLARATION, RobotTokenType.SETTING_TEST_SETUP_DECLARATION,
                RobotTokenType.SETTING_TEST_TEARDOWN_DECLARATION, RobotTokenType.SETTING_TEST_TEMPLATE_DECLARATION,
                RobotTokenType.SETTING_TASK_SETUP_DECLARATION, RobotTokenType.SETTING_TASK_TEARDOWN_DECLARATION,
                RobotTokenType.SETTING_TASK_TEMPLATE_DECLARATION);
    }

    static boolean isTemplateGeneralSetting(final RobotSuiteFile model, final int offset) {
        return firstTokenInLineContains(model, offset, RobotTokenType.SETTING_TEST_TEMPLATE_DECLARATION,
                RobotTokenType.SETTING_TASK_TEMPLATE_DECLARATION);
    }

    static boolean firstTokenInLineContains(final RobotSuiteFile model, final int offset,
            final RobotTokenType... types) {
        final List<IRobotTokenType> firstTokenInLineTypes = getFirstTokenInLineTypes(model, offset);
        for (final IRobotTokenType type : types) {
            if (firstTokenInLineTypes.contains(type)) {
                return true;
            }
        }
        return false;
    }

    private static List<IRobotTokenType> getFirstTokenInLineTypes(final RobotSuiteFile model, final int offset) {
        return model.getLinkedElement()
                .getRobotLineBy(offset)
                .map(RobotLine::getLineTokens)
                .flatMap(x -> x.stream().findFirst())
                .map(RobotToken::getTypes)
                .orElseGet(ArrayList::new);
    }
}
