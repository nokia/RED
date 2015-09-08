/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.ValidationContext.RobotVersion;

import com.google.common.collect.Range;

abstract class VersionDependentModelUnitValidator implements ModelUnitValidator {

    boolean isApplicableFor(final RobotVersion version) {
        return getApplicableVersionRange().contains(version);
    }

    protected abstract Range<RobotVersion> getApplicableVersionRange();

}
