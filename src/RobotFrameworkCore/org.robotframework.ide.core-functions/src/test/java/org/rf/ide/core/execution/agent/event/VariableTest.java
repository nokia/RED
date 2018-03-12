/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.agent.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;

public class VariableTest {

    @Test
    public void gettersTests() {
        assertThat(new Variable("name", VariableScope.GLOBAL).getName()).isEqualTo("name");
        assertThat(new Variable("name", VariableScope.GLOBAL).getScope()).isEqualTo(VariableScope.GLOBAL);
    }

    @Test
    public void equalsTests() {
        assertThat(new Variable("name", VariableScope.LOCAL)).isEqualTo(new Variable("name", VariableScope.LOCAL));
        assertThat(new Variable("n", VariableScope.TEST_CASE)).isEqualTo(new Variable("n", VariableScope.TEST_CASE));
        assertThat(new Variable("name", VariableScope.GLOBAL)).isEqualTo(new Variable("name", VariableScope.LOCAL));
        assertThat(new Variable("name", VariableScope.LOCAL)).isEqualTo(new Variable("name", VariableScope.GLOBAL));

        assertThat(new Variable("name", VariableScope.TEST_CASE))
                .isNotEqualTo(new Variable("name1", VariableScope.TEST_CASE));
        assertThat(new Variable("name1", VariableScope.TEST_CASE))
                .isNotEqualTo(new Variable("name", VariableScope.TEST_CASE));
        assertThat(new Variable("name", VariableScope.TEST_SUITE))
                .isNotEqualTo(new Variable("name1", VariableScope.TEST_CASE));
        assertThat(new Variable("name1", VariableScope.TEST_SUITE))
                .isNotEqualTo(new Variable("name", VariableScope.TEST_CASE));
        assertThat(new Variable("name", VariableScope.LOCAL)).isNotEqualTo(new Object());
        assertThat(new Variable("name", VariableScope.TEST_CASE)).isNotEqualTo(null);
    }

    @Test
    public void hashCodeTests() {
        assertThat(new Variable("name", VariableScope.TEST_SUITE).hashCode())
                .isEqualTo(new Variable("name", VariableScope.TEST_SUITE).hashCode());
    }

    @Test
    public void stringRepresentationTests() {
        assertThat(new Variable("name", VariableScope.GLOBAL).toString()).isEqualTo("GLOBAL: name");
        assertThat(new Variable("name", VariableScope.TEST_SUITE).toString()).isEqualTo("TEST_SUITE: name");
        assertThat(new Variable("name", VariableScope.TEST_CASE).toString()).isEqualTo("TEST_CASE: name");
        assertThat(new Variable("name", VariableScope.LOCAL).toString()).isEqualTo("LOCAL: name");
        assertThat(new Variable("name").toString()).isEqualTo("<null>: name");
    }
}
