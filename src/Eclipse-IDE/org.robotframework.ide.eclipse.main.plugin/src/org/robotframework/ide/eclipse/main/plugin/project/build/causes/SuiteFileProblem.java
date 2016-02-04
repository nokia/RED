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

public enum SuiteFileProblem implements IProblemCause {
    SUITE_FILE_IS_NAMED_INIT {

        @Override
        public String getProblemDescription() {
            return "Suite initialization file shouldn't contain Test Cases section";
        }
    },
    UNRECOGNIZED_TABLE_HEADER {
        @Override
        public Severity getSeverity() {
            return Severity.WARNING;
        }
        
        @Override
        public String getProblemDescription() {
            return "Unrecognized table header: '%s'";
        }
    },
    BUILD_ERROR_MESSAGE {
        
        @Override
        public boolean hasResolution() {
            return false;
        }

        @Override
        public String getProblemDescription() {
            return "Parser error in '%s': %s";
        }
    },
    BUILD_WARNING_MESSAGE {
        
        @Override
        public boolean hasResolution() {
            return false;
        }
        
        @Override
        public Severity getSeverity() {
            return Severity.WARNING;
        }

        @Override
        public String getProblemDescription() {
            return "Parser warning in '%s': %s";
        }
    },
    FILE_PARSING_FAILED {

        @Override
        public boolean hasResolution() {
            return false;
        }
        
        @Override
        public String getProblemDescription() {
            return "Parsing file '%s' failed";
        }
    };

    @Override
    public boolean hasResolution() {
        return true;
    }

    @Override
    public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
        return newArrayList();
    }

    @Override
    public Severity getSeverity() {
        return Severity.ERROR;
    }

    @Override
    public ProblemCategory getProblemCategory() {
        return null;
    }

    @Override
    public String getEnumClassName() {
        return SuiteFileProblem.class.getName();
    }
}
