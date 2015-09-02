package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.ui.IMarkerResolution;

public enum SuiteFileProblem implements IProblemCause {
    SUITE_FILE_IS_NAMED_INIT {

        @Override
        public String getProblemDescription() {
            return "Suite initialization file shouldn't contain Test Cases section";
        }
    };

    @Override
    public boolean hasResolution() {
        return true;
    }

    @Override
    public List<? extends IMarkerResolution> createFixers() {
        return newArrayList();
    }

    @Override
    public Severity getSeverity() {
        return Severity.ERROR;
    }

    @Override
    public ProblemCategory getProblemCategory() {
        return null;
    }

    @Override
    public String getEnumClassName() {
        return SuiteFileProblem.class.getName();
    }
}
