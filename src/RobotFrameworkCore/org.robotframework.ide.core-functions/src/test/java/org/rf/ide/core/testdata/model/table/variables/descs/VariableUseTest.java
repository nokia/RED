/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Condition;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.table.variables.descs.VariableUse.VariableUseSyntaxException;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class VariableUseTest {

    private final static RobotVersion VERSION_30 = new RobotVersion(3, 0);

    private final static RobotVersion VERSION_32 = new RobotVersion(3, 2);

    @Nested
    class SyntaxValidationInRfPre32 {

        @Test
        public void syntaxVariablesErrorsTest() {
            assertThatExceptionOfType(VariableUseSyntaxException.class)
                    .isThrownBy(oldVariableSyntaxValidation("${}"))
                    .has(proposalEqualTo("${}"));
            assertThatExceptionOfType(VariableUseSyntaxException.class)
                    .isThrownBy(oldVariableSyntaxValidation("${x{}"))
                    .has(proposalEqualTo("${x}"));
            assertThatExceptionOfType(VariableUseSyntaxException.class)
                    .isThrownBy(oldVariableSyntaxValidation("${{1+2}}"))
                    .has(proposalEqualTo("${1+2}"));
        }

        @Test
        public void syntaxIsFineTest() {
            assertThatCode(oldVariableSyntaxValidation("${x}[0")).doesNotThrowAnyException();

            assertThatCode(oldVariableSyntaxValidation("${1}")).doesNotThrowAnyException();
            assertThatCode(oldVariableSyntaxValidation("${x}")).doesNotThrowAnyException();
            assertThatCode(oldVariableSyntaxValidation("${x[0]}")).doesNotThrowAnyException();
            assertThatCode(oldVariableSyntaxValidation("${1+2}")).doesNotThrowAnyException();
            assertThatCode(oldVariableSyntaxValidation("${1+2}}")).doesNotThrowAnyException();
            assertThatCode(oldVariableSyntaxValidation("${x}[${y}]")).doesNotThrowAnyException();
            assertThatCode(oldVariableSyntaxValidation("${x${y}}")).doesNotThrowAnyException();
        }
    }

    @Nested
    class SyntaxValidationInRf32 {

        @Test
        public void syntaxVariablesErrorsTest() {
            assertThatExceptionOfType(VariableUseSyntaxException.class)
                    .isThrownBy(newVariableSyntaxValidation("${x{}"))
                    .has(proposalEqualTo("${x{}}"));
            assertThatExceptionOfType(VariableUseSyntaxException.class)
                    .isThrownBy(newVariableSyntaxValidation("${x"))
                    .has(proposalEqualTo("${x}"));
            assertThatExceptionOfType(VariableUseSyntaxException.class)
                    .isThrownBy(newVariableSyntaxValidation("${x}[0"))
                    .has(proposalEqualTo("${x}[0]"));
        }

        @Test
        public void syntaxIsFineTest() {
            assertThatCode(newVariableSyntaxValidation("${}")).doesNotThrowAnyException();

            assertThatCode(newVariableSyntaxValidation("${1}")).doesNotThrowAnyException();
            assertThatCode(newVariableSyntaxValidation("${x}")).doesNotThrowAnyException();
            assertThatCode(newVariableSyntaxValidation("${x[0]}")).doesNotThrowAnyException();
            assertThatCode(newVariableSyntaxValidation("${1+2}")).doesNotThrowAnyException();
            assertThatCode(newVariableSyntaxValidation("${1+2}}")).doesNotThrowAnyException();
        }
    }

    private static Condition<VariableUseSyntaxException> proposalEqualTo(final String fixProposal) {
        return new Condition<>(e -> e.getFixedNameProposal().equals(fixProposal),
                "proposal fix should be equal to '%s'", fixProposal);
    }

    private static ThrowingCallable oldVariableSyntaxValidation(final String expression) {
        return VariableUseTest.variableSyntaxValidation(VERSION_30, expression);
    }

    private static ThrowingCallable newVariableSyntaxValidation(final String expression) {
        return VariableUseTest.variableSyntaxValidation(VERSION_32, expression);
    }

    private static ThrowingCallable variableSyntaxValidation(final RobotVersion version, final String expression) {
        return () -> varUse(version, expression).validate();
    }

    private static VariableUse varUse(final RobotVersion version, final String expression) {
        final List<VariableUse> uses = new ArrayList<>();
        VariablesAnalyzer.analyzer(version)
                .visitVariables(RobotToken.create(expression, RobotTokenType.VARIABLE_USAGE), uses::add);
        return uses.get(0);
    }
}
