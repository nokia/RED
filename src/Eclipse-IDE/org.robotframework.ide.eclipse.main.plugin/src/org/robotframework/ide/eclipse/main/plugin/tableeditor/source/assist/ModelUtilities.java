/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.rf.ide.core.testdata.model.table.RobotEmptyRow;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTask;

class ModelUtilities {

    private static final EnumSet<RobotTokenType> LOCAL_SETTING_TYPES = EnumSet.of(
            RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION, RobotTokenType.TEST_CASE_SETTING_SETUP,
            RobotTokenType.TEST_CASE_SETTING_TAGS_DECLARATION, RobotTokenType.TEST_CASE_SETTING_TEARDOWN,
            RobotTokenType.TEST_CASE_SETTING_TEMPLATE, RobotTokenType.TEST_CASE_SETTING_TIMEOUT,
            RobotTokenType.TASK_SETTING_DOCUMENTATION, RobotTokenType.TASK_SETTING_SETUP,
            RobotTokenType.TASK_SETTING_TAGS_DECLARATION, RobotTokenType.TASK_SETTING_TEARDOWN,
            RobotTokenType.TASK_SETTING_TEMPLATE, RobotTokenType.TASK_SETTING_TIMEOUT,
            RobotTokenType.KEYWORD_SETTING_ARGUMENTS, RobotTokenType.KEYWORD_SETTING_DOCUMENTATION,
            RobotTokenType.KEYWORD_SETTING_RETURN, RobotTokenType.KEYWORD_SETTING_TAGS,
            RobotTokenType.KEYWORD_SETTING_TEARDOWN, RobotTokenType.KEYWORD_SETTING_TIMEOUT);

    private static final EnumSet<RobotTokenType> KEYWORD_BASED_LOCAL_SETTING_TYPES = EnumSet.of(
            RobotTokenType.TEST_CASE_SETTING_SETUP, RobotTokenType.TEST_CASE_SETTING_TEARDOWN,
            RobotTokenType.TEST_CASE_SETTING_TEMPLATE, RobotTokenType.TASK_SETTING_SETUP,
            RobotTokenType.TASK_SETTING_TEARDOWN, RobotTokenType.TASK_SETTING_TEMPLATE,
            RobotTokenType.KEYWORD_SETTING_TEARDOWN);

    private static final EnumSet<RobotTokenType> TEMPLATE_LOCAL_SETTING_TYPES = EnumSet
            .of(RobotTokenType.TEST_CASE_SETTING_TEMPLATE, RobotTokenType.TASK_SETTING_TEMPLATE);

    private static final EnumSet<RobotTokenType> KEYWORD_BASED_GENERAL_SETTING_TYPES = EnumSet.of(
            RobotTokenType.SETTING_SUITE_SETUP_DECLARATION, RobotTokenType.SETTING_SUITE_TEARDOWN_DECLARATION,
            RobotTokenType.SETTING_TEST_SETUP_DECLARATION, RobotTokenType.SETTING_TEST_TEARDOWN_DECLARATION,
            RobotTokenType.SETTING_TEST_TEMPLATE_DECLARATION, RobotTokenType.SETTING_TASK_SETUP_DECLARATION,
            RobotTokenType.SETTING_TASK_TEARDOWN_DECLARATION, RobotTokenType.SETTING_TASK_TEMPLATE_DECLARATION);

    private static final EnumSet<RobotTokenType> TEMPLATE_GENERAL_SETTING_TYPES = EnumSet
            .of(RobotTokenType.SETTING_TEST_TEMPLATE_DECLARATION, RobotTokenType.SETTING_TASK_TEMPLATE_DECLARATION);

    static boolean isLocalSetting(final RobotSuiteFile model, final int offset) {
        return firstTokenInLineContains(model, offset, LOCAL_SETTING_TYPES);
    }

    static boolean isKeywordBasedLocalSetting(final RobotSuiteFile model, final int offset) {
        return firstTokenInLineContains(model, offset, KEYWORD_BASED_LOCAL_SETTING_TYPES);
    }

    static boolean isTemplateLocalSetting(final RobotSuiteFile model, final int offset) {
        return firstTokenInLineContains(model, offset, TEMPLATE_LOCAL_SETTING_TYPES);
    }

    static boolean isKeywordBasedGeneralSetting(final RobotSuiteFile model, final int offset) {
        return firstTokenInLineContains(model, offset, KEYWORD_BASED_GENERAL_SETTING_TYPES);
    }

    static boolean isTemplateGeneralSetting(final RobotSuiteFile model, final int offset) {
        return firstTokenInLineContains(model, offset, TEMPLATE_GENERAL_SETTING_TYPES);
    }

    static boolean firstTokenInLineContains(final RobotSuiteFile model, final int offset,
            final EnumSet<RobotTokenType> types) {
        final List<IRobotTokenType> firstTokenInLineTypes = getFirstTokenInLineTypes(model, offset);
        return types.stream().anyMatch(firstTokenInLineTypes::contains);
    }

    private static List<IRobotTokenType> getFirstTokenInLineTypes(final RobotSuiteFile model, final int offset) {
        return model.getLinkedElement()
                .getRobotLineBy(offset)
                .map(RobotLine::getLineTokens)
                .flatMap(x -> x.stream().findFirst())
                .map(RobotToken::getTypes)
                .orElseGet(ArrayList::new);
    }

    static boolean isEmptyLine(final RobotSuiteFile model, final int offset) {
        return model.findElement(offset)
                .filter(element -> element instanceof RobotKeywordCall)
                .map(element -> (RobotKeywordCall) element)
                .map(RobotKeywordCall::getLinkedElement)
                .filter(element -> element instanceof RobotEmptyRow)
                .isPresent();
    }

    static Optional<String> getTemplateInUse(final RobotSuiteFile model, final int offset) {
        return model.findElement(offset).flatMap(element -> {
            final Optional<String> template = getTemplateInUse(element);
            return template.isPresent() ? template : getTemplateInUse(element.getParent());
        });
    }

    private static Optional<String> getTemplateInUse(final Object element) {
        if (element instanceof RobotCase) {
            return ((RobotCase) element).getTemplateInUse();
        } else if (element instanceof RobotTask) {
            return ((RobotTask) element).getTemplateInUse();
        } else {
            return Optional.empty();
        }
    }
}
