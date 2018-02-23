/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.testcases;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestDocumentation;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.TestCasesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.ADocumentDeprecatedDeclarationValidator;

public class DocumentationTestCaseDeclarationSettingValidator extends ADocumentDeprecatedDeclarationValidator {

    private final RobotCasesSection section;

    public DocumentationTestCaseDeclarationSettingValidator(final IFile file, final RobotCasesSection section,
            final ValidationReportingStrategy reporter) {
        super(file, reporter);
        this.section = section;
    }

    @Override
    public IProblemCause getSettingProblemId() {
        return TestCasesProblem.DOCUMENT_SYNONYM;
    }

    @Override
    public List<RobotToken> getDocumentationDeclaration() {
        final List<RobotToken> documentationDec = new ArrayList<>(0);
        final TestCaseTable testCaseTable = section.getLinkedElement();
        if (testCaseTable.isPresent()) {
            final List<TestCase> testCases = testCaseTable.getTestCases();
            for (final TestCase testCase : testCases) {
                final List<TestDocumentation> documentation = testCase.getDocumentation();
                for (final TestDocumentation testDocumentation : documentation) {
                    documentationDec.add(testDocumentation.getDeclaration());
                }
            }
        }

        return documentationDec;
    }
}
