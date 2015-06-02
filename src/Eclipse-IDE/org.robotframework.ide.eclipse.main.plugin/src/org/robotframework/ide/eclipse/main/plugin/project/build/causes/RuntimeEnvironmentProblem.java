package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.ui.IMarkerResolution;
import org.robotframework.ide.eclipse.main.plugin.project.build.IProblemCause;

public enum RuntimeEnvironmentProblem implements IProblemCause {
    MISSING_ENVIRONMENT {
        @Override
        public List<? extends IMarkerResolution> createFixers() {
            return newArrayList();
        }

        @Override
        public String getFormattedProblemDescription(final Object... objects) {
            return null;
        }
    },
    NON_PYTHON_INSTALLATION {
        @Override
        public List<? extends IMarkerResolution> createFixers() {
            return newArrayList();
        }

        @Override
        public String getFormattedProblemDescription(final Object... objects) {
            return String.format("FATAL: %s is not a Python installation directory. Fix this problem to build project",
                    objects);
        }
    },
    MISSING_ROBOT {
        @Override
        public List<? extends IMarkerResolution> createFixers() {
            return newArrayList();
        }

        @Override
        public String getFormattedProblemDescription(final Object... objects) {
            return String.format("FATAL: Python instalation %s has no Robot installed", objects);
        }
    };

    @Override
    public boolean hasResolution() {
        return true;
    }

    @Override
    public Severity getSeverity() {
        return Severity.ERROR;
    }

    @Override
    public String getEnumClassName() {
        return RuntimeEnvironmentProblem.class.getName();
    }
}
