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
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.CreateConfigurationFileFixer;

public enum ProjectConfigurationProblem implements IProblemCause {
    CONFIG_FILE_MISSING {

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new CreateConfigurationFileFixer());
        }

        @Override
        public String getProblemDescription() {
            return "FATAL: project configuration file " + RobotProjectConfig.FILENAME + " does not exist";
        }

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.PROJECT_CONFIGURATION_FILE;
        }
    },
    CONFIG_FILE_READING_PROBLEM {

        @Override
        public String getProblemDescription() {
            return "FATAL: unable to read configuration file. %s Fix this problem in order to properly build project";
        }

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.PROJECT_CONFIGURATION_FILE;
        }
    },
    ENVIRONMENT_MISSING {

        @Override
        public String getProblemDescription() {
            return "FATAL: %s Python environment is not defined in preferences. Fix this problem to build project";
        }

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.MISSING_ROBOT_ENVIRONMENT;
        }
    },
    ENVIRONMENT_NOT_A_PYTHON {

        @Override
        public String getProblemDescription() {
            return "FATAL: %s is not a Python installation directory. Fix this problem to build project";
        }

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.MISSING_ROBOT_ENVIRONMENT;
        }
    },
    ENVIRONMENT_HAS_NO_ROBOT {

        @Override
        public String getProblemDescription() {
            return "FATAL: Python installation %s has no Robot installed. Fix this problem to build project";
        }

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.MISSING_ROBOT_ENVIRONMENT;
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
    public String getEnumClassName() {
        return ProjectConfigurationProblem.class.getName();
    }
}
