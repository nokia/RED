/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.names;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport.extractUnifiedVariableName;
import static org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport.extractUnifiedVariableNameWithoutBrackets;
import static org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport.extractUnifiedVariableNames;
import static org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport.hasEqualNames;
import static org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport.isDefinedVariable;

import java.util.List;

import org.junit.Test;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.MappingResult;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class VariableNamesSupportTest {

    @Test
    public void testExtractUnifiedVariableName() {
        assertThat(extractUnifiedVariableName(null)).isEmpty();
        assertThat(extractUnifiedVariableName("")).isEmpty();
        assertThat(extractUnifiedVariableName("${var 1}")).isEqualTo("${var1}");
        assertThat(extractUnifiedVariableName("${ va r 1}")).isEqualTo("${var1}");
        assertThat(extractUnifiedVariableName("${var_1}")).isEqualTo("${var1}");
        assertThat(extractUnifiedVariableName("${_va_r_1}")).isEqualTo("${var1}");
        assertThat(extractUnifiedVariableName("${vAr_ 1}")).isEqualTo("${var1}");
        assertThat(extractUnifiedVariableName("${_VA _r_ 1}")).isEqualTo("${var1}");
    }

    @Test
    public void testExtractUnifiedVariableNameWithoutBrackets() {
        assertThat(extractUnifiedVariableNameWithoutBrackets(null)).isEmpty();
        assertThat(extractUnifiedVariableNameWithoutBrackets("")).isEmpty();
        assertThat(extractUnifiedVariableNameWithoutBrackets("${}")).isEmpty();
        assertThat(extractUnifiedVariableNameWithoutBrackets("$")).isEmpty();
        assertThat(extractUnifiedVariableNameWithoutBrackets("${var1")).isEmpty();
        assertThat(extractUnifiedVariableNameWithoutBrackets("$var1}")).isEmpty();
        assertThat(extractUnifiedVariableNameWithoutBrackets("${Var _1}")).isEqualTo("var1");
    }

    @Test
    public void testHasEqualNames() {
        assertThat(hasEqualNames("${Va_r 1}", "@{_va R1}")).isTrue();
        assertThat(hasEqualNames("${Va_r 1}", "@{_va R2}")).isFalse();
    }

    @Test
    public void testIsDefinedVariable() {
        assertThat(isDefinedVariable(createVariableDeclaration("${var1}"), newHashSet())).isFalse();
        assertThat(isDefinedVariable(createVariableDeclaration("${var1}"), newHashSet("${var1}"))).isTrue();
        assertThat(isDefinedVariable(createVariableDeclaration("@{V ar_1}"), newHashSet("${var1}"))).isTrue();
        assertThat(isDefinedVariable(createVariableDeclaration("&{vaR__ 1}"), newHashSet("${var1}"))).isTrue();
        assertThat(isDefinedVariable(createVariableDeclaration("${var1.object.name}"), newHashSet("${var1}"))).isTrue();
        assertThat(isDefinedVariable(createVariableDeclaration("${var2}"), newHashSet("${var1}"))).isFalse();
    }

    @Test
    public void testExtractUnifiedVariableNames() {
        final List<VariableDeclaration> assignments = newArrayList(createVariableDeclaration("${VAR_1}"),
                createVariableDeclaration("@{VAR 2}"));
        assertThat(extractUnifiedVariableNames(assignments)).containsExactly("${var1}", "@{var2}");
    }

    private VariableDeclaration createVariableDeclaration(final String text) {
        final RobotToken rt = new RobotToken();
        rt.setLineNumber(0);
        rt.setStartOffset(0);
        rt.setStartColumn(0);
        rt.setRaw(text);
        rt.setText(text);
        rt.setType(RobotTokenType.VARIABLE_USAGE);
        final VariableExtractor varExt = new VariableExtractor();
        final MappingResult extract = varExt.extract(rt, "fake.txt");

        return extract.getCorrectVariables().get(0);
    }
}
