/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server.response;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ChangeVariableTest {

    @Test
    public void properRequestIsConstructed_forVariableChangeMessage() {
        assertThat(new ChangeVariable("a", newArrayList()).toMessage())
                .isEqualTo("{\"variable_change\":{\"a\":[]}}");
        assertThat(new ChangeVariable("a", newArrayList("b")).toMessage())
                .isEqualTo("{\"variable_change\":{\"a\":[\"b\"]}}");
        assertThat(new ChangeVariable("a", newArrayList("b", "c")).toMessage())
                .isEqualTo("{\"variable_change\":{\"a\":[\"b\",\"c\"]}}");
    }
}
