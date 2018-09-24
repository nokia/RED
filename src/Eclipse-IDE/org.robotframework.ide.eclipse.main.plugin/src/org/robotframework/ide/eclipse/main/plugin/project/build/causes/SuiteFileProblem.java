/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.ChangeToFixer;

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
    DEPRECATED_TABLE_HEADER {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.DEPRECATED_API;
        }

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public String getProblemDescription() {
            return "Table header '%s' is deprecated. Use '%s' instead.";
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            final String targetName = marker.getAttribute(AdditionalMarkerAttributes.VALUE, "");
            return newArrayList(new ChangeToFixer(targetName));
        }
    },
    UNSUPPORTED_TABLE {

        @Override
        public String getProblemDescription() {
            return "'%s' table is not supported inside %s file";
        }
    },
    DEPRECATED_TEST_SUITE_FILE_EXTENSION {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.DEPRECATED_API;
        }

        @Override
        public String getProblemDescription() {
            return "The '*.%s' file extension is deprecated. Only '*.robot' should be used.";
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
