/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.test.helpers.ClassFieldCleaner;
import org.rf.ide.core.test.helpers.ClassFieldCleaner.ForClean;

@SuppressWarnings("PMD.MethodNamingConventions")
public class VariableComputationHelperTest {

    @ForClean
    private VariableComputationHelper vch;

    @Test
    public void test_isBracketDecorated_bracketAndAfterText_shouldReturn_FALSE() {
        assertThat(vch.isBracketDecorated("[text")).isFalse();
    }

    @Test
    public void test_isBracketDecorated_bracketAndAfterNegativeNumber_twoAsDouble_shouldReturn_TRUE() {
        assertThat(vch.isBracketDecorated("[-2.0")).isTrue();
    }

    @Test
    public void test_isBracketDecorated_bracketAndAfterNegativeNumber_two_shouldReturn_TRUE() {
        assertThat(vch.isBracketDecorated("[-2")).isTrue();
    }

    @Test
    public void test_isBracketDecorated_bracketAndAfterPossitiveNumber_twoAsDouble_shouldReturn_TRUE() {
        assertThat(vch.isBracketDecorated("[2.0")).isTrue();
    }

    @Test
    public void test_isBracketDecorated_bracketAndAfterPossitiveNumber_two_shouldReturn_TRUE() {
        assertThat(vch.isBracketDecorated("[2")).isTrue();
    }

    @Test
    public void test_isBracketDecorated_onlyBrackets_shouldReturn_TRUE() {
        assertThat(vch.isBracketDecorated("[()]")).isTrue();
    }

    @Test
    public void test_COMPUTATION_PATTERN() {
        assertThat(VariableComputationHelper.COMPUTATION_OPERATION_PATTERN.pattern())
                .isEqualTo("([+]|[-]|[*]|[/]|[:]|[>]|[<]|[=]|[&]|[%]|\\^|\\!|[|])+");
    }

    @Test
    public void test_getFirstNotWhitespaceCharacter_WITH_TABS_index_3() {
        assertThat(vch.getFirstNotWhitespaceCharacter("\t\t\t\tW", 3)).isEqualTo(4);
    }

    @Test
    public void test_getFirstNotWhitespaceCharacter_WITH_SPACES_index_3() {
        assertThat(vch.getFirstNotWhitespaceCharacter("    W", 3)).isEqualTo(4);
    }

    @Test
    public void test_getFirstNotWhitespaceCharacter_WITH_TABS_index_0() {
        assertThat(vch.getFirstNotWhitespaceCharacter("\t\t\tW", 0)).isEqualTo(3);
    }

    @Test
    public void test_getFirstNotWhitespaceCharacter_WITH_SPACES_index_0() {
        assertThat(vch.getFirstNotWhitespaceCharacter("   W", 0)).isEqualTo(3);
    }

    @Before
    public void setUp() throws Exception {
        vch = new VariableComputationHelper();
    }

    @After
    public void tearDown() throws Exception {
        ClassFieldCleaner.init(this);
    }
}
