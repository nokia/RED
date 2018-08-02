/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;

import com.google.common.collect.Range;

class LocalSettingsDuplicationInOldRfValidator extends VersionDependentModelUnitValidator {

    private final IFile file;

    private final List<RobotLine> fileContent;

    private final FilePosition startPosition;

    private final FilePosition endPosition;

    private final RobotTokenType duplicatedTokenType;

    private final ValidationReportingStrategy reporter;

    LocalSettingsDuplicationInOldRfValidator(final IFile file, final List<RobotLine> fileContent,
            final FilePosition startPosition, final FilePosition endPosition, final RobotTokenType duplicatedTokenType,
            final ValidationReportingStrategy reporter) {
        this.file = file;
        this.fileContent = fileContent;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.duplicatedTokenType = duplicatedTokenType;
        this.reporter = reporter;
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        return Range.lessThan(new RobotVersion(3, 0));
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        for (int i = startPosition.getLine(); i <= endPosition.getLine(); i++) {
            final RobotLine line = fileContent.get(i - 1);
            for (final RobotToken token : line.getLineTokens()) {
                if (token.getTypes().contains(duplicatedTokenType)) {
                    reporter.handleProblem(RobotProblem.causedBy(GeneralSettingsProblem.DUPLICATED_SETTING_OLD)
                            .formatMessageWith(token.getText()), file, token);
                }
            }
        }
    }
}
