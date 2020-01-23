/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.ForLoopDeclarationRowDescriptor;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;


class ForLoopInExpressionsValidator extends VersionDependentModelUnitValidator {

    private final IFile file;

    private final ForLoopDeclarationRowDescriptor<?> descriptor;

    private final ValidationReportingStrategy reporter;

    ForLoopInExpressionsValidator(final IFile file, final ForLoopDeclarationRowDescriptor<?> descriptor,
            final ValidationReportingStrategy reporter) {
        this.file = file;
        this.descriptor = descriptor;
        this.reporter = reporter;
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        return Range.atLeast(new RobotVersion(3, 1));
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        final RobotToken inToken = descriptor.getInAction();
        RobotTokenType.IN_TOKEN.getRepresentation().stream()
                .filter(repr -> !repr.equals(inToken.getText())
                        && repr.replaceAll(" ", "").equalsIgnoreCase(inToken.getText().replaceAll(" +", "")))
                .findFirst()
                .ifPresent(canonical -> {
                    final Map<String, Object> additionals = ImmutableMap.of(AdditionalMarkerAttributes.NAME, canonical);
                    final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.FOR_IN_EXPR_WRONGLY_TYPED)
                            .formatMessageWith(inToken.getText(), canonical);
                    reporter.handleProblem(problem, file, inToken, additionals);
                });
    }
}
