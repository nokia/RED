/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.rflint;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class RfLintRuleConfigurationTest {

    @Test
    public void ruleConfigurationTest() {
        final RfLintRuleConfiguration config = new RfLintRuleConfiguration();
        
        assertThat(config.isEmpty()).isTrue();
        assertThat(config.getSeverity()).isNull();
        assertThat(config.getArguments()).isNull();

        config.setSeverity(RfLintViolationSeverity.ERROR);

        assertThat(config.isEmpty()).isFalse();
        assertThat(config.getSeverity()).isEqualTo(RfLintViolationSeverity.ERROR);
        assertThat(config.getArguments()).isNull();

        config.setArguments("args");

        assertThat(config.isEmpty()).isFalse();
        assertThat(config.getSeverity()).isEqualTo(RfLintViolationSeverity.ERROR);
        assertThat(config.getArguments()).isEqualTo("args");

        config.setSeverity(null);

        assertThat(config.isEmpty()).isFalse();
        assertThat(config.getSeverity()).isNull();
        assertThat(config.getArguments()).isEqualTo("args");

        config.setArguments(null);

        assertThat(config.isEmpty()).isTrue();
        assertThat(config.getSeverity()).isNull();
        assertThat(config.getArguments()).isNull();
    }

}
