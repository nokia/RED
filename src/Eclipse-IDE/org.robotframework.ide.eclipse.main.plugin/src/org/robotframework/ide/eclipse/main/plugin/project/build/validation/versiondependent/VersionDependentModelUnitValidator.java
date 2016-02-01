/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import org.rf.ide.core.testdata.model.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;

import com.google.common.collect.Range;

public abstract class VersionDependentModelUnitValidator implements ModelUnitValidator {

    public boolean isApplicableFor(final RobotVersion version) {
        return getApplicableVersionRange().contains(version);
    }

    protected abstract Range<RobotVersion> getApplicableVersionRange();

}
