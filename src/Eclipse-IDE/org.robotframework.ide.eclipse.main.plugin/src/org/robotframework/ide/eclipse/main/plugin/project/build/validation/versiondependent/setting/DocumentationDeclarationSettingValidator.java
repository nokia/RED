/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.setting;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.ADocumentDepracatedDeclarationValidator;

public class DocumentationDeclarationSettingValidator extends ADocumentDepracatedDeclarationValidator {

    private final RobotSettingsSection section;

    public DocumentationDeclarationSettingValidator(final IFile file, final RobotSettingsSection section,
            final ProblemsReportingStrategy reporter) {
        super(file, reporter);
        this.section = section;
    }

    @Override
    public IProblemCause getSettingProblemId() {
        return GeneralSettingsProblem.DEPRACATED_DOCUMENT_WORD_FROM_30;
    }

    @Override
    public List<RobotToken> getDocumentationDeclaration() {
        List<RobotToken> documentationDec = new ArrayList<>(0);
        SettingTable settingTable = (SettingTable) section.getLinkedElement();
        if (settingTable.isPresent()) {
            List<SuiteDocumentation> documentation = settingTable.getDocumentation();
            for (SuiteDocumentation sd : documentation) {
                documentationDec.add(sd.getDeclaration());
            }
        }

        return documentationDec;
    }
}
