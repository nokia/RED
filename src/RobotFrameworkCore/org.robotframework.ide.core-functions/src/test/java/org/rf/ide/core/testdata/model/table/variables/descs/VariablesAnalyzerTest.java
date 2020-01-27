/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class VariablesAnalyzerTest {

    @Test
    public void testExtractUnifiedVariableName() {
        assertThat(VariablesAnalyzer.normalizeName((String) null)).isEmpty();
        assertThat(VariablesAnalyzer.normalizeName("")).isEmpty();

        assertThat(VariablesAnalyzer.normalizeName("${var 1}")).isEqualTo("${var1}");
        assertThat(VariablesAnalyzer.normalizeName("${ va r 1}")).isEqualTo("${var1}");
        assertThat(VariablesAnalyzer.normalizeName("${var_1}")).isEqualTo("${var1}");
        assertThat(VariablesAnalyzer.normalizeName("${_va_r_1}")).isEqualTo("${var1}");
        assertThat(VariablesAnalyzer.normalizeName("${vAr_ 1}")).isEqualTo("${var1}");
        assertThat(VariablesAnalyzer.normalizeName("${_VA _r_ 1}")).isEqualTo("${var1}");
    }

    @Test
    public void testExtractUnifiedVariableNameWithoutBrackets() {
        assertThat(VariablesAnalyzer.extractFromBrackets((String) null)).isEmpty();
        assertThat(VariablesAnalyzer.extractFromBrackets("")).isEmpty();
        assertThat(VariablesAnalyzer.extractFromBrackets("${}")).isEmpty();
        assertThat(VariablesAnalyzer.extractFromBrackets("$")).isEmpty();
        assertThat(VariablesAnalyzer.extractFromBrackets("${var1")).isEmpty();
        assertThat(VariablesAnalyzer.extractFromBrackets("$var1}")).isEmpty();

        assertThat(VariablesAnalyzer.extractFromBrackets("${var1}")).isEqualTo("var1");
        assertThat(VariablesAnalyzer.extractFromBrackets("@{list}")).isEqualTo("list");
        assertThat(VariablesAnalyzer.extractFromBrackets("&{dict}")).isEqualTo("dict");
    }

    @Test
    public void testHasEqualNames() {
        assertThat(VariablesAnalyzer.hasEqualNormalizedNames("${Va_r 1}", "@{_va R2}")).isFalse();

        assertThat(VariablesAnalyzer.hasEqualNormalizedNames("${Va_r 1}", "@{_va R1}")).isTrue();
    }

    @Test
    public void testExtractUnifiedVariableNames() {
        assertThat(VariablesAnalyzer.normalizeName(varToken("${VAR_1}"))).isEqualTo("${var1}");
        assertThat(VariablesAnalyzer.normalizeName(varToken("@{VAR_2}"))).isEqualTo("@{var2}");
    }

    private static RobotToken varToken(final String text) {
        return RobotToken.create(text, new FilePosition(0, 0, 0), RobotTokenType.VARIABLE_USAGE);
    }
}
