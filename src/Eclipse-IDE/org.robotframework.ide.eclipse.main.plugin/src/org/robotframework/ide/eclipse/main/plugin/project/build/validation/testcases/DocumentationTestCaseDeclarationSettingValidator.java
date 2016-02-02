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
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.TestCasesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.ADocumentDepracatedDeclarationValidator;

public class DocumentationTestCaseDeclarationSettingValidator extends ADocumentDepracatedDeclarationValidator {

    private final RobotCasesSection section;

    public DocumentationTestCaseDeclarationSettingValidator(final IFile file, final RobotCasesSection section,
            ProblemsReportingStrategy reporter) {
        super(file, reporter);
        this.section = section;
    }

    @Override
    public IProblemCause getSettingProblemId() {
        return TestCasesProblem.DOCUMENT_SYNONIM;
    }

    @Override
    public List<RobotToken> getDocumentationDeclaration() {
        List<RobotToken> documentationDec = new ArrayList<>(0);
        TestCaseTable testCaseTable = (TestCaseTable) section.getLinkedElement();
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
