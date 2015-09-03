package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.robotframework.ide.core.testData.model.table.VariableTable;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;

class VariablesTableValidator {

    private final IFile file;

    private final ProblemsReportingStrategy reporter = new ProblemsReportingStrategy();

    VariablesTableValidator(final IFile file) {
        this.file = file;
    }

    void validate(final VariableTable variableTable, final IProgressMonitor monitor) throws CoreException {
        if (!variableTable.isPresent()) {
            return;
        }
    }
}
