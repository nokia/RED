package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;

class GeneralSettingsTableValidator {

    private final IFile file;

    private final ProblemsReportingStrategy reporter = new ProblemsReportingStrategy();

    GeneralSettingsTableValidator(final IFile file) {
        this.file = file;
    }

    void validate(final SettingTable settingTable, final IProgressMonitor monitor) throws CoreException {
        if (!settingTable.isPresent()) {
            return;
        }
    }
}
