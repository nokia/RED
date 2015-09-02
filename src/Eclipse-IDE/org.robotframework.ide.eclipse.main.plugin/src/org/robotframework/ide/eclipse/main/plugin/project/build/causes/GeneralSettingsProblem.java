package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.ui.IMarkerResolution;

public enum GeneralSettingsProblem implements IProblemCause {
    UNSUPPORTED_SETTING {
        @Override
        public String getProblemDescription() {
            return "The setting '%s' is not supported inside %s file";
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
        return GeneralSettingsProblem.class.getName();
    }
}
