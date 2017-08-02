/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import java.util.ArrayList;
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
        public String getProblemDescription() {
            return "Unrecognized table header: '%s'";
        }

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.UNRECOGNIZED_HEADER;
        }
    },
    BUILD_ERROR_MESSAGE {

        @Override
        public String getProblemDescription() {
            return "%s";
        }
    },
    BUILD_WARNING_MESSAGE {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.PARSER_WARNING;
        }

        @Override
        public String getProblemDescription() {
            return "%s";
        }
    },
    FILE_PARSING_FAILED {

        @Override
        public String getProblemDescription() {
            return "Parsing file '%s' failed";
        }
    };

    @Override
    public boolean hasResolution() {
        return false;
    }

    @Override
    public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
        return new ArrayList<>();
    }

    @Override
    public ProblemCategory getProblemCategory() {
        return ProblemCategory.RUNTIME_ERROR;
    }

    @Override
    public String getEnumClassName() {
        return SuiteFileProblem.class.getName();
    }
}
