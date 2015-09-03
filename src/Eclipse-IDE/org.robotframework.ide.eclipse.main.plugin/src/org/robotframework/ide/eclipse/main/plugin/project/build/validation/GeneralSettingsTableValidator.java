package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;

class GeneralSettingsTableValidator implements ModelUnitValidator {

    private final IFile file;

    private final SettingTable settingTable;

    private final ProblemsReportingStrategy reporter = new ProblemsReportingStrategy();

    GeneralSettingsTableValidator(final IFile file, final SettingTable settingTable) {
        this.file = file;
        this.settingTable = settingTable;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        if (!settingTable.isPresent()) {
            return;
        }
    }
}
