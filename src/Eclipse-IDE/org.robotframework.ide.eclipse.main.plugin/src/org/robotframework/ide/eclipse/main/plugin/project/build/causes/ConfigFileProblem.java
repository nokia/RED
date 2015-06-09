package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.ui.IMarkerResolution;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.build.IProblemCause;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.CreateConfigurationFileFixer;

public enum ConfigFileProblem implements IProblemCause {
    DOES_NOT_EXIST {
        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers() {
            return newArrayList(new CreateConfigurationFileFixer());
        }

        @Override
        public String getFormattedProblemDescription(final Object... objects) {
            return "FATAL: project configuration file " + RobotProjectConfig.FILENAME + " does not exist";
        }


    },
    OTHER_PROBLEM {
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
            return String.format(
                    "FATAL: unable to read configuration file. %s Fix this problem in order to properly build project",
                            objects);
        }
    };

    @Override
    public Severity getSeverity() {
        return Severity.ERROR;
    }

    @Override
    public String getEnumClassName() {
        return ConfigFileProblem.class.getName();
    }
}
