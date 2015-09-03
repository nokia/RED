package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;

class TestCasesTableValidator {

    private final IFile file;

    private final ProblemsReportingStrategy reporter = new ProblemsReportingStrategy();

    TestCasesTableValidator(final IFile file) {
        this.file = file;
    }

    void validate(final TestCaseTable testCaseTable, final IProgressMonitor monitor) throws CoreException {
        if (!testCaseTable.isPresent()) {
            return;
        }
    }
}
