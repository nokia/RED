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
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.ADocumentDeprecatedDeclarationValidator;

public class DocumentationDeclarationSettingValidator extends ADocumentDeprecatedDeclarationValidator {

    private final RobotSettingsSection section;

    public DocumentationDeclarationSettingValidator(final IFile file, final RobotSettingsSection section,
            final ValidationReportingStrategy reporter) {
        super(file, reporter);
        this.section = section;
    }

    @Override
    public IProblemCause getSettingProblemId() {
        return GeneralSettingsProblem.DOCUMENT_SYNONYM;
    }

    @Override
    public List<RobotToken> getDocumentationDeclaration() {
        final List<RobotToken> documentationDec = new ArrayList<>(0);
        final SettingTable settingTable = section.getLinkedElement();
        if (settingTable.isPresent()) {
            final List<SuiteDocumentation> documentation = settingTable.getDocumentation();
            for (final SuiteDocumentation sd : documentation) {
                documentationDec.add(sd.getDeclaration());
            }
        }

        return documentationDec;
    }
}
