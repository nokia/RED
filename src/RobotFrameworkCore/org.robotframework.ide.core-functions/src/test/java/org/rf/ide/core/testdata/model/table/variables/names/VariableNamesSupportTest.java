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
import static org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport.isDefinedVariableInsideComputation;

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
        assertThat(hasEqualNames("${Va_r 1}", "@{_va R2}")).isFalse();

        assertThat(hasEqualNames("${Va_r 1}", "@{_va R1}")).isTrue();
    }

    @Test
    public void testIsDefinedVariable1() {
        assertThat(isDefinedVariable(varDeclaration("${var1}"), newHashSet())).isFalse();
        assertThat(isDefinedVariable(varDeclaration("${var2}"), newHashSet("${var1}"))).isFalse();

        assertThat(isDefinedVariable(varDeclaration("${var1}"), newHashSet("${var1}"))).isTrue();
        assertThat(isDefinedVariable(varDeclaration("@{var1}"), newHashSet("${var1}"))).isTrue();
        assertThat(isDefinedVariable(varDeclaration("&{var1}"), newHashSet("${var1}"))).isTrue();
        assertThat(isDefinedVariable(varDeclaration("${var1}"), newHashSet("@{var1}"))).isTrue();
        assertThat(isDefinedVariable(varDeclaration("${var1}"), newHashSet("&{var1}"))).isTrue();
        assertThat(isDefinedVariable(varDeclaration("${V ar_1}"), newHashSet("${var1}"))).isTrue();
        assertThat(isDefinedVariable(varDeclaration("${vaR__ 1}"), newHashSet("${var1}"))).isTrue();
        assertThat(isDefinedVariable(varDeclaration("${var1.object.name}"), newHashSet("${var1}"))).isTrue();
    }

    @Test
    public void testIsDefinedVariable2() {
        assertThat(isDefinedVariable("var1", "$", newHashSet())).isFalse();
        assertThat(isDefinedVariable("var2", "$", newHashSet("${var1}"))).isFalse();

        assertThat(isDefinedVariable("var1", "$", newHashSet("${var1}"))).isTrue();
        assertThat(isDefinedVariable("var1", "@", newHashSet("${var1}"))).isTrue();
        assertThat(isDefinedVariable("var1", "&", newHashSet("${var1}"))).isTrue();
        assertThat(isDefinedVariable("var1", "$", newHashSet("@{var1}"))).isTrue();
        assertThat(isDefinedVariable("var1", "$", newHashSet("&{var1}"))).isTrue();
        assertThat(isDefinedVariable("V ar_1", "$", newHashSet("${var1}"))).isTrue();
        assertThat(isDefinedVariable("vaR__ 1", "$", newHashSet("${var1}"))).isTrue();
        assertThat(isDefinedVariable("var1.object.name", "$", newHashSet("${var1}"))).isTrue();
    }

    @Test
    public void testIsDefinedVariableInsideComputation() {
        assertThat(isDefinedVariableInsideComputation(varDeclaration("${x}"), newHashSet())).isFalse();

        assertThat(isDefinedVariableInsideComputation(varDeclaration("${x * 10}"), newHashSet("${x}"))).isTrue();
        assertThat(isDefinedVariableInsideComputation(varDeclaration("${1 + 10}"), newHashSet())).isTrue();
    }

    @Test
    public void testExtractUnifiedVariableNames() {
        assertThat(extractUnifiedVariableNames(newArrayList(varDeclaration("${VAR_1}"), varDeclaration("@{VAR 2}"))))
                .containsExactly("${var1}", "@{var2}");
    }

    private VariableDeclaration varDeclaration(final String text) {
        final RobotToken rt = new RobotToken();
        rt.setLineNumber(0);
        rt.setStartOffset(0);
        rt.setStartColumn(0);
        rt.setText(text);
        rt.setType(RobotTokenType.VARIABLE_USAGE);
        final VariableExtractor varExt = new VariableExtractor();
        final MappingResult extract = varExt.extract(rt, "fake.txt");

        return extract.getCorrectVariables().get(0);
    }
}
