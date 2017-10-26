package org.rf.ide.core.rflint;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class RfLintRuleTest {

    @Test
    public void gettersTest() {
        assertThat(new RfLintRule("Rule", RfLintViolationSeverity.ERROR, "config").getRuleName()).isEqualTo("Rule");
        assertThat(new RfLintRule("Rule", RfLintViolationSeverity.ERROR, "config").getSeverity())
                .isEqualTo(RfLintViolationSeverity.ERROR);
        assertThat(new RfLintRule("Rule", RfLintViolationSeverity.ERROR, "config").getConfiguration())
                .isEqualTo("config");
    }

    @Test
    public void settersTest() {
        final RfLintRule rule = new RfLintRule("Rule", RfLintViolationSeverity.ERROR, "config");

        rule.setRuleName("OtherRule");
        assertThat(rule.getRuleName()).isEqualTo("OtherRule");

        rule.setSeverity(RfLintViolationSeverity.WARNING);
        assertThat(rule.getSeverity()).isEqualTo(RfLintViolationSeverity.WARNING);

        rule.setConfiguration("42");
        assertThat(rule.getConfiguration()).isEqualTo("42");
    }

    @Test
    public void ruleHasConfiguration_ifTheConfigIsNonEmpty() {
        assertThat(new RfLintRule("Rule", RfLintViolationSeverity.ERROR, "config").hasConfigurationArguments())
                .isTrue();
        assertThat(new RfLintRule("Rule", RfLintViolationSeverity.ERROR, "a").hasConfigurationArguments()).isTrue();
        assertThat(new RfLintRule("Rule", RfLintViolationSeverity.ERROR, "").hasConfigurationArguments()).isFalse();
        assertThat(new RfLintRule("Rule", RfLintViolationSeverity.ERROR, "  ").hasConfigurationArguments()).isFalse();
    }

    @Test
    public void ruleHasChangedSeverity_ifTheSeverityIsNonDefault() {
        assertThat(new RfLintRule("Rule", RfLintViolationSeverity.ERROR, "config").hasChangedSeverity()).isTrue();
        assertThat(new RfLintRule("Rule", RfLintViolationSeverity.WARNING, "config").hasChangedSeverity()).isTrue();
        assertThat(new RfLintRule("Rule", RfLintViolationSeverity.DEFAULT, "config").hasChangedSeverity()).isFalse();
    }

    @Test
    public void ruleIsDead_ifItHasNoConfigurationAndSeverityRemainsUnchanged() {
        assertThat(new RfLintRule("Rule", RfLintViolationSeverity.DEFAULT, "").isDead()).isTrue();
        assertThat(new RfLintRule("Rule", RfLintViolationSeverity.DEFAULT, "config").isDead()).isFalse();
        assertThat(new RfLintRule("Rule", RfLintViolationSeverity.ERROR, "").isDead()).isFalse();
        assertThat(new RfLintRule("Rule", RfLintViolationSeverity.ERROR, "config").isDead()).isFalse();

    }

}
