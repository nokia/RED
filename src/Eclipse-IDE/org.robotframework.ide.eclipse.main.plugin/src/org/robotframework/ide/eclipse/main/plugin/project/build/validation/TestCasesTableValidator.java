/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;

class TestCasesTableValidator implements ModelUnitValidator {

    private final IFile file;

    private final TestCaseTable testCaseTable;

    private final ProblemsReportingStrategy reporter = new ProblemsReportingStrategy();

    TestCasesTableValidator(final IFile file, final TestCaseTable testCaseTable) {
        this.file = file;
        this.testCaseTable = testCaseTable;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        if (!testCaseTable.isPresent()) {
            return;
        }
    }
}
