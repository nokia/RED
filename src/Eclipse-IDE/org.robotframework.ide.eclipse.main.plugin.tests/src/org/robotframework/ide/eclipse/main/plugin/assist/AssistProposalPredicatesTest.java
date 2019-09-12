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

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibraryArgumentsVariant;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;

public class AssistProposalPredicatesTest {

    @Test
    public void alwaysTruePredicate_isAlwaysSatisfied() {
        final AssistProposalPredicate<Object> predicate = AssistProposalPredicates.alwaysTrue();

        assertThat(predicate.test(null)).isTrue();
        assertThat(predicate.test(new Object())).isTrue();
        assertThat(predicate.test("abc")).isTrue();
        assertThat(predicate.test(newArrayList())).isTrue();
        assertThat(predicate.test(newArrayList(1, 2, 3))).isTrue();
    }

    @Test
    public void alwaysFalsePredicate_isAlwaysNotSatisfied() {
        final AssistProposalPredicate<Object> predicate = AssistProposalPredicates.alwaysFalse();

        assertThat(predicate.test(null)).isFalse();
        assertThat(predicate.test(new Object())).isFalse();
        assertThat(predicate.test("abc")).isFalse();
        assertThat(predicate.test(newArrayList())).isFalse();
        assertThat(predicate.test(newArrayList(1, 2, 3))).isFalse();
    }

    @Test
    public void whenLibraryIsReferencedOrNotReserved_reservedLibPredicateIsSatisfied() {
        final LibrarySpecification spec1 = new LibrarySpecification();
        spec1.setDescriptor(LibraryDescriptor.ofReferencedLibrary(mock(ReferencedLibrary.class),
                ReferencedLibraryArgumentsVariant.create()));
        spec1.setName("foo");

        final LibrarySpecification spec2 = new LibrarySpecification();
        spec2.setDescriptor(LibraryDescriptor.ofStandardLibrary("build"));
        spec2.setName("build");

        final AssistProposalPredicate<LibrarySpecification> predicate = AssistProposalPredicates
                .reservedLibraryPredicate();

        assertThat(predicate.test(spec1)).isTrue();
        assertThat(predicate.test(spec2)).isTrue();
    }

    @Test
    public void whenLibraryIsStandardOtherThanReserved_reservedLibPredicateIsSatisfied() {
        final LibrarySpecification spec = new LibrarySpecification();
        spec.setDescriptor(LibraryDescriptor.ofStandardLibrary("foo"));
        spec.setName("foo");

        final AssistProposalPredicate<LibrarySpecification> predicate = AssistProposalPredicates
                .reservedLibraryPredicate();

        assertThat(predicate.test(spec)).isTrue();
    }

    @Test
    public void whenLibraryIsStandardReserved_reservedLibPredicateIsNotSatisfied() {
        final LibrarySpecification spec = new LibrarySpecification();
        spec.setDescriptor(LibraryDescriptor.ofStandardLibrary("reserved"));
        spec.setName("reserved");

        final AssistProposalPredicate<LibrarySpecification> predicate = AssistProposalPredicates
                .reservedLibraryPredicate();

        assertThat(predicate.test(spec)).isFalse();
    }

    @Test
    public void whenInSecondCellAndGherkinWordIsGiven_theGherkinWordPredicateIsSatisfied() {
        final AssistProposalPredicate<String> predicate = AssistProposalPredicates.gherkinReservedWordsPredicate(1);

        assertThat(predicate.test("Given")).isTrue();
        assertThat(predicate.test("When")).isTrue();
        assertThat(predicate.test("And")).isTrue();
        assertThat(predicate.test("But")).isTrue();
        assertThat(predicate.test("Then")).isTrue();
    }

