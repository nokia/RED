/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.formatter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class LineReplaceTabWithSpacesFormatterTest {

    @Test
    public void tabsAreReplacedWithSpaces() throws Exception {
        final ILineFormatter formatter = new LineReplaceTabWithSpacesFormatter(2);

        assertThat(formatter.format("text")).isEqualTo("text");
        assertThat(formatter.format("text \t")).isEqualTo("text   ");
        assertThat(formatter.format("\ttext \t\t")).isEqualTo("  text     ");
        assertThat(formatter.format("\tt\te\txt")).isEqualTo("  t  e  xt");
    }

}
