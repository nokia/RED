/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer.variables;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Pattern;

import org.junit.Test;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;


@SuppressWarnings("PMD.MethodNamingConventions")
public class AVariablesTokenRecognizerTest {

    private static final VariableType TYPE = VariableType.INVALID;


    @Test
    public void test_createVariablePattern() {
        final Pattern p = AVariablesTokenRecognizer
                .createVariablePattern(VariableType.SCALAR);

        assertThat(p.toString()).isEqualTo(
                "[ ]?[" + VariableType.SCALAR.getIdentificator() + "]"
                        + "(\\s*)[{].*([}]$|$)");
    }


    @Test
    public void test_getProduceType() {
        final ATokenRecognizer rec = new DummyTokenRecognizer();

        // execute & verify
        assertThat(rec.getProducedType()).isEqualTo(TYPE.getType());
    }


    @Test
    public void test_newInstance_shouldReturn_twoDifferentInstances() {
        final ATokenRecognizer rec = new DummyTokenRecognizer();

        assertThat(rec.newInstance()).isNotSameAs(rec.newInstance());
    }


    @Test
    public void test_getPattern() {
        // prepare
        final ATokenRecognizer rec = new DummyTokenRecognizer();

        // execute & verify
        assertThat(rec.getPattern().toString())
                .isEqualTo(
                        "[ ]?[" + TYPE.getIdentificator() + "]"
                                + "(\\s*)[{].*([}]$|$)");
    }

    private static class DummyTokenRecognizer extends AVariablesTokenRecognizer {

        public DummyTokenRecognizer() {
            super(TYPE);
        }


        @Override
        public ATokenRecognizer newInstance() {
            return new DummyTokenRecognizer();
        }
    }
}
