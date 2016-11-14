/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class RobotExpressionsTest {

    @Test
    public void testSpacesUnEscaping() {
        assertThat(RobotExpressions.unescapeSpaces("value")).isEqualTo("value");
        assertThat(RobotExpressions.unescapeSpaces("value 1")).isEqualTo("value 1");
        assertThat(RobotExpressions.unescapeSpaces("value  2")).isEqualTo("value  2");
        assertThat(RobotExpressions.unescapeSpaces("value   3")).isEqualTo("value   3");
        assertThat(RobotExpressions.unescapeSpaces("value \\ 2")).isEqualTo("value  2");
        assertThat(RobotExpressions.unescapeSpaces("value \\ \\ 3")).isEqualTo("value   3");
        assertThat(RobotExpressions.unescapeSpaces("value\\\\ 1")).isEqualTo("value\\\\ 1");
    }
}
