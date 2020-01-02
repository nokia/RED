/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.red.nattable.edit.CellEditorValueValidator.CellEditorValueValidationException;

/**
 * @author wypych
 */
public class VariableNameRedCellEditorValidatorTest {

    private VariableNameRedCellEditorValidator testable;

    @Before
    public void setUp() {
        this.testable = new VariableNameRedCellEditorValidator(mock(IRowDataProvider.class));
    }

    @Test
    public void test_logicExecution_shouldNotBeAnyErrorThrown() {
        // given
        final String varName = "${var}";

        // when
        testable.validate(varName, 0);
    }

    @Test
    public void test_logicExecution_shouldBeSimpleErrorThrown_fromSuperClass() {
        // given
        final String varName = "  d";

        // when then
        assertThatExceptionOfType(CellEditorValueValidationException.class)
                .isThrownBy(() -> testable.validate(varName, 0));
    }

    @Test
    public void test_logicExecution_shouldBeSimpleErrorThrown_fromCurrentClass() {
        // given
        final String varName = "";

        // when then
        assertThatExceptionOfType(CellEditorValueValidationException.class)
                .isThrownBy(() -> testable.validate(varName, 0));
    }

    @Test
    public void checkProblemsWith_variableNameIsCorrect_shouldNotBe_anyMsgReturn() {
        // given
        final String varName = "${a}";

        // when
        final Optional<String> msg = testable.getProblemsWithVariableName(varName);

        // then
        assertThat(msg.isPresent()).isFalse();
    }

    @Test
    public void checkProblemsWith_variableNameContainsMoreThanTwoBrackets_shouldBe_properMsgReturn() {
        // given
        final String varName = "${a}}";

        // when
        final Optional<String> msg = testable.getProblemsWithVariableName(varName);

        // then
        assertThat(msg.isPresent()).isTrue();
        assertThat(msg.get()).isEqualTo("Name should match with [$@&]{name}");
    }

    @Test
    public void checkProblemsWith_variableNameNotEndsWithBracket_shouldBe_properMsgReturn() {
        // given
        final String varName = "${aa";

        // when
        final Optional<String> msg = testable.getProblemsWithVariableName(varName);

        // then
        assertThat(msg.isPresent()).isTrue();
        assertThat(msg.get()).isEqualTo("Name should end with }");
    }

    @Test
    public void checkProblemsWith_variableNameNotStartsWithBracket_shouldBe_properMsgReturn() {
        // given
        final String varName = " {a}";

        // when
        final Optional<String> msg = testable.getProblemsWithVariableName(varName);

        // then
        assertThat(msg.isPresent()).isTrue();
        assertThat(msg.get()).isEqualTo("Name should start with one of [$@&] followed by {");
    }

    @Test
    public void checkProblemsWith_lessCharacterThan_isExpected_shouldBe_properMsgReturn() {
        // given
        final String varName = "";

        // when
        final Optional<String> msg = testable.getProblemsWithVariableName(varName);

        // then
        assertThat(msg.isPresent()).isTrue();
        assertThat(msg.get()).isEqualTo("Name should match with [$@&]{name}");
    }
}
