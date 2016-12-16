/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

public class AssistProposalPredicatesTest {

    @Test
    public void alwaysTruePredicate_isAlwaysSatisfied() {
        final AssistProposalPredicate<Object> predicate = AssistProposalPredicates.alwaysTrue();

        assertThat(predicate.apply(null)).isTrue();
        assertThat(predicate.apply(new Object())).isTrue();
        assertThat(predicate.apply("abc")).isTrue();
        assertThat(predicate.apply(newArrayList())).isTrue();
        assertThat(predicate.apply(newArrayList(1, 2, 3))).isTrue();
    }

    @Test
    public void alwaysFalsePredicate_isAlwayNotSatisfied() {
        final AssistProposalPredicate<Object> predicate = AssistProposalPredicates.alwaysFalse();

        assertThat(predicate.apply(null)).isFalse();
        assertThat(predicate.apply(new Object())).isFalse();
        assertThat(predicate.apply("abc")).isFalse();
        assertThat(predicate.apply(newArrayList())).isFalse();
        assertThat(predicate.apply(newArrayList(1, 2, 3))).isFalse();
    }

    @Test
    public void whenFileIsASuite_sectionsPredicateIsSatisfiedForAnyGivenSectionName() {
        final RobotSuiteFile model = mock(RobotSuiteFile.class);
        when(model.isSuiteFile()).thenReturn(true);

        final AssistProposalPredicate<String> predicate = AssistProposalPredicates.testCaseSectionPredicate(model);

        assertThat(predicate.apply(null)).isTrue();
        assertThat(predicate.apply("")).isTrue();
        assertThat(predicate.apply("foo")).isTrue();
        assertThat(predicate.apply("Settings")).isTrue();
        assertThat(predicate.apply("Variables")).isTrue();
        assertThat(predicate.apply("Keywords")).isTrue();
        assertThat(predicate.apply("Test Cases")).isTrue();
    }

    @Test
    public void whenFileIsAResource_sectionsPredicateIsSatisfiedForAnyGivenSectionNameExceptTestCases() {
        final RobotSuiteFile model = mock(RobotSuiteFile.class);
        when(model.isSuiteFile()).thenReturn(false);

        final AssistProposalPredicate<String> predicate = AssistProposalPredicates.testCaseSectionPredicate(model);

        assertThat(predicate.apply(null)).isTrue();
        assertThat(predicate.apply("")).isTrue();
        assertThat(predicate.apply("foo")).isTrue();
        assertThat(predicate.apply("Settings")).isTrue();
        assertThat(predicate.apply("Variables")).isTrue();
        assertThat(predicate.apply("Keywords")).isTrue();
        assertThat(predicate.apply("Test Cases")).isFalse();
    }

    @Test
    public void whenLibraryIsReferenced_reservedLibPredicateIsSatisfied() {
        final LibrarySpecification spec1 = new LibrarySpecification();
        spec1.setReferenced(mock(ReferencedLibrary.class));
        spec1.setName("foo");

        final LibrarySpecification spec2 = new LibrarySpecification();
        spec2.setReferenced(mock(ReferencedLibrary.class));
        spec2.setName("reserved");

        final AssistProposalPredicate<LibrarySpecification> predicate = AssistProposalPredicates
                .reservedLibraryPredicate();

        assertThat(predicate.apply(spec1)).isTrue();
        assertThat(predicate.apply(spec2)).isTrue();
    }

    @Test
    public void whenLibraryIsStandardOtherThanReserved_reservedLibPredicateIsSatisfied() {
        final LibrarySpecification spec = new LibrarySpecification();
        spec.setName("foo");

        final AssistProposalPredicate<LibrarySpecification> predicate = AssistProposalPredicates
                .reservedLibraryPredicate();

        assertThat(predicate.apply(spec)).isTrue();
    }

    @Test
    public void whenLibraryIsStandardReserved_reservedLibPredicateIsNotSatisfied() {
        final LibrarySpecification spec = new LibrarySpecification();
        spec.setName("reserved");

        final AssistProposalPredicate<LibrarySpecification> predicate = AssistProposalPredicates
                .reservedLibraryPredicate();

        assertThat(predicate.apply(spec)).isFalse();
    }

