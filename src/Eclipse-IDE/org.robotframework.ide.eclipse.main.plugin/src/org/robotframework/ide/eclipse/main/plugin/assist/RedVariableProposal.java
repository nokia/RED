/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import java.util.Comparator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;

public final class RedVariableProposal {

    private final String name;

    private final String source;

    private final String value;

    private final String comment;

    private final VariableOrigin origin;

    @VisibleForTesting
    RedVariableProposal(final String name, final String source, final String value, final String comment,
            final VariableOrigin origin) {
        this.name = name;
        this.source = source;
        this.value = value;
        this.comment = comment;
        this.origin = origin;
    }

    public static Comparator<RedVariableProposal> variablesSortedByOriginAndNames() {
        return new Comparator<RedVariableProposal>() {

            @Override
            public int compare(final RedVariableProposal proposal1, final RedVariableProposal proposal2) {
                if (proposal1.origin == proposal2.origin) {
                    return proposal1.getName().compareToIgnoreCase(proposal2.getName());
                } else {
                    return proposal1.origin.compareTo(proposal2.origin);
                }
            }
        };
    }

    static RedVariableProposal create(final RobotVariable robotVariable) {
        return new RedVariableProposal(robotVariable.getPrefix() + robotVariable.getName() + robotVariable.getSuffix(),
                robotVariable.getSuiteFile().getName(), robotVariable.getValue(), robotVariable.getComment(),
                VariableOrigin.LOCAL);
    }

    static RedVariableProposal createLocal(final String name, final String path) {
        if (name.contains("}=")) {
            return new RedVariableProposal(name.substring(0, name.indexOf("}=") + 1), path, "", "", VariableOrigin.LOCAL);
        } else {
            return new RedVariableProposal(name, path, "", "", VariableOrigin.LOCAL);
        }
    }

    static RedVariableProposal create(final String name, final String value, final String path) {
        return new RedVariableProposal(name, path, value, "", VariableOrigin.IMPORTED);
    }

    public static RedVariableProposal createBuiltIn(final String name, final String value) {
        return new RedVariableProposal(name, "built-in", value, "", VariableOrigin.BUILTIN);
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

    public ImageDescriptor getImage() {
        if (name.startsWith("&")) {
            return RedImages.getRobotDictionaryVariableImage();
        } else if (name.startsWith("@")) {
            return RedImages.getRobotListVariableImage();
        } else {
            return RedImages.getRobotScalarVariableImage();
        }
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
                    && this.origin == that.origin;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(name, source, value, comment, origin);
    }

    @VisibleForTesting
    enum VariableOrigin {
        LOCAL,
        IMPORTED,
        BUILTIN
    }
}
