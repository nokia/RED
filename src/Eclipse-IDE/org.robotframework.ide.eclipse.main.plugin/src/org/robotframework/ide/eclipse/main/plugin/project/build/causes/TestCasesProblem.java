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
public enum TestCasesProblem implements IProblemCause {
    DUPLICATED_CASE {
        @Override
        public String getProblemDescription() {
            return "Duplicated test case definition '%s'";
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList();// new RemoveKeywordFixer(marker.getAttribute("name", null)));
        }
    },
    EMPTY_CASE {

        @Override
        public String getProblemDescription() {
            return "Test case '%s' is empty";
        }

        @Override
        public Severity getSeverity() {
            return Severity.ERROR;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList();// new RemoveKeywordFixer(marker.getAttribute("name", null)));
        }
    };

    @Override
    public Severity getSeverity() {
        return Severity.WARNING;
    }

    @Override
    public boolean hasResolution() {
        return false;
    }

    @Override
    public ProblemCategory getProblemCategory() {
        return null;
    }

    @Override
    public String getEnumClassName() {
        return TestCasesProblem.class.getName();
    }
}
