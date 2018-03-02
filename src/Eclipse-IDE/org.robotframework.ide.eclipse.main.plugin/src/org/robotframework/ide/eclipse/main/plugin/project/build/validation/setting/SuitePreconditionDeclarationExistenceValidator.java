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
import org.rf.ide.core.testdata.model.table.setting.SuiteSetup;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.ADeprecatedSettingElement;

public class SuitePreconditionDeclarationExistenceValidator extends ADeprecatedSettingElement {

    private final RobotSettingsSection section;

    public SuitePreconditionDeclarationExistenceValidator(final IFile file, final ValidationReportingStrategy reporter,
            final RobotSettingsSection section) {
        super(file, reporter, "Suite Precondition");
        this.section = section;
    }

    @Override
    public IProblemCause getProblemId() {
        return GeneralSettingsProblem.SUITE_PRECONDITION_SYNONYM;
    }

    @Override
    public List<RobotToken> getDeclaration() {
        final List<RobotToken> declarations = new ArrayList<>(0);
        final SettingTable settingTable = section.getLinkedElement();
        if (settingTable.isPresent()) {
            for (final SuiteSetup setup : settingTable.getSuiteSetups()) {
                declarations.add(setup.getDeclaration());
            }
        }

        return declarations;
    }
}
