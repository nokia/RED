/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs.impl.old;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.table.variables.descs.impl.old.MappingResult;
import org.rf.ide.core.testdata.model.table.variables.descs.impl.old.VariableDeclaration;
import org.rf.ide.core.testdata.model.table.variables.descs.impl.old.VariableExtractor;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class VariableDeclarationTest {

    @Test
    public void testIsDefinedVariable1() {
        assertThat(varDeclaration("${var1}").isDefinedVariable(newHashSet())).isFalse();
        assertThat(varDeclaration("${var2}").isDefinedVariable(newHashSet("${var1}"))).isFalse();

        assertThat(varDeclaration("${var1}").isDefinedVariable(newHashSet("${var1}"))).isTrue();
        assertThat(varDeclaration("@{var1}").isDefinedVariable(newHashSet("${var1}"))).isTrue();
        assertThat(varDeclaration("&{var1}").isDefinedVariable(newHashSet("${var1}"))).isTrue();
        assertThat(varDeclaration("${var1}").isDefinedVariable(newHashSet("@{var1}"))).isTrue();
        assertThat(varDeclaration("${var1}").isDefinedVariable(newHashSet("&{var1}"))).isTrue();
        assertThat(varDeclaration("${V ar_1}").isDefinedVariable(newHashSet("${var1}"))).isTrue();
        assertThat(varDeclaration("${vaR__ 1}").isDefinedVariable(newHashSet("${var1}"))).isTrue();
        assertThat(varDeclaration("${var1.object.name}").isDefinedVariable(newHashSet("${var1}"))).isTrue();
    }

    @Test
    public void testIsDefinedVariable2() {
        assertThat(VariableDeclaration.isDefinedVariable("var1", "$", newHashSet())).isFalse();
        assertThat(VariableDeclaration.isDefinedVariable("var2", "$", newHashSet("${var1}"))).isFalse();

        assertThat(VariableDeclaration.isDefinedVariable("var1", "$", newHashSet("${var1}"))).isTrue();
        assertThat(VariableDeclaration.isDefinedVariable("var1", "@", newHashSet("${var1}"))).isTrue();
        assertThat(VariableDeclaration.isDefinedVariable("var1", "&", newHashSet("${var1}"))).isTrue();
        assertThat(VariableDeclaration.isDefinedVariable("var1", "$", newHashSet("@{var1}"))).isTrue();
        assertThat(VariableDeclaration.isDefinedVariable("var1", "$", newHashSet("&{var1}"))).isTrue();
        assertThat(VariableDeclaration.isDefinedVariable("V ar_1", "$", newHashSet("${var1}"))).isTrue();
        assertThat(VariableDeclaration.isDefinedVariable("vaR__ 1", "$", newHashSet("${var1}"))).isTrue();
        assertThat(VariableDeclaration.isDefinedVariable("var1.object.name", "$", newHashSet("${var1}"))).isTrue();
    }

    @Test
    public void testIsDefinedVariableInsideComputation() {
        assertThat(varDeclaration("${x}").isDefinedVariableInsideComputation(newHashSet())).isFalse();

        assertThat(varDeclaration("${x * 10}").isDefinedVariableInsideComputation(newHashSet("${x}"))).isTrue();
        assertThat(varDeclaration("${1 + 10}").isDefinedVariableInsideComputation(newHashSet())).isTrue();
    }

    private static VariableDeclaration varDeclaration(final String text) {
        final MappingResult extract = new VariableExtractor().extract(varToken(text));
        return extract.getCorrectVariables().get(0);
    }

    private static RobotToken varToken(final String text) {
        return RobotToken.create(text, new FilePosition(0, 0, 0), RobotTokenType.VARIABLE_USAGE);
    }
}
