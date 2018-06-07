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
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.RemoveLibraryFromConfigurationFileFixer;

public enum ConfigFileProblem implements IProblemCause {
    INVALID_VERSION {

        @Override
        public String getProblemDescription() {
            return "Red.xml file is in version %s, but %s expected";
        }
    },
    UNREACHABLE_HOST {

        @Override
        public String getProblemDescription() {
            return "Unreachable remote server %s";
        }
    },
    ABSOLUTE_PATH {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.ABSOLUTE_PATH;
        }

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new RemoveLibraryFromConfigurationFileFixer());
        }

        @Override
        public String getProblemDescription() {
            return "The path %s is absolute. RED prefers using workspace-relative paths which makes your projects more portable";
        }
    },
    MISSING_LIBRARY_FILE {

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new RemoveLibraryFromConfigurationFileFixer());
        }

        @Override
        public String getProblemDescription() {
            return "Missing library file '%s'. Keywords from this library will not be accessible";
        }
    },
    MISSING_VARIABLE_FILE {

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new RemoveLibraryFromConfigurationFileFixer());
        }

        @Override
        public String getProblemDescription() {
            return "Missing variable file '%s'. Variables from this file will not be accessible";
        }
    },
    MISSING_EXCLUDED_FOLDER {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.MISSING_PATH;
        }

        @Override
        public String getProblemDescription() {
            return "Missing excluded resource '%s'";
        }
    },
    JAVA_LIB_NOT_A_JAR_FILE {

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new RemoveLibraryFromConfigurationFileFixer());
        }

        @Override
        public String getProblemDescription() {
            return "The path '%s' for Java library should point to .jar file. Keywords from this library will not be accessible";
        }
    },
    JAVA_LIB_IN_NON_JAVA_ENV {

        @Override
        public String getProblemDescription() {
            return "Java library '%s' requires Jython, but %s environment is in use by this project";
        }
    },
    USELESS_FOLDER_EXCLUSION {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.DUPLICATED_PATH;
        }

        @Override
        public String getProblemDescription() {
            return "The path '%s' is already excluded by '%s'";
        }
    },
    MISSING_SEARCH_PATH {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.MISSING_PATH;
        }

        @Override
        public String getProblemDescription() {
            return "The path '%s' points to non-existing location";
        }
    },
    INVALID_SEARCH_PATH {

        @Override
        public String getProblemDescription() {
            return "The path '%s' is invalid";
        }
    },
    LIBRARY_SPEC_CANNOT_BE_GENERATED {

        @Override
        public String getProblemDescription() {
            return "Library specification file was not generated for '%s' library";
        }

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.LIBRARY_SPECIFICATION_FILE;
        }
    };

    public static final String LIBRARY_INDEX = "marker.libraryIndex";

    @Override
    public boolean hasResolution() {
        return false;
    }

    @Override
    public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
        return new ArrayList<>();
    }

    @Override
    public String getEnumClassName() {
        return ConfigFileProblem.class.getName();
    }

    @Override
    public ProblemCategory getProblemCategory() {
        return ProblemCategory.RUNTIME_ERROR;
    }
}
