/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.environment.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.SuiteFileProblem;

import com.google.common.collect.Range;

class UnsupportedSuiteFileExtensionValidator extends DeprecatedSuiteFileExtensionValidator {

    public UnsupportedSuiteFileExtensionValidator(final IFile file, final RobotSuiteFile fileModel,
            final Class<? extends RobotSuiteFileSection> suiteSectionClass,
            final ValidationReportingStrategy reporter) {
        super(file, fileModel, suiteSectionClass, reporter);
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        return Range.atLeast(new RobotVersion(3, 2));
    }

    @Override
    protected RobotProblem createProblem(final String extension) {
        final String detail = "It is possible to launch this suite with '--extension " + extension
                + "' argument provided.";
        return RobotProblem.causedBy(SuiteFileProblem.REMOVED_SUITE_FILE_EXTENSION)
                .formatMessageWith(extension, detail);
    }
}
