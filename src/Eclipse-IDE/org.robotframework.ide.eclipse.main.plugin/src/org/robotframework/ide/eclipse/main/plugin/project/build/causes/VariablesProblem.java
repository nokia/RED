package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.ui.IMarkerResolution;


public enum VariablesProblem implements IProblemCause {
    DUPLICATED_VARIABLE {
        @Override
        public String getProblemDescription() {
            return "Duplicated variable definition '%s'";
        }
    },
    INVALID_TYPE {
        @Override
        public String getProblemDescription() {
            return "Invalid variable definition '%s'. Unable to recognize variable type";
        }
    },
    DICTIONARY_NOT_AVAILABLE {
        @Override
        public String getProblemDescription() {
            return "Invalid variable definition '%s'. Dictionary type is available since Robot Framework 2.9.x version";
        }
    },
    SCALAR_WITH_MULTIPLE_VALUES_2_7 {
        @Override
        public Severity getSeverity() {
            return Severity.WARNING;
        }

        @Override
        public String getProblemDescription() {
            return "Scalar variable '%s' is initialized with list value";
        }
    },
    SCALAR_WITH_MULTIPLE_VALUES_2_8_x {
        @Override
        public String getProblemDescription() {
            return "Invalid variable definition '%s'. Scalar variable cannot have multiple value in RobotFramework 2.8.x";
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
    public List<? extends IMarkerResolution> createFixers() {
        return newArrayList();
    }

    @Override
    public ProblemCategory getProblemCategory() {
        return null;
    }

    @Override
    public String getEnumClassName() {
        return VariablesProblem.class.getName();
    }
}
