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
import org.rf.ide.core.testdata.model.table.setting.TestSetup;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.ADeprecatedSettingElement;

public class TestPreconditionDeclarationExistanceValidator extends ADeprecatedSettingElement {

    private final RobotSettingsSection section;

    public TestPreconditionDeclarationExistanceValidator(final IFile file, final ProblemsReportingStrategy reporter,
            final RobotSettingsSection section) {
        super(file, reporter, "Test Precondition");
        this.section = section;
    }

    @Override
    public IProblemCause getProblemId() {
        return GeneralSettingsProblem.TEST_PRECONDITION_SYNONIM;
    }

    @Override
    public List<RobotToken> getDeclaration() {
        List<RobotToken> declarations = new ArrayList<>(0);
        SettingTable settingTable = (SettingTable) section.getLinkedElement();
        if (settingTable.isPresent()) {
            for (final TestSetup setup : settingTable.getTestSetups()) {
                declarations.add(setup.getDeclaration());
            }
        }

        return declarations;
    }
}
