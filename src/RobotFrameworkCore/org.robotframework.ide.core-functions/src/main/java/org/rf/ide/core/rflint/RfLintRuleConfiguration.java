/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.rflint;

import java.util.Objects;

public class RfLintRuleConfiguration {

    private RfLintViolationSeverity severity;

    private String arguments;


    public RfLintRuleConfiguration() {
        this(null, null);
    }

    public RfLintRuleConfiguration(final RfLintViolationSeverity severity, final String arguments) {
        this.severity = severity;
        this.arguments = arguments;
    }

    boolean isEmpty() {
        return severity == null && arguments == null;
    }

    public RfLintViolationSeverity getSeverity() {
        return severity;
    }

    public String getArguments() {
        return arguments;
    }

    public void setSeverity(final RfLintViolationSeverity severity) {
        this.severity = severity;
    }

    public void setArguments(final String arguments) {
        this.arguments = arguments;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof RfLintRuleConfiguration) {
            final RfLintRuleConfiguration that = (RfLintRuleConfiguration) obj;
            return Objects.equals(this.arguments, that.arguments) && this.severity == that.severity;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(severity, arguments);
    }
}
