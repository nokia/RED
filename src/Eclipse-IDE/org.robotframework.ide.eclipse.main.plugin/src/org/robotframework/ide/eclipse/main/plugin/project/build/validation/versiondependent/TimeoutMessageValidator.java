/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;

import com.google.common.collect.Range;


class TimeoutMessageValidator<T extends AModelElement<?>> extends VersionDependentModelUnitValidator {

    private final IFile file;

    private final Supplier<List<T>> elementsSupplier;

    private final Function<T, List<RobotToken>> elementTokenProvider;

    private final ValidationReportingStrategy reporter;

    TimeoutMessageValidator(final IFile file, final Supplier<List<T>> elementsSupplier,
            final Function<T, List<RobotToken>> elementTokenProvider,
            final ValidationReportingStrategy reporter) {
        this.file = file;
        this.elementsSupplier = elementsSupplier;
        this.elementTokenProvider = elementTokenProvider;
        this.reporter = reporter;
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        return Range.atLeast(new RobotVersion(3, 0, 1));
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        for (final T timeout : elementsSupplier.get()) {
            final List<RobotToken> msgTokens = elementTokenProvider.apply(timeout);
            if (!msgTokens.isEmpty()) {
                final RobotProblem problem = RobotProblem.causedBy(GeneralSettingsProblem.TIMEOUT_MESSAGE_DEPRECATED);
                reporter.handleProblem(problem, file, msgTokens.get(0));
            }
        }
    }
}
