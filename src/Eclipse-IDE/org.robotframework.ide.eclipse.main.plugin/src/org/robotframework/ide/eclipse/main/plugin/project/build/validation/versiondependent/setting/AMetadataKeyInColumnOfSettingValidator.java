/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.setting;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.VersionDependentModelUnitValidator;

public abstract class AMetadataKeyInColumnOfSettingValidator extends VersionDependentModelUnitValidator {

    private final IFile file;

    private final RobotSettingsSection section;

    private final ProblemsReportingStrategy reporter;

    public AMetadataKeyInColumnOfSettingValidator(final IFile file, final RobotSettingsSection section,
            final ProblemsReportingStrategy reporter) {
        this.file = file;
        this.section = section;
        this.reporter = reporter;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        final SettingTable table = (SettingTable) section.getLinkedElement();

        final List<Metadata> metadatas = table.getMetadatas();
        for (final Metadata metadata : metadatas) {
            RobotToken settingDeclaration = metadata.getDeclaration();
            String settingText = settingDeclaration.getText();
            if ("meta:".equalsIgnoreCase(settingText.trim())) {
                if (settingDeclaration.getEndColumn() + 1 == metadata.getKey().getStartColumn()) {
                    reporter.handleProblem(RobotProblem.causedBy(getSettingProblemId())
                            .formatMessageWith(settingDeclaration.getText()), file, settingDeclaration);
                }
            }
        }
    }

    public abstract IProblemCause getSettingProblemId();
}
