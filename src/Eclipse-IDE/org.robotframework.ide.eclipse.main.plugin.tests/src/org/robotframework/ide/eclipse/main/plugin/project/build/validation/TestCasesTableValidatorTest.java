/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class TestCasesTableValidatorTest {

    @Test
    public void testExtractVariables() throws Exception {
        assertThat(TestCasesTableValidator.extractVariables("${var}")).containsExactly("${var}");
    }

}
