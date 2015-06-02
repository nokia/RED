package org.robotframework.ide.eclipse.main.plugin.project.build;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;

public interface IProblemCause {

    Severity getSeverity();

    boolean hasResolution();

    List<? extends IMarkerResolution> createFixers();
    
    String getFormattedProblemDescription(Object... objects);

    String getEnumClassName();

    public enum Severity {
        ERROR {
            @Override
            int getLevel() {
                return IMarker.SEVERITY_ERROR;
            }
        },
        WARNING {
            @Override
            int getLevel() {
                return IMarker.SEVERITY_WARNING;
            }
        },
        INFO {
            @Override
            int getLevel() {
                return IMarker.SEVERITY_INFO;
            }
        };

        abstract int getLevel();
    }
}
