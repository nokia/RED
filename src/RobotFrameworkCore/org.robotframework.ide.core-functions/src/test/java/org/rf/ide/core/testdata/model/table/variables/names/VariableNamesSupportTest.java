/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.names;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.MappingResult;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class VariableNamesSupportTest {

    @Test
    public void testExtractVarNameWithSpaces() {
        String variable = "${var 1}";
        Assert.assertEquals("${var1}", VariableNamesSupport.extractUnifiedVariableName(variable));
        variable = "${ va r 1}";
        Assert.assertEquals("${var1}", VariableNamesSupport.extractUnifiedVariableName(variable));
    }

    @Test
    public void testExtractVarNameWithUnderscores() {
        String variable = "${var_1}";
        Assert.assertEquals("${var1}", VariableNamesSupport.extractUnifiedVariableName(variable));
        variable = "${_va_r_1}";
        Assert.assertEquals("${var1}", VariableNamesSupport.extractUnifiedVariableName(variable));
    }

    @Test
    public void testExtractVarNameWithUnderscoresAndSpacesAndUpperCases() {
        String variable = "${vAr_ 1}";
        Assert.assertEquals("${var1}", VariableNamesSupport.extractUnifiedVariableName(variable));
        variable = "${_VA _r_ 1}";
        Assert.assertEquals("${var1}", VariableNamesSupport.extractUnifiedVariableName(variable));
    }

    @Test
    public void testExtractVarNameWithoutBrackets() {
        String variable = "${Var _1}";
        Assert.assertEquals("var1", VariableNamesSupport.extractUnifiedVariableNameWithoutBrackets(variable));
    }

    @Test
    public void testExtractWrongVarNameWithoutBrackets() {
        String variable = "${}";
        Assert.assertEquals("", VariableNamesSupport.extractUnifiedVariableNameWithoutBrackets(variable));
        variable = "$";
        Assert.assertEquals("", VariableNamesSupport.extractUnifiedVariableNameWithoutBrackets(variable));
        variable = "${var1";
        Assert.assertEquals("", VariableNamesSupport.extractUnifiedVariableNameWithoutBrackets(variable));
    }

    @Test
    public void testHasEqualNames() {
        String variable1 = "${Va_r 1}";
        String variable2 = "@{_va R1}";
        Assert.assertTrue(VariableNamesSupport.hasEqualNames(variable1, variable2));
        variable1 = "${Va_r 1}";
        variable2 = "@{_va R2}";
        Assert.assertFalse(VariableNamesSupport.hasEqualNames(variable1, variable2));
    }

    @Test
    public void testIsDefinedVariable() {
        VariableDeclaration variable = createVariableDeclaration("${var1}");
        Assert.assertFalse(VariableNamesSupport.isDefinedVariable(variable, new HashSet<String>()));

        Set<String> definedVars = new HashSet<String>();
        definedVars.add("${var1}");

        variable = createVariableDeclaration("${var1}");
        Assert.assertTrue(VariableNamesSupport.isDefinedVariable(variable, definedVars));
        variable = createVariableDeclaration("@{V ar_1}");
        Assert.assertTrue(VariableNamesSupport.isDefinedVariable(variable, definedVars));
        variable = createVariableDeclaration("&{vaR__ 1}");
        Assert.assertTrue(VariableNamesSupport.isDefinedVariable(variable, definedVars));
        variable = createVariableDeclaration("${var1.object.name}");
        Assert.assertTrue(VariableNamesSupport.isDefinedVariable(variable, definedVars));
        variable = createVariableDeclaration("${var2}");
        Assert.assertFalse(VariableNamesSupport.isDefinedVariable(variable, definedVars));
    }

    @Test
    public void testExtractUnifiedVariableNames() {
        VariableDeclaration variable1 = createVariableDeclaration("${VAR_1}");
        VariableDeclaration variable2 = createVariableDeclaration("@{VAR 2}");

        List<VariableDeclaration> vars = new ArrayList<VariableDeclaration>();
        vars.add(variable1);
        vars.add(variable2);

        List<String> variableNames = VariableNamesSupport.extractUnifiedVariableNames(vars);
        Assert.assertEquals(2, variableNames.size());
        Assert.assertEquals("${var1}", variableNames.get(0));
        Assert.assertEquals("@{var2}", variableNames.get(1));
    }

    private VariableDeclaration createVariableDeclaration(final String text) {
        RobotToken rt = new RobotToken();
        rt.setLineNumber(0);
        rt.setStartOffset(0);
        rt.setStartColumn(0);
        rt.setRaw(text);
        rt.setText(text);
        rt.setType(RobotTokenType.VARIABLE_USAGE);
        VariableExtractor varExt = new VariableExtractor();
        MappingResult extract = varExt.extract(rt, "fake.txt");

        return extract.getCorrectVariables().get(0);
    }
}
