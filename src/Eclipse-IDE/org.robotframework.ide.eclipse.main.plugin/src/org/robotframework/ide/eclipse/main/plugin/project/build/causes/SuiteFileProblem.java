/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.ChangeToFixer;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.ConvertToRobotFileFormat;

public enum SuiteFileProblem implements IProblemCause {
    INIT_FILE_CONTAINS_TESTS_OR_TASKS {

        @Override
        public String getProblemDescription() {
            return "Suite initialization file shouldn't contain Test Cases nor Tasks section";
        }
    },
    SUITE_FILE_CONTAINS_TASKS {

        @Override
        public String getProblemDescription() {
            return "Tests suite shouldn't contain 'Tasks' section";
        }
    },
    RPA_SUITE_FILE_CONTAINS_TESTS {

        @Override
        public String getProblemDescription() {
            return "Tasks suite shouldn't contain 'Test Cases' section";
        }
    },
    UNRECOGNIZED_TABLE_HEADER {

        @Override
        public String getProblemDescription() {
            return "Unrecognized table header: '%s'%s";
        }

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.UNRECOGNIZED_HEADER;
        }

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            final String wrongName = marker.getAttribute(AdditionalMarkerAttributes.VALUE, "");

            final List<ChangeToFixer> fixers = new ArrayList<>();
            new SimilaritiesAnalyst().provideSimilarSectionNames(wrongName)
                    .stream()
                    .map(name -> "*** " + name + " ***")
                    .map(ChangeToFixer::new)
                    .forEach(fixers::add);

            final String canonicalWrongName = wrongName.replaceAll("\\s", "").toLowerCase();
            if (canonicalWrongName.contains("metadata")) {
                fixers.add(new ChangeToFixer("*** Settings ***"));

            } else if (canonicalWrongName.contains("userkeyword")) {
                fixers.add(new ChangeToFixer("*** Keywords ***"));
            }
            fixers.add(new ChangeToFixer("*** Comments ***"));
            return fixers;
        }
    },
    UNRECOGNIZED_TABLE_HEADER_RF31 {

        @Override
        public String getProblemDescription() {
            return "Unrecognized table header: '%s'";
        }

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.RUNTIME_ERROR;
        }

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return UNRECOGNIZED_TABLE_HEADER.createFixers(marker);
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
    DEPRECATED_SUITE_FILE_EXTENSION {

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
            return "The '*.%s' file extension is deprecated. Only '*.robot' should be used for suites.";
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            final Optional<File> file = RedWorkspace.getLocalFile(marker.getResource());
            return file.isPresent() ? newArrayList(new ConvertToRobotFileFormat(file.get())) : new ArrayList<>();
        }
    },
    REMOVED_SUITE_FILE_EXTENSION {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.REMOVED_API;
        }

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public String getProblemDescription() {
            return "The '*.%s' file extension is not supported. Only '*.robot' should be used for suites. %s";
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            final Optional<File> file = RedWorkspace.getLocalFile(marker.getResource());
            return file.isPresent() ? newArrayList(new ConvertToRobotFileFormat(file.get())) : new ArrayList<>();
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
