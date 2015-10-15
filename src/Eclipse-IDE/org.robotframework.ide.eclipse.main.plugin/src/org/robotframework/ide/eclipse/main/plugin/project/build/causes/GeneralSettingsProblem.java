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

public enum GeneralSettingsProblem implements IProblemCause {
    UNKNOWN_SETTING {
        @Override
        public String getProblemDescription() {
            return "Unknwon '%s' setting";
        }
    },
    UNSUPPORTED_SETTING {
        @Override
        public String getProblemDescription() {
            return "The setting '%s' is not supported inside %s file";
        }
    },
    MISSING_LIBRARY_NAME {
        @Override
        public String getProblemDescription() {
            return "Empty setting '%s'. Specify name or path of library to import";
        }
    },
    MISSING_RESOURCE_NAME {
        @Override
        public String getProblemDescription() {
            return "Empty setting '%s'. Specify path of resource file to import";
        }
    },
    MISSING_VARIABLES_NAME {
        @Override
        public String getProblemDescription() {
            return "Empty setting '%s'. Specify path of variable file to import";
        }
    },
    PARAMETERIZED_IMPORT_PATH {
        @Override
        public String getProblemDescription() {
            return "The library name/path '%s' is parameterized. RED currently does not support such imports";
        }
    },
    ABSOLUTE_IMPORT_PATH {
        @Override
        public Severity getSeverity() {
            return Severity.WARNING;
        }

        @Override
        public String getProblemDescription() {
            return "Path '%s' is absolute. RED prefers relative paths";
        }
    },
    IMPORT_PATH_OUTSIDE_WORKSPACE {
        @Override
        public String getProblemDescription() {
            return "Path '%s' points to location outside your workspace";
        }
    },
    UNKNOWN_LIBRARY {
        @Override
        public String getProblemDescription() {
            return "Unknown '%s' library";
        }
    },
    SETTING_ARGUMENTS_NOT_APPLICABLE {
        @Override
        public String getProblemDescription() {
            return "Setting '%s' is not applicable for arguments: %s. %s";
        }
    },
    INVALID_RESOURCE_IMPORT {

        @Override
        public String getProblemDescription() {
            return "Resource import '%s' is invalid%s";
        }
    },
    INVALID_VARIABLES_IMPORT {

        @Override
        public String getProblemDescription() {
            return "Variable import '%s' is invalid%s";
        }
    },
    EMPTY_SETTING {
        @Override
        public Severity getSeverity() {
            return Severity.WARNING;
        }

        @Override
        public String getProblemDescription() {
            return "Empty setting '%s'";
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
        return GeneralSettingsProblem.class.getName();
    }
}
