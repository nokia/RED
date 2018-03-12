/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.agent.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class VariableTypedValueTest {

    @Test
    public void gettersTests() {
        assertThat(new VariableTypedValue("t", 1).getType()).isEqualTo("t");
        assertThat(new VariableTypedValue("t", "val").getValue()).isEqualTo("val");
    }

    @Test
    public void equalsTests() {
        assertThat(new VariableTypedValue("t", 1)).isEqualTo(new VariableTypedValue("t", 1));
        assertThat(new VariableTypedValue("t2", "val")).isEqualTo(new VariableTypedValue("t2", "val"));

        assertThat(new VariableTypedValue("t", "v")).isNotEqualTo(new VariableTypedValue("t2", "v"));
        assertThat(new VariableTypedValue("t2", "v")).isNotEqualTo(new VariableTypedValue("t", "v"));
        assertThat(new VariableTypedValue("t", "v")).isNotEqualTo(new VariableTypedValue("t", "v2"));
        assertThat(new VariableTypedValue("t", "v2")).isNotEqualTo(new VariableTypedValue("t", "v"));
        assertThat(new VariableTypedValue("t", "v")).isNotEqualTo(new VariableTypedValue("t2", "v2"));
        assertThat(new VariableTypedValue("t2", "v2")).isNotEqualTo(new VariableTypedValue("t", "v"));
        assertThat(new VariableTypedValue("t", 1)).isNotEqualTo(new Object());
        assertThat(new VariableTypedValue("t", 1)).isNotEqualTo(null);
    }

    @Test
    public void hashCodeTests() {
        assertThat(new VariableTypedValue("t", "v").hashCode()).isEqualTo(new VariableTypedValue("t", "v").hashCode());
    }

    @Test
    public void stringRepresentationTests() {
        assertThat(new VariableTypedValue("t", "val").toString()).isEqualTo("t: val");
    }
}
