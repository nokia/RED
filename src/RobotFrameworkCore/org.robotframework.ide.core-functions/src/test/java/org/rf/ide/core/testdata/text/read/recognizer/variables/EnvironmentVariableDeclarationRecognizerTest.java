/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer.variables;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;

/**
 * @author lwlodarc
 *
 */
public class EnvironmentVariableDeclarationRecognizerTest {

    @Test
    public void instanceOfRecognizer_hasPossiblePattern() {
        final EnvironmentVariableDeclarationRecognizer recognizer = new EnvironmentVariableDeclarationRecognizer();
        assertThat(recognizer.getPattern()).isNotNull();
        assertThat(recognizer.getPattern().pattern()).contains(VariableType.ENVIRONMENT.getIdentificator());
    }

}