    @Test
    public void whenInNonSecondCellAndGherkinWordIsGiven_theGherkinWordPredicateIsNotSatisfied() {
        final AssistProposalPredicate<String> predicate1 = AssistProposalPredicates.gherkinReservedWordsPredicate(0);
        final AssistProposalPredicate<String> predicate2 = AssistProposalPredicates.gherkinReservedWordsPredicate(3);

        for (final AssistProposalPredicate<String> predicate : newArrayList(predicate1, predicate2)) {
            assertThat(predicate.test("Given")).isFalse();
            assertThat(predicate.test("When")).isFalse();
            assertThat(predicate.test("And")).isFalse();
            assertThat(predicate.test("But")).isFalse();
            assertThat(predicate.test("Then")).isFalse();
        }
    }

    @Test
    public void whenThereIsArbitraryWord_theGherkinWordPredicateIsNotSatisfied() {
        final AssistProposalPredicate<String> predicate1 = AssistProposalPredicates.gherkinReservedWordsPredicate(0);
        final AssistProposalPredicate<String> predicate2 = AssistProposalPredicates.gherkinReservedWordsPredicate(1);
        final AssistProposalPredicate<String> predicate3 = AssistProposalPredicates.gherkinReservedWordsPredicate(5);

        for (final AssistProposalPredicate<String> predicate : newArrayList(predicate1, predicate2, predicate3)) {
            assertThat(predicate.test(null)).isFalse();
            assertThat(predicate.test("")).isFalse();
            assertThat(predicate.test("word")).isFalse();
        }
    }

    @Test
    public void whenInSecondCellAndForWordIsGiven_theReservedWordPredicateIsSatisfied() {
        final AssistProposalPredicate<String> predicate = AssistProposalPredicates.forLoopReservedWordsPredicate(1,
                Optional.empty());

        assertThat(predicate.test(":FOR")).isTrue();
    }

    @Test
    public void whenInNonSecondCellAndForWordIsGiven_theReservedWordPredicateIsNotSatisfied() {
        final AssistProposalPredicate<String> predicate1 = AssistProposalPredicates.forLoopReservedWordsPredicate(0,
                Optional.empty());
        final AssistProposalPredicate<String> predicate2 = AssistProposalPredicates.forLoopReservedWordsPredicate(3,
                Optional.empty());

        for (final AssistProposalPredicate<String> predicate : newArrayList(predicate1, predicate2)) {
            assertThat(predicate.test(":FOR")).isFalse();
            assertThat(predicate.test("Given")).isFalse();
            assertThat(predicate.test("When")).isFalse();
            assertThat(predicate.test("And")).isFalse();
            assertThat(predicate.test("But")).isFalse();
            assertThat(predicate.test("Then")).isFalse();
        }
    }

    @Test
    public void whenThereIsNoTokenGiven_theReservedWordPredicateIsNotSatisfiedForArbitraryWords() {
        final AssistProposalPredicate<String> predicate1 = AssistProposalPredicates.forLoopReservedWordsPredicate(0,
                Optional.empty());
        final AssistProposalPredicate<String> predicate2 = AssistProposalPredicates.forLoopReservedWordsPredicate(1,
                Optional.empty());
        final AssistProposalPredicate<String> predicate3 = AssistProposalPredicates.forLoopReservedWordsPredicate(5,
                Optional.empty());

        for (final AssistProposalPredicate<String> predicate : newArrayList(predicate1, predicate2, predicate3)) {
            assertThat(predicate.test(null)).isFalse();
            assertThat(predicate.test("")).isFalse();
            assertThat(predicate.test("word")).isFalse();
        }
    }

    @Test
    public void whenThereIsTokenGivenWithoutFOR_theReservedWordPredicateIsNotSatisfied() {
        final AssistProposalPredicate<String> predicate1 = AssistProposalPredicates.forLoopReservedWordsPredicate(0,
                Optional.of(RobotToken.create("foo")));
        final AssistProposalPredicate<String> predicate2 = AssistProposalPredicates.forLoopReservedWordsPredicate(1,
                Optional.of(RobotToken.create("foo")));
        final AssistProposalPredicate<String> predicate3 = AssistProposalPredicates.forLoopReservedWordsPredicate(5,
                Optional.of(RobotToken.create("foo")));

        for (final AssistProposalPredicate<String> predicate : newArrayList(predicate1, predicate2, predicate3)) {
            assertThat(predicate.test(null)).isFalse();
            assertThat(predicate.test("")).isFalse();
            assertThat(predicate.test("word")).isFalse();
        }
    }

