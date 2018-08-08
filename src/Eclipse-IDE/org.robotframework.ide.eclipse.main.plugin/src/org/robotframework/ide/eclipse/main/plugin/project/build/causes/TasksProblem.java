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

/**
 * @author Michal Anglart
 */
public enum TasksProblem implements IProblemCause {
    DUPLICATED_TASK {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.DUPLICATED_TEST_OR_TASK;
        }

        @Override
        public String getProblemDescription() {
            return "Duplicated task definition '%s'";
        }
    },
    EMPTY_TASK_NAME {

        @Override
        public String getProblemDescription() {
            return "Task name cannot be empty";
        }
    },
    EMPTY_TASK {

        @Override
        public String getProblemDescription() {
            return "Task '%s' contains no keywords to execute";
        }
    },
    UNKNOWN_TASK_SETTING {

        @Override
        public String getProblemDescription() {
            return "Unknown task setting definition '%s'";
        }
    },
    EMPTY_TASK_SETTING {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.EMPTY_SETTINGS;
        }

        @Override
        public String getProblemDescription() {
            return "The %s task setting is empty";
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
        return TasksProblem.class.getName();
    }
}
