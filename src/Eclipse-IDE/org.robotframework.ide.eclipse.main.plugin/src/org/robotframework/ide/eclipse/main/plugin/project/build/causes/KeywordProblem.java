package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.ui.IMarkerResolution;
import org.robotframework.ide.eclipse.main.plugin.project.build.IProblemCause;

public enum KeywordProblem implements IProblemCause {
    UNKNOWN_KEYWORD;

    @Override
    public Severity getSeverity() {
        return Severity.WARNING;
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
    public String getFormattedProblemDescription(final Object... objects) {
        return "The keyword %s cannot be found";
    }

    @Override
    public String getEnumClassName() {
        return KeywordProblem.class.getName();
    }
}