    @Test
    public void whenThereIsTokenGivenWithFORButCellIsAtMostSecond_theReservedWordPredicateIsNotSatisfied() {
        final AssistProposalPredicate<String> predicate1 = AssistProposalPredicates.forLoopReservedWordsPredicate(0,
                Optional.of(RobotToken.create(":FOR", RobotTokenType.FOR_TOKEN)));
        final AssistProposalPredicate<String> predicate2 = AssistProposalPredicates.forLoopReservedWordsPredicate(1,
                Optional.of(RobotToken.create(":FOR", RobotTokenType.FOR_TOKEN)));
        final AssistProposalPredicate<String> predicate3 = AssistProposalPredicates.forLoopReservedWordsPredicate(2,
                Optional.of(RobotToken.create(": FOR", RobotTokenType.FOR_TOKEN)));

        for (final AssistProposalPredicate<String> predicate : newArrayList(predicate1, predicate2, predicate3)) {
            assertThat(predicate.test(null)).isFalse();
            assertThat(predicate.test("")).isFalse();
            assertThat(predicate.test("word")).isFalse();
        }
    }

    @Test
    public void whenThereIsTokenGivenWithFORAndCellIsAtLeastThird_theReservedWordPredicateIsSatisfied() {
        final AssistProposalPredicate<String> predicate1 = AssistProposalPredicates.forLoopReservedWordsPredicate(3,
                Optional.of(RobotToken.create(":FOR", RobotTokenType.FOR_TOKEN)));
        final AssistProposalPredicate<String> predicate2 = AssistProposalPredicates.forLoopReservedWordsPredicate(4,
                Optional.of(RobotToken.create(":FOR", RobotTokenType.FOR_TOKEN)));
        final AssistProposalPredicate<String> predicate3 = AssistProposalPredicates.forLoopReservedWordsPredicate(10,
                Optional.of(RobotToken.create(": FOR", RobotTokenType.FOR_TOKEN)));

        for (final AssistProposalPredicate<String> predicate : newArrayList(predicate1, predicate2, predicate3)) {
            assertThat(predicate.test(null)).isTrue();
            assertThat(predicate.test("")).isTrue();
            assertThat(predicate.test("word")).isTrue();
        }
    }

    @Test
    public void whenGivenWordDoesNotMatchReservedWord_theLibraryAliasPredicateIsNotSatisfied() {
        for (final int cellIndex : ContiguousSet.create(Range.closed(0, 10), DiscreteDomain.integers())) {
            final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                    .libraryAliasReservedWordPredicate(cellIndex, Optional.empty());
            assertThat(predicate.test(null)).isFalse();
            assertThat(predicate.test("")).isFalse();
            assertThat(predicate.test("word")).isFalse();
        }
    }

    @Test
    public void whenThereIsNoTokenGiven_theLibraryAliasPredicateIsNotSatisfied() {
        for (final int cellIndex : ContiguousSet.create(Range.closed(0, 10), DiscreteDomain.integers())) {
            final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                    .libraryAliasReservedWordPredicate(cellIndex, Optional.empty());
            assertThat(predicate.test("WITH NAME")).isFalse();
        }
    }

    @Test
    public void whenThereIsTokenGivenWithoutLibDeclaration_theLibraryAliasPredicateIsNotSatisfied() {
        for (final int cellIndex : ContiguousSet.create(Range.closed(0, 10), DiscreteDomain.integers())) {
            final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                    .libraryAliasReservedWordPredicate(cellIndex, Optional.of(RobotToken.create("Library")));
            assertThat(predicate.test("WITH NAME")).isFalse();
        }
    }

