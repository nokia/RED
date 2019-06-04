/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.rflint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.Test;

public class RfLintViolationSeverityTest {

    @Test
    public void parsingFromSingleMarkTest() {
        assertThat(RfLintViolationSeverity.from("E")).isEqualTo(RfLintViolationSeverity.ERROR);
        assertThat(RfLintViolationSeverity.from("W")).isEqualTo(RfLintViolationSeverity.WARNING);
        assertThat(RfLintViolationSeverity.from("I")).isEqualTo(RfLintViolationSeverity.IGNORE);
    }
    
    @Test
    public void parsingFromSingleMarkExceptionsTest() {
        assertThatIllegalArgumentException().isThrownBy(() -> RfLintViolationSeverity.from("A"));
        assertThatIllegalArgumentException().isThrownBy(() -> RfLintViolationSeverity.from("Z"));
    }

    @Test
    public void switchesTest() {
        assertThat(RfLintViolationSeverity.ERROR.severitySwitch()).isEqualTo("e");
        assertThat(RfLintViolationSeverity.WARNING.severitySwitch()).isEqualTo("w");
        assertThat(RfLintViolationSeverity.IGNORE.severitySwitch()).isEqualTo("i");
    }
}
