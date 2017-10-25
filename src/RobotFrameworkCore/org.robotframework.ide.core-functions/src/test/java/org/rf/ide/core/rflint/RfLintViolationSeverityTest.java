package org.rf.ide.core.rflint;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class RfLintViolationSeverityTest {

    @Test
    public void parsingFromSingleMarkTest() {
        assertThat(RfLintViolationSeverity.from("E")).isEqualTo(RfLintViolationSeverity.ERROR);
        assertThat(RfLintViolationSeverity.from("W")).isEqualTo(RfLintViolationSeverity.WARNING);
        assertThat(RfLintViolationSeverity.from("I")).isEqualTo(RfLintViolationSeverity.IGNORE);
        assertThat(RfLintViolationSeverity.from("A")).isEqualTo(RfLintViolationSeverity.OTHER);
        assertThat(RfLintViolationSeverity.from("Z")).isEqualTo(RfLintViolationSeverity.OTHER);
    }
}
