/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.rflint;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.google.common.base.Strings;

public class RfLintRule {

    private final String ruleName;

    private final RfLintViolationSeverity defaultSeverity;

    private final String filepath;

    private final String documentation;

    private RfLintRuleConfiguration configuration;

    public RfLintRule(final String ruleName, final RfLintViolationSeverity defaultSeverity, final String filepath,
            final String documentation) {
        this(ruleName, defaultSeverity, filepath, documentation, null);
    }

    public RfLintRule(final String ruleName, final RfLintViolationSeverity defaultSeverity, final String filepath,
            final String documentation, final RfLintRuleConfiguration configuration) {
        this.ruleName = ruleName;
        this.defaultSeverity = defaultSeverity;
        this.filepath = filepath;
        this.documentation = documentation;
        this.configuration = configuration;
    }

    public String getRuleName() {
        return ruleName;
    }

    public RfLintViolationSeverity getSeverity() {
        return defaultSeverity;
    }

    public String getFilepath() {
        return filepath;
    }

    public String getDocumentation() {
        return documentation;
    }

    public RfLintViolationSeverity getConfiguredSeverity() {
        return isConfigured() ? Optional.ofNullable(configuration.getSeverity()).orElse(defaultSeverity)
                : defaultSeverity;
    }

    public String getConfiguredArguments() {
        return isConfigured() ? Strings.nullToEmpty(configuration.getArguments()) : "";
    }

    public boolean isConfigured() {
        return configuration != null;
    }

    public RfLintRuleConfiguration getConfiguration() {
        return configuration;
    }

    public RfLintRule configure(final RfLintViolationSeverity severity) {
        if (configuration == null) {
            configuration = new RfLintRuleConfiguration();
        }
        configuration.setSeverity(severity == defaultSeverity ? null : severity);
        if (configuration.isEmpty()) {
            configuration = null;
        }
        return this;
    }

    public RfLintRule configure(final String arguments) {
        if (configuration == null) {
            configuration = new RfLintRuleConfiguration();
        }
        configuration.setArguments(arguments != null && arguments.trim().isEmpty() ? null : arguments);
        if (configuration.isEmpty()) {
            configuration = null;
        }
        return this;
    }

    public RfLintRule configure(final RfLintRuleConfiguration config) {
        if (config == null) {
            configuration = null;
        } else {
            configure(config.getSeverity());
            configure(config.getArguments());
        }
        return this;
    }

    public List<String> getConfigurationSwitches() {
        final List<String> switches = new ArrayList<>();
        if (isConfigured()) {
            if (configuration.getSeverity() != null && defaultSeverity != configuration.getSeverity()) {
                switches.add("-" + configuration.getSeverity().severitySwitch());
                switches.add(ruleName);
            }
            if (getConfiguredSeverity() != RfLintViolationSeverity.IGNORE && configuration.getArguments() != null
                    && !configuration.getArguments().isEmpty()) {
                switches.add("-c");
                switches.add(ruleName + ":" + configuration.getArguments());
            }
        }
        return switches;
    }

    public RfLintRule copyFresh() {
        return new RfLintRule(ruleName, defaultSeverity, filepath, documentation);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof RfLintRule) {
            final RfLintRule that = (RfLintRule) obj;
            return this.ruleName.equals(that.ruleName) && this.defaultSeverity == that.defaultSeverity
                    && this.filepath.equals(that.filepath)
                    && this.documentation.equals(that.documentation)
                    && Objects.equals(this.configuration, that.configuration);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleName, defaultSeverity, filepath, documentation, configuration);
    }
}