    @Test
    public void whenThereIsTokenGivenWithLibDeclarationAndGivenWordDoesNotMatch_theLibraryAliasPredicateIsNotSatisfied() {
        for (final int cellIndex : ContiguousSet.create(Range.closed(0, 10), DiscreteDomain.integers())) {
            final Optional<RobotToken> firstTokenInLine = Optional
                    .of(RobotToken.create("Library", RobotTokenType.SETTING_LIBRARY_DECLARATION));
            final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                    .libraryAliasReservedWordPredicate(cellIndex, firstTokenInLine);
            assertThat(predicate.test(null)).isFalse();
            assertThat(predicate.test("")).isFalse();
            assertThat(predicate.test("word")).isFalse();
        }
    }

    @Test
    public void whenThereIsTokenGivenWithLibDeclaration_theLibraryAliasPredicateIsSatisfiedOnlyForCertainCells() {
        for (final int cellIndex : ContiguousSet.create(Range.closed(0, 10), DiscreteDomain.integers())) {
            final Optional<RobotToken> firstTokenInLine = Optional
                    .of(RobotToken.create("Library", RobotTokenType.SETTING_LIBRARY_DECLARATION));
            final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                    .libraryAliasReservedWordPredicate(cellIndex, firstTokenInLine);
            if (cellIndex < 2) {
                assertThat(predicate.test("WITH NAME")).isFalse();
            } else {
                assertThat(predicate.test("WITH NAME")).isTrue();
            }
        }
    }

    @Test
    public void whenGivenWordDoesNotMatchReservedWord_theDisabledSettingPredicateIsNotSatisfied() {
        for (final int cellIndex : ContiguousSet.create(Range.closed(0, 10), DiscreteDomain.integers())) {
            final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                    .disableSettingReservedWordPredicate(cellIndex, Optional.empty());
            assertThat(predicate.test(null)).isFalse();
            assertThat(predicate.test("")).isFalse();
            assertThat(predicate.test("word")).isFalse();
        }
    }

    @Test
    public void whenThereIsNoTokenGiven_theDisabledSettingPredicateIsNotSatisfied() {
        for (final int cellIndex : ContiguousSet.create(Range.closed(0, 10), DiscreteDomain.integers())) {
            final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                    .disableSettingReservedWordPredicate(cellIndex, Optional.empty());
            assertThat(predicate.test("NONE")).isFalse();
        }
    }

    @Test
    public void whenThereIsTokenGivenWithoutKeywordBasedSetting_theDisabledSettingPredicateIsNotSatisfied() {
        for (final int cellIndex : ContiguousSet.create(Range.closed(0, 10), DiscreteDomain.integers())) {
            final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                    .disableSettingReservedWordPredicate(cellIndex, Optional.of(RobotToken.create("[Setting]")));
            assertThat(predicate.test("NONE")).isFalse();
        }
    }

    @Test
    public void whenThereIsTokenGivenWithKeywordBasedSettingAndGivenWordDoesNotMatch_theDisabledSettingPredicateIsNotSatisfied() {
        for (final int cellIndex : ContiguousSet.create(Range.closed(0, 10), DiscreteDomain.integers())) {
            for (final RobotTokenType type : EnumSet.of(RobotTokenType.TEST_CASE_SETTING_SETUP,
                    RobotTokenType.TEST_CASE_SETTING_TEARDOWN, RobotTokenType.TEST_CASE_SETTING_TEMPLATE,
                    RobotTokenType.TASK_SETTING_SETUP, RobotTokenType.TASK_SETTING_TEARDOWN,
                    RobotTokenType.TASK_SETTING_TEMPLATE, RobotTokenType.KEYWORD_SETTING_TEARDOWN)) {
                final Optional<RobotToken> firstTokenInLine = Optional.of(RobotToken.create("[Setting]", type));
                final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                        .disableSettingReservedWordPredicate(cellIndex, firstTokenInLine);
                assertThat(predicate.test(null)).isFalse();
                assertThat(predicate.test("")).isFalse();
                assertThat(predicate.test("word")).isFalse();
            }
        }
    }

