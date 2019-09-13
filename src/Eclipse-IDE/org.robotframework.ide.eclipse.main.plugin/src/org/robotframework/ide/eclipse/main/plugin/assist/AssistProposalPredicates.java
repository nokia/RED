/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Sets.newHashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;

public class AssistProposalPredicates {

    public static <T> AssistProposalPredicate<T> alwaysTrue() {
        return any -> true;
    }

    public static <T> AssistProposalPredicate<T> alwaysFalse() {
        return any -> false;
    }

    public static AssistProposalPredicate<String> globalVariablePredicate(final RobotElement element) {
        return globalVarName -> {

            if (newHashSet("${TEST_NAME}", "@{TEST_TAGS}", "${TEST_DOCUMENTATION}").contains(globalVarName)) {
                // those are only available inside test case
                return element instanceof RobotCase || element.getParent() instanceof RobotCase;

            } else if (newHashSet("${SUITE_STATUS}", "${SUITE_MESSAGE}").contains(globalVarName)) {
                // those are only available for suite teardown
                return element instanceof RobotKeywordCall
                        && ((RobotKeywordCall) element).getLinkedElement().getModelType() == ModelType.SUITE_TEARDOWN;

            } else if (newHashSet("${TEST_STATUS}", "${TEST_MESSAGE}").contains(globalVarName)) {
                // those are only available for test teardown
                return element instanceof RobotKeywordCall && (((RobotKeywordCall) element).getLinkedElement()
                        .getModelType() == ModelType.TEST_CASE_TEARDOWN
                        || ((RobotKeywordCall) element).getLinkedElement()
                                .getModelType() == ModelType.SUITE_TEST_TEARDOWN);

            } else if (newHashSet("${KEYWORD_STATUS}", "${KEYWORD_MESSAGE}").contains(globalVarName)) {
                // those are only available for keyword teardown
                return element instanceof RobotKeywordCall && ((RobotKeywordCall) element).getLinkedElement()
                        .getModelType() == ModelType.USER_KEYWORD_TEARDOWN;
            }
            return true;
        };
    }

    public static AssistProposalPredicate<LibrarySpecification> reservedLibraryPredicate() {
        return spec -> spec.getDescriptor().isReferencedLibrary() || !spec.getName().equalsIgnoreCase("reserved");
    }

    public static AssistProposalPredicate<String> gherkinReservedWordsPredicate(final int cellIndex) {
        return reservedWord -> cellIndex == 1 && GherkinReservedWordProposals.GHERKIN_ELEMENTS.contains(reservedWord);
    }

    public static AssistProposalPredicate<String> forLoopReservedWordsPredicate(final int cellIndex,
            final Optional<RobotToken> firstTokenInLine) {
        return reservedWord -> {
            if (ForLoopReservedWordsProposals.FOR_LOOP_1.equals(reservedWord)
                    || ForLoopReservedWordsProposals.NEW_FOR_LOOP_LITERALS.contains(reservedWord)) {
                // we're in 2nd cell
                return cellIndex == 1;

            } else {
                // line starts with :FOR and we're in at least 4th cell
                return cellIndex >= 3 && firstTokenInLine.isPresent()
                        && firstTokenInLine.get().getTypes().contains(RobotTokenType.FOR_TOKEN);
            }
        };
    }

    public static AssistProposalPredicate<String> libraryAliasReservedWordPredicate(final int cellIndex,
            final Optional<RobotToken> firstTokenInLine) {
        return reservedWord -> cellIndex >= 2 && LibraryAliasReservedWordProposals.WITH_NAME.equals(reservedWord)
                && firstTokenInLine.isPresent()
                && firstTokenInLine.get().getTypes().contains(RobotTokenType.SETTING_LIBRARY_DECLARATION);
    }

    public static AssistProposalPredicate<String> disableSettingReservedWordPredicate(final int cellIndex,
            final Optional<RobotToken> firstTokenInLine) {
        return disableSettingReservedWordPredicate(cellIndex,
                firstTokenInLine.map(RobotToken::getTypes).orElseGet(ArrayList::new));
    }

