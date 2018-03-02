/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.setting;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;

import com.google.common.collect.Range;

public class MetadataKeyInColumnOfSettingValidatorUntilRF30 extends AMetadataKeyInColumnOfSettingValidator {

    public MetadataKeyInColumnOfSettingValidatorUntilRF30(final IFile file, final RobotSettingsSection section,
            final ValidationReportingStrategy reporter) {
        super(file, section, reporter);
    }

    @Override
    public IProblemCause getSettingProblemId() {
        return GeneralSettingsProblem.METADATA_SETTING_JOINED_WITH_KEY_IN_COLUMN_29;
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        return Range.lessThan(new RobotVersion(3, 0));
    }
}
