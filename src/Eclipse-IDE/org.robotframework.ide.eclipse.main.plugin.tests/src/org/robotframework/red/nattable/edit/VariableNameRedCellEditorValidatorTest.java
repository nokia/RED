/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.robotframework.red.nattable.edit.CellEditorValueValidator.CellEditorValueValidationException;

import com.google.common.base.Optional;

/**
 * @author wypych
 */
public class VariableNameRedCellEditorValidatorTest {

    private VariableNameRedCellEditorValidator testable;

    private String identificator;

    @Before
    public void setUp() {
        final VariableType varType = VariableType.SCALAR;
        this.testable = new VariableNameRedCellEditorValidator(varType);
        this.identificator = varType.getIdentificator();
    }

    @Test
    public void test_logicExecution_shouldNotBeAnyErrorThrown() {
        // given
        final String varName = identificator + "{var}";

        // when
        testable.validate(varName);
    }

    @Test(expected = CellEditorValueValidationException.class)
    public void test_logicExecution_shouldBeSimpleErrorThrown_fromSuperClass() {
        // given
        final String varName = "  d";

        // when
        testable.validate(varName);
    }

    @Test(expected = CellEditorValueValidationException.class)
    public void test_logicExecution_shouldBeSimpleErrorThrown_fromCurrentClass() {
        // given
        final String varName = "";

        // when
        testable.validate(varName);
    }

    @Test
    public void checkProblemsWith_variableNameIsCorrect_shouldNotBe_anyMsgReturn() {
        // given
        final String varName = identificator + "{a}";

        // when
        Optional<String> msg = testable.getProblemsWithVariableName(varName);

        // then
        assertThat(msg.isPresent()).isFalse();
    }

    @Test
    public void checkProblemsWith_variableNameContainsMoreThanTwoBrackets_shouldBe_properMsgReturn() {
        // given
        final String varName = identificator + "{a}}";

        // when
        Optional<String> msg = testable.getProblemsWithVariableName(varName);

        // then
        assertThat(msg.isPresent()).isTrue();
        assertThat(msg.get()).isEqualTo("Incorrect variable name it should be in syntax " + identificator + "{name} .");
    }

    @Test
    public void checkProblemsWith_variableNameNotEndsWithBracket_shouldBe_properMsgReturn() {
        // given
        final String varName = identificator + "{aa";

        // when
        Optional<String> msg = testable.getProblemsWithVariableName(varName);

        // then
        assertThat(msg.isPresent()).isTrue();
        assertThat(msg.get()).isEqualTo("Expected to ends variable with } .");
    }

    @Test
    public void checkProblemsWith_variableNameNotStartsWithBracket_shouldBe_properMsgReturn() {
        // given
        final String varName = identificator + " {a}";

        // when
        Optional<String> msg = testable.getProblemsWithVariableName(varName);

        // then
        assertThat(msg.isPresent()).isTrue();
        assertThat(msg.get()).isEqualTo("Expected to start variable with " + identificator + "{ .");
    }

    @Test
    public void checkProblemsWith_lessCharacterThan_isExpected_shouldBe_properMsgReturn() {
        // given
        final String varName = "";

        // when
        Optional<String> msg = testable.getProblemsWithVariableName(varName);

        // then
        assertThat(msg.isPresent()).isTrue();
        assertThat(msg.get()).isEqualTo("Is not variable syntax " + identificator + "{[name]}.");
    }
}
