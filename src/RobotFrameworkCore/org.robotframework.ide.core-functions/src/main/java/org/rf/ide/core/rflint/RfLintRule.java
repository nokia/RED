package org.rf.ide.core.rflint;

public class RfLintRule {

    private String ruleName;

    private String severity;

    private String configuration;

    public RfLintRule(final String ruleName, final String severity, final String configuration) {
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

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(final String severity) {
        this.severity = severity;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(final String configuration) {
        this.configuration = configuration;
    }

    public boolean isDead() {
        return "default".equals(severity.toLowerCase()) && configuration.trim().isEmpty();
    }

    public boolean hasChangedSeverity() {
        return !"default".equals(severity.toLowerCase());
    }

    public boolean hasConfigurationArguments() {
        return !configuration.trim().isEmpty();
    }
}