    @Test
    public void whenThereIsTokenGivenWithKeywordBasedSetting_theDisabledSettingPredicateIsSatisfiedOnlyForCertainCells() {
        for (final int cellIndex : ContiguousSet.create(Range.closed(0, 10), DiscreteDomain.integers())) {
            for (final RobotTokenType type : EnumSet.of(RobotTokenType.TEST_CASE_SETTING_SETUP,
                    RobotTokenType.TEST_CASE_SETTING_TEARDOWN, RobotTokenType.TEST_CASE_SETTING_TEMPLATE,
                    RobotTokenType.TASK_SETTING_SETUP, RobotTokenType.TASK_SETTING_TEARDOWN,
                    RobotTokenType.TASK_SETTING_TEMPLATE, RobotTokenType.KEYWORD_SETTING_TEARDOWN)) {
                final Optional<RobotToken> firstTokenInLine = Optional.of(RobotToken.create("[Setting]", type));
                final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                        .disableSettingReservedWordPredicate(cellIndex, firstTokenInLine);
                if (cellIndex != 2) {
                    assertThat(predicate.test("NONE")).isFalse();
                } else {
                    assertThat(predicate.test("NONE")).isTrue();
                }
            }
        }
    }

    @Test
    public void whenGivenWordDoesNotMatchReservedWord_theDisabledSettingInSettingsPredicateIsNotSatisfied() {
        for (final int cellIndex : ContiguousSet.create(Range.closed(0, 10), DiscreteDomain.integers())) {
            final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                    .disableSettingInSettingsReservedWordPredicate(cellIndex, Optional.empty());
            assertThat(predicate.test(null)).isFalse();
            assertThat(predicate.test("")).isFalse();
            assertThat(predicate.test("word")).isFalse();
        }
    }

    @Test
    public void whenThereIsNoTokenGiven_theDisabledSettingInSettingsPredicateIsNotSatisfied() {
        for (final int cellIndex : ContiguousSet.create(Range.closed(0, 10), DiscreteDomain.integers())) {
            final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                    .disableSettingInSettingsReservedWordPredicate(cellIndex, Optional.empty());
            assertThat(predicate.test("NONE")).isFalse();
        }
    }

    @Test
    public void whenThereIsTokenGivenWithoutKeywordBasedSetting_theDisabledSettingInSettingsPredicateIsNotSatisfied() {
        for (final int cellIndex : ContiguousSet.create(Range.closed(0, 10), DiscreteDomain.integers())) {
            final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                    .disableSettingInSettingsReservedWordPredicate(cellIndex,
                            Optional.of(RobotToken.create("General Setting")));
            assertThat(predicate.test("NONE")).isFalse();
        }
    }

    @Test
    public void whenThereIsTokenGivenWithKeywordBasedSettingAndGivenWordDoesNotMatch_theDisabledSettingInSettingsPredicateIsNotSatisfied() {
        for (final int cellIndex : ContiguousSet.create(Range.closed(0, 10), DiscreteDomain.integers())) {
            for (final RobotTokenType type : EnumSet.of(RobotTokenType.SETTING_SUITE_SETUP_DECLARATION,
                    RobotTokenType.SETTING_SUITE_TEARDOWN_DECLARATION, RobotTokenType.SETTING_TEST_SETUP_DECLARATION,
                    RobotTokenType.SETTING_TEST_TEARDOWN_DECLARATION, RobotTokenType.SETTING_TEST_TEMPLATE_DECLARATION,
                    RobotTokenType.SETTING_TASK_SETUP_DECLARATION, RobotTokenType.SETTING_TASK_TEARDOWN_DECLARATION,
                    RobotTokenType.SETTING_TASK_TEMPLATE_DECLARATION)) {
                final Optional<RobotToken> firstTokenInLine = Optional.of(RobotToken.create("General Setting", type));
                final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                        .disableSettingInSettingsReservedWordPredicate(cellIndex, firstTokenInLine);
                assertThat(predicate.test(null)).isFalse();
                assertThat(predicate.test("")).isFalse();
                assertThat(predicate.test("word")).isFalse();
            }
        }
    }