    public static AssistProposalPredicate<String> disableSettingReservedWordPredicate(final int cellIndex,
            final List<IRobotTokenType> firstTokenInLineTypes) {
        return reservedWord -> {
            if (cellIndex == 2 && DisableSettingReservedWordProposals.NONE.equals(reservedWord)) {
                return firstTokenInLineTypes.contains(RobotTokenType.TEST_CASE_SETTING_SETUP)
                        || firstTokenInLineTypes.contains(RobotTokenType.TEST_CASE_SETTING_TEARDOWN)
                        || firstTokenInLineTypes.contains(RobotTokenType.TEST_CASE_SETTING_TEMPLATE)
                        || firstTokenInLineTypes.contains(RobotTokenType.TEST_CASE_SETTING_TIMEOUT)
                        || firstTokenInLineTypes.contains(RobotTokenType.TEST_CASE_SETTING_TAGS_DECLARATION)
                        || firstTokenInLineTypes.contains(RobotTokenType.TASK_SETTING_SETUP)
                        || firstTokenInLineTypes.contains(RobotTokenType.TASK_SETTING_TEARDOWN)
                        || firstTokenInLineTypes.contains(RobotTokenType.TASK_SETTING_TEMPLATE)
                        || firstTokenInLineTypes.contains(RobotTokenType.TASK_SETTING_TIMEOUT)
                        || firstTokenInLineTypes.contains(RobotTokenType.TASK_SETTING_TAGS_DECLARATION)
                        || firstTokenInLineTypes.contains(RobotTokenType.KEYWORD_SETTING_TEARDOWN)
                        || firstTokenInLineTypes.contains(RobotTokenType.KEYWORD_SETTING_TIMEOUT)
                        || firstTokenInLineTypes.contains(RobotTokenType.KEYWORD_SETTING_TAGS);
            }
            return false;
        };
    }

    public static AssistProposalPredicate<String> disableSettingInSettingsReservedWordPredicate(final int cellIndex,
            final Optional<RobotToken> firstTokenInLine) {
        return disableSettingInSettingsReservedWordPredicate(cellIndex,
                firstTokenInLine.map(RobotToken::getTypes).orElseGet(ArrayList::new));
    }

    public static AssistProposalPredicate<String> disableSettingInSettingsReservedWordPredicate(final int cellIndex,
            final List<IRobotTokenType> firstTokenInLineTypes) {
        return reservedWord -> {
            if (cellIndex == 1 && DisableSettingReservedWordProposals.NONE.equals(reservedWord)) {
                return firstTokenInLineTypes.contains(RobotTokenType.SETTING_SUITE_SETUP_DECLARATION)
                        || firstTokenInLineTypes.contains(RobotTokenType.SETTING_SUITE_TEARDOWN_DECLARATION)
                        || firstTokenInLineTypes.contains(RobotTokenType.SETTING_TEST_SETUP_DECLARATION)
                        || firstTokenInLineTypes.contains(RobotTokenType.SETTING_TEST_TEARDOWN_DECLARATION)
                        || firstTokenInLineTypes.contains(RobotTokenType.SETTING_TEST_TEMPLATE_DECLARATION)
                        || firstTokenInLineTypes.contains(RobotTokenType.SETTING_TEST_TIMEOUT_DECLARATION)
                        || firstTokenInLineTypes.contains(RobotTokenType.SETTING_TASK_SETUP_DECLARATION)
                        || firstTokenInLineTypes.contains(RobotTokenType.SETTING_TASK_TEARDOWN_DECLARATION)
                        || firstTokenInLineTypes.contains(RobotTokenType.SETTING_TASK_TEMPLATE_DECLARATION)
                        || firstTokenInLineTypes.contains(RobotTokenType.SETTING_TASK_TIMEOUT_DECLARATION);
            }
            return false;
        };
    }
}
