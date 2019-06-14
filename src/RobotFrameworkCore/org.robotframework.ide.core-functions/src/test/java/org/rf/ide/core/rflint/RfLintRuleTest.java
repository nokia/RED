/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.rflint;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class RfLintRuleTest {

    @Test
    public void gettersTest() {
        final RfLintRuleConfiguration config = new RfLintRuleConfiguration();
        assertThat(new RfLintRule("Rule", RfLintViolationSeverity.ERROR, "file", "docu").getRuleName())
                .isEqualTo("Rule");
        assertThat(new RfLintRule("Rule", RfLintViolationSeverity.ERROR, "file", "docu").getSeverity())
                .isEqualTo(RfLintViolationSeverity.ERROR);
        assertThat(new RfLintRule("Rule", RfLintViolationSeverity.ERROR, "file", "docu").getFilepath())
                .isEqualTo("file");
        assertThat(new RfLintRule("Rule", RfLintViolationSeverity.ERROR, "file", "docu").getConfiguration()).isNull();
        assertThat(new RfLintRule("Rule", RfLintViolationSeverity.ERROR, "file", "docu", config).getConfiguration())
                .isSameAs(config);
        assertThat(new RfLintRule("Rule", RfLintViolationSeverity.ERROR, "file", "docu").isConfigured()).isFalse();
        assertThat(new RfLintRule("Rule", RfLintViolationSeverity.ERROR, "file", "docu", config).isConfigured())
                .isTrue();
    }

    @Test
    public void ruleSeverityConfigurationTest() {
        final RfLintRule rule = new RfLintRule("Rule", RfLintViolationSeverity.ERROR, "", "docu");

        assertThat(rule.isConfigured()).isFalse();
        assertThat(rule.getConfiguration()).isNull();

        rule.configure(RfLintViolationSeverity.ERROR);

        assertThat(rule.isConfigured()).isFalse();
        assertThat(rule.getConfiguration()).isNull();

        rule.configure(RfLintViolationSeverity.WARNING);

        assertThat(rule.isConfigured()).isTrue();
        assertThat(rule.getConfiguration()).isNotNull();
        assertThat(rule.getConfiguration().getSeverity()).isEqualTo(RfLintViolationSeverity.WARNING);
        assertThat(rule.getConfiguration().getArguments()).isNull();

        rule.configure(RfLintViolationSeverity.ERROR);

        assertThat(rule.isConfigured()).isFalse();
        assertThat(rule.getConfiguration()).isNull();
    }

    @Test
    public void ruleArgumentsConfigurationTest() {
        final RfLintRule rule = new RfLintRule("Rule", RfLintViolationSeverity.ERROR, "", "docu");

        assertThat(rule.isConfigured()).isFalse();
        assertThat(rule.getConfiguration()).isNull();

        rule.configure("");

        assertThat(rule.isConfigured()).isFalse();
        assertThat(rule.getConfiguration()).isNull();

        rule.configure("args");

        assertThat(rule.isConfigured()).isTrue();
        assertThat(rule.getConfiguration()).isNotNull();
        assertThat(rule.getConfiguration().getSeverity()).isNull();
        assertThat(rule.getConfiguration().getArguments()).isEqualTo("args");

        rule.configure(" ");

        assertThat(rule.isConfigured()).isFalse();
        assertThat(rule.getConfiguration()).isNull();
    }

    @Test
    public void ruleCmdLineSwitchesTest() {
        final RfLintViolationSeverity err = RfLintViolationSeverity.ERROR;
        final RfLintViolationSeverity wrn = RfLintViolationSeverity.WARNING;
        final RfLintViolationSeverity ign = RfLintViolationSeverity.IGNORE;

        assertThat(new RfLintRule("Rule", err, "", "docu").getConfigurationSwitches()).isEmpty();
        assertThat(new RfLintRule("Rule", wrn, "", "docu").getConfigurationSwitches()).isEmpty();
        assertThat(new RfLintRule("Rule", ign, "", "docu").getConfigurationSwitches()).isEmpty();

        assertThat(new RfLintRule("Rule", err, "", "docu").configure(wrn).getConfigurationSwitches())
                .containsExactly("-w", "Rule");
        assertThat(new RfLintRule("Rule", wrn, "", "docu").configure(ign).getConfigurationSwitches())
                .containsExactly("-i", "Rule");
        assertThat(new RfLintRule("Rule", ign, "", "docu").configure(err).getConfigurationSwitches())
                .containsExactly("-e", "Rule");

        assertThat(new RfLintRule("Rule", err, "", "docu").configure("a").getConfigurationSwitches())
                .containsExactly("-c", "Rule:a");
        assertThat(new RfLintRule("Rule", wrn, "", "docu").configure("b").getConfigurationSwitches())
                .containsExactly("-c", "Rule:b");

        assertThat(new RfLintRule("Rule", err, "", "docu").configure(wrn).configure("a").getConfigurationSwitches())
                .containsExactly("-w", "Rule", "-c", "Rule:a");
        assertThat(new RfLintRule("Rule", ign, "", "docu").configure(err).configure("c").getConfigurationSwitches())
                .containsExactly("-e", "Rule", "-c", "Rule:c");
    }

    @Test
    public void ignoredRuleDoesNotHaveRuleConfiguration() {
        final RfLintViolationSeverity err = RfLintViolationSeverity.ERROR;
        final RfLintViolationSeverity wrn = RfLintViolationSeverity.WARNING;
        final RfLintViolationSeverity ign = RfLintViolationSeverity.IGNORE;

        assertThat(new RfLintRule("Rule", ign, "", "docu").configure("c").getConfigurationSwitches()).isEmpty();
        assertThat(new RfLintRule("Rule", wrn, "", "docu").configure(ign).configure("b").getConfigurationSwitches())
                .containsExactly("-i", "Rule");
        assertThat(new RfLintRule("Rule", err, "", "docu").configure(ign).configure("c").getConfigurationSwitches())
                .containsExactly("-i", "Rule");

    }

}