    @Test
    public void whenInSecondCellAndForOrGherkingWordIsGiven_theReservedWordPredicateIsSatisfied() {
        final AssistProposalPredicate<String> predicate = AssistProposalPredicates.codeReservedWordsPredicate(1,
                Optional.<RobotToken> absent());

        assertThat(predicate.apply(":FOR")).isTrue();
        assertThat(predicate.apply("Given")).isTrue();
        assertThat(predicate.apply("When")).isTrue();
        assertThat(predicate.apply("And")).isTrue();
        assertThat(predicate.apply("But")).isTrue();
        assertThat(predicate.apply("Then")).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenInNonSecondCellAndForOrGherkingWordIsGiven_theReservedWordPredicateIsNotSatisfied() {
        final AssistProposalPredicate<String> predicate1 = AssistProposalPredicates.codeReservedWordsPredicate(0,
                Optional.<RobotToken> absent());
        final AssistProposalPredicate<String> predicate2 = AssistProposalPredicates.codeReservedWordsPredicate(3,
                Optional.<RobotToken> absent());

        for (final AssistProposalPredicate<String> predicate : newArrayList(predicate1, predicate2)) {
            assertThat(predicate.apply(":FOR")).isFalse();
            assertThat(predicate.apply("Given")).isFalse();
            assertThat(predicate.apply("When")).isFalse();
            assertThat(predicate.apply("And")).isFalse();
            assertThat(predicate.apply("But")).isFalse();
            assertThat(predicate.apply("Then")).isFalse();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenThereIsNoTokenGiven_theReservedWordPredicateIsNotSatisfiedForArbitraryWords() {
        final AssistProposalPredicate<String> predicate1 = AssistProposalPredicates.codeReservedWordsPredicate(0,
                Optional.<RobotToken> absent());
        final AssistProposalPredicate<String> predicate2 = AssistProposalPredicates.codeReservedWordsPredicate(1,
                Optional.<RobotToken> absent());
        final AssistProposalPredicate<String> predicate3 = AssistProposalPredicates.codeReservedWordsPredicate(5,
                Optional.<RobotToken> absent());

        for (final AssistProposalPredicate<String> predicate : newArrayList(predicate1, predicate2, predicate3)) {
            assertThat(predicate.apply(null)).isFalse();
            assertThat(predicate.apply("")).isFalse();
            assertThat(predicate.apply("word")).isFalse();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenThereIsTokenGivenWithoutFOR_theReservedWordPredicateIsNotSatisfied() {
        final AssistProposalPredicate<String> predicate1 = AssistProposalPredicates.codeReservedWordsPredicate(0,
                Optional.of(RobotToken.create("foo")));
        final AssistProposalPredicate<String> predicate2 = AssistProposalPredicates.codeReservedWordsPredicate(1,
                Optional.of(RobotToken.create("foo")));
        final AssistProposalPredicate<String> predicate3 = AssistProposalPredicates.codeReservedWordsPredicate(5,
                Optional.of(RobotToken.create("foo")));

        for (final AssistProposalPredicate<String> predicate : newArrayList(predicate1, predicate2, predicate3)) {
            assertThat(predicate.apply(null)).isFalse();
            assertThat(predicate.apply("")).isFalse();
            assertThat(predicate.apply("word")).isFalse();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenThereIsTokenGivenWithFORButCellIsAtMostSecond_theReservedWordPredicateIsNotSatisfied() {
        final AssistProposalPredicate<String> predicate1 = AssistProposalPredicates.codeReservedWordsPredicate(0,
                Optional.of(RobotToken.create(":FOR")));
        final AssistProposalPredicate<String> predicate2 = AssistProposalPredicates.codeReservedWordsPredicate(1,
                Optional.of(RobotToken.create(":FOR")));
        final AssistProposalPredicate<String> predicate3 = AssistProposalPredicates.codeReservedWordsPredicate(2,
                Optional.of(RobotToken.create(": FOR")));

        for (final AssistProposalPredicate<String> predicate : newArrayList(predicate1, predicate2, predicate3)) {
            assertThat(predicate.apply(null)).isFalse();
            assertThat(predicate.apply("")).isFalse();
            assertThat(predicate.apply("word")).isFalse();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenThereIsTokenGivenWithFORAndCellIsAtLeastThird_theReservedWordPredicateIsSatisfied() {
        final AssistProposalPredicate<String> predicate1 = AssistProposalPredicates.codeReservedWordsPredicate(3,
                Optional.of(RobotToken.create(":FOR")));
        final AssistProposalPredicate<String> predicate2 = AssistProposalPredicates.codeReservedWordsPredicate(4,
                Optional.of(RobotToken.create(":FOR")));
        final AssistProposalPredicate<String> predicate3 = AssistProposalPredicates.codeReservedWordsPredicate(10,
                Optional.of(RobotToken.create(": FOR")));

        for (final AssistProposalPredicate<String> predicate : newArrayList(predicate1, predicate2, predicate3)) {
            assertThat(predicate.apply(null)).isTrue();
            assertThat(predicate.apply("")).isTrue();
            assertThat(predicate.apply("word")).isTrue();
        }
    }

    @Test
    public void whenNoParticularVariableIsTested_theGlobalVarsPredicateIsSatisfied() {
        final RobotElement element = mock(RobotElement.class);

        final AssistProposalPredicate<String> predicate = AssistProposalPredicates.globalVariablePredicate(element);

        assertThat(predicate.apply(null)).isTrue();
        assertThat(predicate.apply("")).isTrue();
        assertThat(predicate.apply("foo")).isTrue();
        assertThat(predicate.apply("${var}")).isTrue();

        verifyZeroInteractions(element);
    }

    @Test
    public void whenTestCaseVariableIsTested_theGlobalVarsPredicateIsOnlySatisfiedForTestCaseOrItsChild() {
        final RobotSuiteFile model = createModel();
        final RobotCase testCase = model.findSection(RobotCasesSection.class).get().getChildren().get(0);

        for (final RobotElement element : Iterables.concat(newArrayList(testCase), testCase.getChildren())) {
            final AssistProposalPredicate<String> predicate = AssistProposalPredicates.globalVariablePredicate(element);
            assertThat(predicate.apply("${TEST_NAME}")).isTrue();
            assertThat(predicate.apply("@{TEST_TAGS}")).isTrue();
            assertThat(predicate.apply("${TEST_DOCUMENTATION")).isTrue();
        }
    }

    @Test
    public void whenTestCaseVariableIsTested_theGlobalVarsPredicateIsNotSatisfiedForAnyElement() {
        final RobotElement anyElement = mock(RobotElement.class);

        final AssistProposalPredicate<String> anyElementPredicate = AssistProposalPredicates
                .globalVariablePredicate(anyElement);
        assertThat(anyElementPredicate.apply("${TEST_NAME}")).isFalse();
        assertThat(anyElementPredicate.apply("@{TEST_TAGS}")).isFalse();
        assertThat(anyElementPredicate.apply("${TEST_DOCUMENTATION}")).isFalse();
    }

    @Test
    public void whenSuiteTeardownVariableIsTested_theGlobalVarsPredicateIsOnlySatisfiedForSuiteTeardown() {
        final RobotSuiteFile model = createModel();
        final RobotKeywordCall suiteTeardown = model.findSection(RobotSettingsSection.class).get().getChildren().get(0);

        final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                .globalVariablePredicate(suiteTeardown);
        assertThat(predicate.apply("${SUITE_STATUS}")).isTrue();
        assertThat(predicate.apply("${SUITE_MESSAGE}")).isTrue();
    }

    @Test
    public void whenSuiteTeardownVariableIsTested_theGlobalVarsPredicateIsNotSatisfiedForOtherSettingThanSuiteTeardown() {
        final RobotSuiteFile model = createModel();
        final List<RobotKeywordCall> settings = model.findSection(RobotSettingsSection.class).get().getChildren();

        // omiting suite teardown
        for (int i = 1; i < settings.size(); i++) {
            final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                    .globalVariablePredicate(settings.get(i));
            assertThat(predicate.apply("${SUITE_STATUS}")).isFalse();
            assertThat(predicate.apply("${SUITE_MESSAGE}")).isFalse();
        }
    }

    @Test
    public void whenSuiteTeardownVariableIsTested_theGlobalVarsPredicateIsNotSatisfiedForAnyElement() {
        final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                .globalVariablePredicate(mock(RobotElement.class));
        assertThat(predicate.apply("${SUITE_STATUS}")).isFalse();
        assertThat(predicate.apply("${SUITE_MESSAGE}")).isFalse();
    }

    @Test
    public void whenKeywordTeardownVariableIsTested_theGlobalVarsPredicateIsOnlySatisfiedForKeywordTeardown() {
        final RobotSuiteFile model = createModel();
        final RobotKeywordCall keywordTeardown = model.findSection(RobotKeywordsSection.class).get().getChildren()
                .get(0)
                .getChildren()
                .get(1);

        final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                .globalVariablePredicate(keywordTeardown);
        assertThat(predicate.apply("${KEYWORD_STATUS}")).isTrue();
        assertThat(predicate.apply("${KEYWORD_MESSAGE}")).isTrue();
    }

    @Test
    public void whenKeywordTeardownVariableIsTested_theGlobalVarsPredicateIsNotSatisfiedForOtherSettingThanKeywordTeardown() {
        final RobotSuiteFile model = createModel();
        final List<RobotKeywordCall> settings = model.findSection(RobotKeywordsSection.class).get().getChildren()
                .get(0).getChildren();

        for (int i = 0; i < settings.size(); i++) {
            // omiting keyword teardown
            if (i != 1) {
                final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                        .globalVariablePredicate(settings.get(i));
                assertThat(predicate.apply("${KEYWORD_STATUS}")).isFalse();
                assertThat(predicate.apply("${KEYWORD_MESSAGE}")).isFalse();
            }
        }
    }

    @Test
    public void whenKeywordTeardownVariableIsTested_theGlobalVarsPredicateIsNotSatisfiedForAnyElement() {
        final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                .globalVariablePredicate(mock(RobotElement.class));
        assertThat(predicate.apply("${KEYWORD_STATUS}")).isFalse();
        assertThat(predicate.apply("${KEYWORD_MESSAGE}")).isFalse();
    }

    @Test
    public void whenTestTeardownVariableIsTested_theGlobalVarsPredicateIsSatisfiedForGeneralTestTeardown() {
        final RobotSuiteFile model = createModel();
        final RobotKeywordCall generalTestTeardown = model.findSection(RobotSettingsSection.class)
                .get().getChildren().get(2);

        final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                .globalVariablePredicate(generalTestTeardown);
        assertThat(predicate.apply("${TEST_STATUS}")).isTrue();
        assertThat(predicate.apply("${TEST_MESSAGE}")).isTrue();
    }

    @Test
    public void whenTestTeardownVariableIsTested_theGlobalVarsPredicateIsSatisfiedForLocalTestTeardown() {
        final RobotSuiteFile model = createModel();
        final RobotKeywordCall testTeardown = model.findSection(RobotCasesSection.class)
                .get().getChildren().get(0).getChildren().get(1);

        final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                .globalVariablePredicate(testTeardown);
        assertThat(predicate.apply("${TEST_STATUS}")).isTrue();
        assertThat(predicate.apply("${TEST_MESSAGE}")).isTrue();
    }

    @Test
    public void whenTestTeardownVariableIsTested_theGlobalVarsPredicateIsNotSatisfiedForOtherSettingThanTestTeardown() {
        final RobotSuiteFile model = createModel();
        final List<RobotKeywordCall> settings = model.findSection(RobotCasesSection.class).get().getChildren()
                .get(0).getChildren();

        for (int i = 0; i < settings.size(); i++) {
            // omiting test teardown
            if (i != 1) {
                final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                        .globalVariablePredicate(settings.get(i));
                assertThat(predicate.apply("${TEST_STATUS}")).isFalse();
                assertThat(predicate.apply("${TEST_MESSAGE}")).isFalse();
            }
        }
    }

    @Test
    public void whenTestTeardownVariableIsTested_theGlobalVarsPredicateIsNotSatisfiedForAnyElement() {
        final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                .globalVariablePredicate(mock(RobotElement.class));
        assertThat(predicate.apply("${TEST_STATUS}")).isFalse();
        assertThat(predicate.apply("${TEST_MESSAGE}")).isFalse();
    }

    private static RobotSuiteFile createModel() {
        return new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  log  10")
                .appendLine("  [Teardown]  t enter")
                .appendLine("  [Tags]  t1  t2")
                .appendLine("*** Keywords ***")
                .appendLine("kw")
                .appendLine("  [Teardown]   Log  kw exit")
                .appendLine("  [Tags]   t1  t2")
                .appendLine("  Log  keyword")
                .appendLine("*** Settings ***")
                .appendLine("Suite Teardown  Log  exit")
                .appendLine("Suite Setup  Log  enter")
                .appendLine("Test Teardown  Log  test exit")
                .appendLine("Metadata  a")
                .build();
    }
}
