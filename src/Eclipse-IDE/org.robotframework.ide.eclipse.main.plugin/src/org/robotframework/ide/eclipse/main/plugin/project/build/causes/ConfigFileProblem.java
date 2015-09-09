/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.ui.IMarkerResolution;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.RemoveLibraryFromConfigurationFileFixer;

public enum ConfigFileProblem implements IProblemCause {
    UNREACHABLE_HOST {
        @Override
        public boolean hasResolution() {
            return false;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers() {
            return newArrayList();
        }

        @Override
        public String getProblemDescription() {
            return "Unreachable remote server %s";
        }
    },
    MISSING_JAR_FILE {

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers() {
            return newArrayList(new RemoveLibraryFromConfigurationFileFixer());
        }

        @Override
        public String getProblemDescription() {
            return "Missing Java library file '%s'. Keywords from this libary will not be visible";
        }
    },
    JAVA_LIB_NOT_A_JAR_FILE {

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers() {
            return newArrayList(new RemoveLibraryFromConfigurationFileFixer());
        }

        @Override
        public String getProblemDescription() {
            return "The path '%s' for Java library should point to .jar file. Keywords from this libary will not be visible";
        }
    },
    JAVA_LIB_MISSING_CLASS {
        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers() {
            return newArrayList(new RemoveLibraryFromConfigurationFileFixer());
        }

        @Override
        public String getProblemDescription() {
            return "Java library '%s' does not contain class '%s'. Keywords from this libary will not be visible";
        }
    },
    ABSOLUTE_PATH {
        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers() {
            return newArrayList(new RemoveLibraryFromConfigurationFileFixer());
        }

        @Override
        public String getProblemDescription() {
            return "The path %s is absolute but all libspec files for "
                    + "given project should be placed in the workspace. Keywords from this library will not be visible";
        }
    },
    MISSING_LIBSPEC_FILE {
        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers() {
            return newArrayList(new RemoveLibraryFromConfigurationFileFixer());
        }

        @Override
        public String getProblemDescription() {
            return "Missing library specification file '%s'. Keywords from this libary will not be visible";
        }
    };

    public static final String LIBRARY_INDEX = "marker.libraryIndex";

    @Override
    public Severity getSeverity() {
        return Severity.ERROR;
    }

    @Override
    public String getEnumClassName() {
        return ConfigFileProblem.class.getName();
    }

    @Override
    public ProblemCategory getProblemCategory() {
        return null;
    }
}
