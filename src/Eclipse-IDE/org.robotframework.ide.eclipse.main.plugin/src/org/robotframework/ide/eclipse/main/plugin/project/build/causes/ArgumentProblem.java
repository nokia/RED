/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;

/**
 * @author Michal Anglart
 *
 */
public enum ArgumentProblem implements IProblemCause {
    INVALID_NUMBER_OF_PARAMETERS {
        @Override
        public String getProblemDescription() {
            return "Invalid number of parameters. %s";
        }
    },
    POSITIONAL_ARGUMENT_AFTER_NAMED {
        @Override
        public String getProblemDescription() {
            return "Positional argument cannot be used after named arguments";
        }
    },
    INVALID_TIME_FORMAT {

        @Override
        public Severity getSeverity() {
            return Severity.WARNING;
        }

        @Override
        public String getProblemDescription() {
            return "Invalid time format '%s'";
        }
    };

    @Override
    public Severity getSeverity() {
        return Severity.ERROR;
    }

    @Override
    public boolean hasResolution() {
        return false;
    }

    @Override
    public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
        return newArrayList();
    }

    @Override
    public ProblemCategory getProblemCategory() {
        return null;
    }

    @Override
    public String getEnumClassName() {
        return ArgumentProblem.class.getName();
    }
}
