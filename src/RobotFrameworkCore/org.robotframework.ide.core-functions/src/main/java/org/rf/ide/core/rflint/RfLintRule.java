package org.rf.ide.core.rflint;

public class RfLintRule {

    private String ruleName;

    private RfLintViolationSeverity severity;

    private String configuration;

    public RfLintRule(final String ruleName, final RfLintViolationSeverity severity, final String configuration) {
        this.ruleName = ruleName;
        this.severity = severity;
        this.configuration = configuration;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(final String ruleName) {
        this.ruleName = ruleName;
    }

    public RfLintViolationSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(final RfLintViolationSeverity severity) {
        this.severity = severity;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(final String configuration) {
        this.configuration = configuration;
    }

    public boolean isDead() {
        return !hasChangedSeverity() && !hasConfigurationArguments();
    }

    public boolean hasChangedSeverity() {
        return severity != RfLintViolationSeverity.DEFAULT;
    }

    public boolean hasConfigurationArguments() {
        return !configuration.trim().isEmpty();
    }
}