    @Test
    public void whenThereIsTokenGivenWithKeywordBasedSetting_theDisabledSettingInSettingsPredicateIsSatisfiedOnlyForCertainCells() {
        for (final int cellIndex : ContiguousSet.create(Range.closed(0, 10), DiscreteDomain.integers())) {
            for (final RobotTokenType type : EnumSet.of(RobotTokenType.SETTING_SUITE_SETUP_DECLARATION,
                    RobotTokenType.SETTING_SUITE_TEARDOWN_DECLARATION, RobotTokenType.SETTING_TEST_SETUP_DECLARATION,
                    RobotTokenType.SETTING_TEST_TEARDOWN_DECLARATION, RobotTokenType.SETTING_TEST_TEMPLATE_DECLARATION,
                    RobotTokenType.SETTING_TASK_SETUP_DECLARATION, RobotTokenType.SETTING_TASK_TEARDOWN_DECLARATION,
                    RobotTokenType.SETTING_TASK_TEMPLATE_DECLARATION)) {
                final Optional<RobotToken> firstTokenInLine = Optional.of(RobotToken.create("General Setting", type));
                final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                        .disableSettingInSettingsReservedWordPredicate(cellIndex, firstTokenInLine);
                if (cellIndex != 1) {
                    assertThat(predicate.test("NONE")).isFalse();
                } else {
                    assertThat(predicate.test("NONE")).isTrue();
                }
            }
        }
    }

    @Test
    public void whenNoParticularVariableIsTested_theGlobalVarsPredicateIsSatisfied() {
        final RobotElement element = mock(RobotElement.class);

        final AssistProposalPredicate<String> predicate = AssistProposalPredicates.globalVariablePredicate(element);

        assertThat(predicate.test(null)).isTrue();
        assertThat(predicate.test("")).isTrue();
        assertThat(predicate.test("foo")).isTrue();
        assertThat(predicate.test("${var}")).isTrue();

        verifyZeroInteractions(element);
    }

    @Test
    public void whenTestCaseVariableIsTested_theGlobalVarsPredicateIsOnlySatisfiedForTestCaseOrItsChild() {
        final RobotSuiteFile model = createModel();
        final RobotCase testCase = model.findSection(RobotCasesSection.class).get().getChildren().get(0);

        for (final RobotElement element : Iterables.concat(newArrayList(testCase), testCase.getChildren())) {
            final AssistProposalPredicate<String> predicate = AssistProposalPredicates.globalVariablePredicate(element);
            assertThat(predicate.test("${TEST_NAME}")).isTrue();
            assertThat(predicate.test("@{TEST_TAGS}")).isTrue();
            assertThat(predicate.test("${TEST_DOCUMENTATION")).isTrue();
        }
    }

    @Test
    public void whenTestCaseVariableIsTested_theGlobalVarsPredicateIsNotSatisfiedForAnyElement() {
        final RobotElement anyElement = mock(RobotElement.class);

        final AssistProposalPredicate<String> anyElementPredicate = AssistProposalPredicates
                .globalVariablePredicate(anyElement);
        assertThat(anyElementPredicate.test("${TEST_NAME}")).isFalse();
        assertThat(anyElementPredicate.test("@{TEST_TAGS}")).isFalse();
        assertThat(anyElementPredicate.test("${TEST_DOCUMENTATION}")).isFalse();
    }

    @Test
    public void whenSuiteTeardownVariableIsTested_theGlobalVarsPredicateIsOnlySatisfiedForSuiteTeardown() {
        final RobotSuiteFile model = createModel();
        final RobotKeywordCall suiteTeardown = model.findSection(RobotSettingsSection.class).get().getChildren().get(0);

        final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                .globalVariablePredicate(suiteTeardown);
        assertThat(predicate.test("${SUITE_STATUS}")).isTrue();
        assertThat(predicate.test("${SUITE_MESSAGE}")).isTrue();
    }

    @Test
    public void whenSuiteTeardownVariableIsTested_theGlobalVarsPredicateIsNotSatisfiedForOtherSettingThanSuiteTeardown() {
        final RobotSuiteFile model = createModel();
        final List<RobotKeywordCall> settings = model.findSection(RobotSettingsSection.class).get().getChildren();

        // omiting suite teardown
        for (int i = 1; i < settings.size(); i++) {
            final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                    .globalVariablePredicate(settings.get(i));
            assertThat(predicate.test("${SUITE_STATUS}")).isFalse();
            assertThat(predicate.test("${SUITE_MESSAGE}")).isFalse();
        }
    }

