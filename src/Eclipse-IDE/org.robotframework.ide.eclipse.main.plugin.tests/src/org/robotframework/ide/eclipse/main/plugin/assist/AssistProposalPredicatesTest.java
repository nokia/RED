/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Optional;

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
        final AssistProposalPredicate<String> predicate = AssistProposalPredicates.codeReservedWordsPredicate(1, Optional.<RobotToken> absent());

        assertThat(predicate.apply(":FOR")).isTrue();
        assertThat(predicate.apply("Given")).isTrue();
        assertThat(predicate.apply("When")).isTrue();
        assertThat(predicate.apply("And")).isTrue();
        assertThat(predicate.apply("But")).isTrue();
        assertThat(predicate.apply("Then")).isTrue();
    }

    @Test
    public void whenInNonSecondCellAndForOrGherkingWordIsGiven_theReservedWordPredicateIsNotSatisfied() {
        final AssistProposalPredicate<String> predicate1 = AssistProposalPredicates.codeReservedWordsPredicate(0,
                Optional.<RobotToken> absent());
        final AssistProposalPredicate<String> predicate2 = AssistProposalPredicates.codeReservedWordsPredicate(3,
                Optional.<RobotToken> absent());

        assertThat(predicate1.apply(":FOR")).isFalse();
        assertThat(predicate1.apply("Given")).isFalse();
        assertThat(predicate1.apply("When")).isFalse();
        assertThat(predicate1.apply("And")).isFalse();
        assertThat(predicate1.apply("But")).isFalse();
        assertThat(predicate1.apply("Then")).isFalse();

        assertThat(predicate2.apply(":FOR")).isFalse();
        assertThat(predicate2.apply("Given")).isFalse();
        assertThat(predicate2.apply("When")).isFalse();
        assertThat(predicate2.apply("And")).isFalse();
        assertThat(predicate2.apply("But")).isFalse();
        assertThat(predicate2.apply("Then")).isFalse();
    }

}
