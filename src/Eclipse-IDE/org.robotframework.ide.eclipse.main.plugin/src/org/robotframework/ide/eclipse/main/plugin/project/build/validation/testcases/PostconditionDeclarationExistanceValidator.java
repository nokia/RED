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
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTeardown;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.TestCasesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.ADeprecatedSettingElement;

public class PostconditionDeclarationExistanceValidator extends ADeprecatedSettingElement {

    private final RobotCasesSection section;

    public PostconditionDeclarationExistanceValidator(final IFile file, final ProblemsReportingStrategy reporter,
            final RobotCasesSection section) {
        super(file, reporter, "Postcondition");
        this.section = section;
    }

    @Override
    public IProblemCause getProblemId() {
        return TestCasesProblem.POSTCONDITION_SYNONIM;
    }

    @Override
    public List<RobotToken> getDeclaration() {
        final List<RobotToken> declarations = new ArrayList<>(0);
        final TestCaseTable testCaseTable = (TestCaseTable) section.getLinkedElement();
        if (testCaseTable.isPresent()) {
            for (final TestCase tc : testCaseTable.getTestCases()) {
                for (final TestCaseTeardown teardown : tc.getTeardowns()) {
                    declarations.add(teardown.getDeclaration());
                }
            }
        }

        return declarations;
    }
}
