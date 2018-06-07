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
public enum ArgumentProblem implements IProblemCause {
    INVALID_ARGUMENTS_DESCRIPTOR {

        @Override
        public String getProblemDescription() {
            return "Keyword '%s' has invalid arguments descriptor";
        }
    },
    INVALID_NUMBER_OF_PARAMETERS {

        @Override
        public String getProblemDescription() {
            return "Invalid number of arguments. %s";
        }
    },
    INVALID_NUMBER_OF_NON_KEYWORD_PARAMETERS {

        @Override
        public String getProblemDescription() {
            return "Invalid number of non-keyword arguments. %s";
        }
    },
    POSITIONAL_ARGUMENT_AFTER_NAMED {

        @Override
        public String getProblemDescription() {
            return "Positional argument cannot be used after named arguments%s";
        }
    },
    MULTIPLE_MATCH_TO_SINGLE_ARG {

        @Override
        public String getProblemDescription() {
            return "Argument '%s' has value already passed (%s)";
        }
    },
    OVERRIDDEN_NAMED_ARGUMENT {
        
        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.OVERRIDDEN_ARGUMENTS;
        }

        @Override
        public String getProblemDescription() {
            return "Argument '%s' is passed multiple times using named syntax. This value will never be used";
        }
    },
    NO_VALUE_PROVIDED_FOR_REQUIRED_ARG {

        @Override
        public String getProblemDescription() {
            return "Keyword '%s' requires %s to be specified";
        }
    },
    COLLECTION_ARGUMENT_SHOULD_PROVIDE_ARGS {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.COLLECTION_ARGUMENT_SIZE;
        }

        @Override
        public String getProblemDescription() {
            return "%s argument '%s' %s";
        }
    },
    INVALID_TIME_FORMAT {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.TIME_FORMAT;
        }

        @Override
        public String getProblemDescription() {
            return "Invalid time format '%s'";
        }
    },
    INVALID_VARIABLE_SYNTAX {

        @Override
        public String getProblemDescription() {
            return "Invalid variable syntax '%s'";
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
        return ArgumentProblem.class.getName();
    }
}
