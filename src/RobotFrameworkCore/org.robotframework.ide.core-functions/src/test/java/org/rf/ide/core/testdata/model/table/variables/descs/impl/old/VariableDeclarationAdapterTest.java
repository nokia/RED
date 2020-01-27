/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs.impl.old;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class VariableDeclarationAdapterTest {

    @Test
    public void nameGettingTest() {
        assertThat(varUsage("${x}").getName()).isEqualTo("x");
        assertThat(varUsage("${x + 2}").getName()).isEqualTo("x");
        assertThat(varUsage("${x * 7}").getName()).isEqualTo("x");
        assertThat(varUsage("${x + 'abc'}").getName()).isEqualTo("x");

        assertThat(varUsage("${var}").getName()).isEqualTo("var");
        assertThat(varUsage("${var + 2}").getName()).isEqualTo("var");
        assertThat(varUsage("${var * 7}").getName()).isEqualTo("var");

        assertThat(varUsage("@{list}").getName()).isEqualTo("list");
        assertThat(varUsage("@{list[10]}").getName()).isEqualTo("list");

        assertThat(varUsage("&{dict}").getName()).isEqualTo("dict");
        assertThat(varUsage("&{dict[key]}").getName()).isEqualTo("dict");
    }

    private static VariableDeclarationAdapter varUsage(final String text) {
        final RobotToken varToken = RobotToken.create(text, new FilePosition(0, 0, 0), RobotTokenType.VARIABLE_USAGE);
        final MappingResult extract = new VariableExtractor().extract(varToken);
        return new VariableDeclarationAdapter(extract.getCorrectVariables().get(0), true, true);
    }
}
