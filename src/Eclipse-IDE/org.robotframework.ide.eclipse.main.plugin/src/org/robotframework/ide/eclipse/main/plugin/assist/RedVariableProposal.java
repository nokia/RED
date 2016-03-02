/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;

import com.google.common.base.Objects;

public final class RedVariableProposal {

    private final String name;

    private final String source;

    private final String value;

    private final String comment;

    private final VariableType type;

    private RedVariableProposal(final String name, final String source, final String value, final String comment,
            final VariableType type) {
        this.name = name;
        this.source = source;
        this.value = value;
        this.comment = comment;
        this.type = type;
    }

    static RedVariableProposal create(final RobotVariable robotVariable) {
        return new RedVariableProposal(robotVariable.getPrefix() + robotVariable.getName() + robotVariable.getSuffix(),
                robotVariable.getSuiteFile().getName(), robotVariable.getValue(), robotVariable.getComment(),
                VariableType.LOCAL);
    }

    static RedVariableProposal createLocal(final String name, final String path) {
        if (name.contains("}=")) {
            return new RedVariableProposal(name.substring(0, name.indexOf("}=") + 1), path, "", "", VariableType.LOCAL);
        } else {
            return new RedVariableProposal(name, path, "", "", VariableType.LOCAL);
        }
    }

    static RedVariableProposal create(final String name, final String value, final String path) {
        return new RedVariableProposal(name, path, value, "", VariableType.IMPORTED);
    }

    public static RedVariableProposal createBuiltIn(final String name, final String value) {
        return new RedVariableProposal(name, "built-in", value, "", VariableType.BUILTIN);
    }

    public String getName() {
        return name;
    }

    public String getSource() {
        return source;
    }

    public String getValue() {
        return value;
    }

    public String getComment() {
        return comment;
    }

    public VariableType getType() {
        return type;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() == RedVariableProposal.class) {
            final RedVariableProposal that = (RedVariableProposal) obj;
            return Objects.equal(this.name, that.name) && Objects.equal(this.source, that.source)
                    && Objects.equal(this.value, that.value) && Objects.equal(this.comment, that.comment)
                    && this.type == that.type;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(name, source, value, comment, type);
    }

    public enum VariableType {
        LOCAL,
        IMPORTED,
        BUILTIN
    }
}
