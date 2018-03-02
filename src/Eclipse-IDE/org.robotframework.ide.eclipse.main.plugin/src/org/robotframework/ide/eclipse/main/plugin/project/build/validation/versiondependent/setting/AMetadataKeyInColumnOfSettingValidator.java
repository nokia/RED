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
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.setting.OldMetaSynataxHelper;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.VersionDependentModelUnitValidator;

public abstract class AMetadataKeyInColumnOfSettingValidator extends VersionDependentModelUnitValidator {

    private final IFile file;

    private final RobotSettingsSection section;

    private final ValidationReportingStrategy reporter;

    private final OldMetaSynataxHelper oldMetaHelper;

    public AMetadataKeyInColumnOfSettingValidator(final IFile file, final RobotSettingsSection section,
            final ValidationReportingStrategy reporter) {
        this.file = file;
        this.section = section;
        this.reporter = reporter;
        this.oldMetaHelper = new OldMetaSynataxHelper();
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        final SettingTable table = section.getLinkedElement();

        final List<Metadata> metadatas = table.getMetadatas();
        for (final Metadata metadata : metadatas) {
            if (oldMetaHelper.isOldSyntax(metadata, table)) {
                final RobotToken settingDeclaration = metadata.getDeclaration();
                reporter.handleProblem(
                        RobotProblem.causedBy(getSettingProblemId()).formatMessageWith(settingDeclaration.getText()),
                        file, settingDeclaration);
            }
        }
    }

    public abstract IProblemCause getSettingProblemId();
}
