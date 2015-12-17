/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.MappingResult;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class VariableExtractorTest {

    @Test
    public void test_extractionOf_EnvironmentVariable_ONLY() {
        // prepare
        VariableExtractor extractor = new VariableExtractor();

        RobotToken varToken = new RobotToken();
        varToken.setLineNumber(0);
        varToken.setStartColumn(1);
        varToken.setStartOffset(2);
        varToken.setRaw("%{user.home}");
        varToken.setText("%{user.home}");

        // execute
        MappingResult mapResult = extractor.extract(varToken, "myFile.robot");

        // verify
        assertThat(mapResult.getMessages()).isEmpty();
        assertThat(mapResult.getFilename()).isEqualTo("myFile.robot");

        assertThat(mapResult.getMappedElements()).hasSize(1);
        assertThat(mapResult.getTextElements()).isEmpty();
        assertThat(mapResult.getCorrectVariables()).hasSize(1);
        VariableDeclaration variableDeclaration = mapResult.getCorrectVariables().get(0);
        assertThat(variableDeclaration.getRobotType()).isEqualTo(VariableType.ENVIRONMENT);
        assertThat(variableDeclaration.getVariableText().getText()).isEqualTo("%{user.home}");
        assertThat(variableDeclaration.getVariableName().getText()).isEqualTo("user.home");
        RobotToken asToken = variableDeclaration.asToken();
        assertThat(asToken.getStartColumn()).isEqualTo(1);
        assertThat(asToken.getEndColumn()).isEqualTo("%{user.home}".length() + 1);
    }
}
