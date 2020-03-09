/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs.impl;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.testdata.model.table.variables.descs.impl.ExpressionAstNode.NodeKind;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class VarAstNodeAdapterTest {

    @Test
    public void variableMadeOfNumbersOnlyIsRecognized() {
        assertThat(newVar("${1}").isDefinedIn(new HashSet<>())).isTrue();
        assertThat(newVar("${ 1}").isDefinedIn(new HashSet<>())).isTrue();
        assertThat(newVar("${1 }").isDefinedIn(new HashSet<>())).isTrue();
        assertThat(newVar("${ 1 }").isDefinedIn(new HashSet<>())).isTrue();
        assertThat(newVar("${1+2}").isDefinedIn(new HashSet<>())).isTrue();
        assertThat(newVar("${0b11101}").isDefinedIn(new HashSet<>())).isTrue();
        assertThat(newVar("${0B11101}").isDefinedIn(new HashSet<>())).isTrue();
        assertThat(newVar("${0o123}").isDefinedIn(new HashSet<>())).isTrue();
        assertThat(newVar("${0O567}").isDefinedIn(new HashSet<>())).isTrue();
        assertThat(newVar("${0xabc}").isDefinedIn(new HashSet<>())).isTrue();
        assertThat(newVar("${0XABC}").isDefinedIn(new HashSet<>())).isTrue();
    }

    @Test
    public void variableIsFoundInGivenSet() {
        assertThat(newVar("${x}").isDefinedIn(newHashSet("${x}"))).isTrue();
        assertThat(newVar("${x}").isDefinedIn(newHashSet("@{x}"))).isTrue();
        assertThat(newVar("${x}").isDefinedIn(newHashSet("&{x}"))).isTrue();
        assertThat(newVar("${X }").isDefinedIn(newHashSet("${x}"))).isTrue();
        assertThat(newVar("${ X}").isDefinedIn(newHashSet("@{x}"))).isTrue();
        assertThat(newVar("${X }").isDefinedIn(newHashSet("&{x}"))).isTrue();

        assertThat(newVar("@{l}").isDefinedIn(newHashSet("${l}"))).isTrue();
        assertThat(newVar("@{l}").isDefinedIn(newHashSet("@{l}"))).isTrue();
        assertThat(newVar("@{l}").isDefinedIn(newHashSet("&{l}"))).isTrue();
        assertThat(newVar("@{L }").isDefinedIn(newHashSet("${l}"))).isTrue();
        assertThat(newVar("@{ L}").isDefinedIn(newHashSet("@{l}"))).isTrue();
        assertThat(newVar("@{L }").isDefinedIn(newHashSet("&{l}"))).isTrue();

        assertThat(newVar("&{d}").isDefinedIn(newHashSet("${d}"))).isTrue();
        assertThat(newVar("&{d}").isDefinedIn(newHashSet("@{d}"))).isTrue();
        assertThat(newVar("&{d}").isDefinedIn(newHashSet("&{d}"))).isTrue();
        assertThat(newVar("&{D }").isDefinedIn(newHashSet("${d}"))).isTrue();
        assertThat(newVar("&{ D}").isDefinedIn(newHashSet("@{d}"))).isTrue();
        assertThat(newVar("&{D }").isDefinedIn(newHashSet("&{d}"))).isTrue();
    }

    @Test
    public void variableUsingExtendedSyntaxIsFoundInGivenSet() {
        assertThat(newVar("${x+1}").isDefinedIn(newHashSet("${x}"))).isTrue();
        assertThat(newVar("${ x +1}").isDefinedIn(newHashSet("${x}"))).isTrue();
        assertThat(newVar("${x.__abs__()}").isDefinedIn(newHashSet("${x}"))).isTrue();
        assertThat(newVar("${x.field}").isDefinedIn(newHashSet("${x}"))).isTrue();
        assertThat(newVar("${x+'abc'}").isDefinedIn(newHashSet("${x}"))).isTrue();
        assertThat(newVar("${x+\"abc\"").isDefinedIn(newHashSet("${x}"))).isTrue();
    }

    private static VarAstNodeAdapter newVar(final String var) {
        final ExpressionAstNode root = ExpressionAstNode.root(RobotToken.create(var));
        final ExpressionAstNode node = ExpressionAstNode.child(root, NodeKind.VAR, 0);
        node.close(var.length());
        return new VarAstNodeAdapter(null, node);
    }

}