    @Test
    public void whenSuiteTeardownVariableIsTested_theGlobalVarsPredicateIsNotSatisfiedForAnyElement() {
        final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                .globalVariablePredicate(mock(RobotElement.class));
        assertThat(predicate.test("${SUITE_STATUS}")).isFalse();
        assertThat(predicate.test("${SUITE_MESSAGE}")).isFalse();
    }

    @Test
    public void whenKeywordTeardownVariableIsTested_theGlobalVarsPredicateIsOnlySatisfiedForKeywordTeardown() {
        final RobotSuiteFile model = createModel();
        final RobotKeywordCall keywordTeardown = model.findSection(RobotKeywordsSection.class).get().getChildren()
                .get(0)
                .getChildren()
                .get(0);

        final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                .globalVariablePredicate(keywordTeardown);
        assertThat(predicate.test("${KEYWORD_STATUS}")).isTrue();
        assertThat(predicate.test("${KEYWORD_MESSAGE}")).isTrue();
    }

    @Test
    public void whenKeywordTeardownVariableIsTested_theGlobalVarsPredicateIsNotSatisfiedForOtherSettingThanKeywordTeardown() {
        final RobotSuiteFile model = createModel();
        final List<RobotKeywordCall> settings = model.findSection(RobotKeywordsSection.class).get().getChildren()
                .get(0).getChildren();

        // omitting keyword teardown at i = 0
        for (int i = 1; i < settings.size(); i++) {
            final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                    .globalVariablePredicate(settings.get(i));
            assertThat(predicate.test("${KEYWORD_STATUS}")).isFalse();
            assertThat(predicate.test("${KEYWORD_MESSAGE}")).isFalse();
        }
    }

    @Test
    public void whenKeywordTeardownVariableIsTested_theGlobalVarsPredicateIsNotSatisfiedForAnyElement() {
        final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                .globalVariablePredicate(mock(RobotElement.class));
        assertThat(predicate.test("${KEYWORD_STATUS}")).isFalse();
        assertThat(predicate.test("${KEYWORD_MESSAGE}")).isFalse();
    }

    @Test
    public void whenTestTeardownVariableIsTested_theGlobalVarsPredicateIsSatisfiedForGeneralTestTeardown() {
        final RobotSuiteFile model = createModel();
        final RobotKeywordCall generalTestTeardown = model.findSection(RobotSettingsSection.class)
                .get().getChildren().get(2);

        final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                .globalVariablePredicate(generalTestTeardown);
        assertThat(predicate.test("${TEST_STATUS}")).isTrue();
        assertThat(predicate.test("${TEST_MESSAGE}")).isTrue();
    }

    @Test
    public void whenTestTeardownVariableIsTested_theGlobalVarsPredicateIsSatisfiedForLocalTestTeardown() {
        final RobotSuiteFile model = createModel();
        final RobotKeywordCall testTeardown = model.findSection(RobotCasesSection.class)
                .get().getChildren().get(0).getChildren().get(1);

        final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                .globalVariablePredicate(testTeardown);
        assertThat(predicate.test("${TEST_STATUS}")).isTrue();
        assertThat(predicate.test("${TEST_MESSAGE}")).isTrue();
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
                assertThat(predicate.test("${TEST_STATUS}")).isFalse();
                assertThat(predicate.test("${TEST_MESSAGE}")).isFalse();
            }
        }
    }

    @Test
    public void whenTestTeardownVariableIsTested_theGlobalVarsPredicateIsNotSatisfiedForAnyElement() {
        final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                .globalVariablePredicate(mock(RobotElement.class));
        assertThat(predicate.test("${TEST_STATUS}")).isFalse();
        assertThat(predicate.test("${TEST_MESSAGE}")).isFalse();
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
