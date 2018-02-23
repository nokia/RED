/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.setting;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.TestTeardown;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.ADeprecatedSettingElement;

public class TestPostconditionDeclarationExistenceValidator extends ADeprecatedSettingElement {

    private final RobotSettingsSection section;

    public TestPostconditionDeclarationExistenceValidator(final IFile file, final ValidationReportingStrategy reporter,
            final RobotSettingsSection section) {
        super(file, reporter, "Test Postcondition");
        this.section = section;
    }

    @Override
    public IProblemCause getProblemId() {
        return GeneralSettingsProblem.TEST_POSTCONDITION_SYNONYM;
    }

    @Override
    public List<RobotToken> getDeclaration() {
        final List<RobotToken> declarations = new ArrayList<>(0);
        final SettingTable settingTable = section.getLinkedElement();
        if (settingTable.isPresent()) {
            for (final TestTeardown teardown : settingTable.getTestTeardowns()) {
                declarations.add(teardown.getDeclaration());
            }
        }

        return declarations;
    }
}
