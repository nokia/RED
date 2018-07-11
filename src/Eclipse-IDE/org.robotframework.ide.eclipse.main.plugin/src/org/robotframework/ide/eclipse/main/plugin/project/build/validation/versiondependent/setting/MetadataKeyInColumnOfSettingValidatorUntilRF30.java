/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.setting;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.VersionDependentModelUnitValidator;

import com.google.common.collect.Range;

public class MetadataKeyInColumnOfSettingValidatorUntilRF30 extends VersionDependentModelUnitValidator {

    private final IFile file;

    private final RobotSettingsSection section;

    private final ValidationReportingStrategy reporter;

    public MetadataKeyInColumnOfSettingValidatorUntilRF30(final IFile file, final RobotSettingsSection section,
            final ValidationReportingStrategy reporter) {
        this.file = file;
        this.section = section;
        this.reporter = reporter;
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        return Range.closedOpen(new RobotVersion(2, 9), new RobotVersion(3, 0));
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        section.getLinkedElement().getMetadatas().stream().filter(this::hasOldSyntax).forEach(this::reportOldSyntax);
    }

    private boolean hasOldSyntax(final Metadata metadata) {
        return "meta:".equalsIgnoreCase(metadata.getDeclaration().getText().trim());
    }

    private void reportOldSyntax(final Metadata metadata) {
        reporter.handleProblem(
                RobotProblem.causedBy(GeneralSettingsProblem.METADATA_SETTING_JOINED_WITH_KEY_IN_COLUMN_29), file,
                metadata.getDeclaration());
    }
}
