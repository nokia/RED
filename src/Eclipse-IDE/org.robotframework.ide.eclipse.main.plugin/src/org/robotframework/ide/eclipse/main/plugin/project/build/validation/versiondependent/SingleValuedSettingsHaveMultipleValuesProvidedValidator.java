/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.function.Supplier;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;

import com.google.common.collect.Range;

class SingleValuedSettingsHaveMultipleValuesProvidedValidator<T extends AModelElement<?>>
        extends VersionDependentModelUnitValidator {

    private final IFile file;

    private final Supplier<List<T>> settingsSupplier;

    private final ValidationReportingStrategy reporter;

    private final String additionalMessage;

    SingleValuedSettingsHaveMultipleValuesProvidedValidator(final IFile file, final Supplier<List<T>> elementsSupplier,
            final ValidationReportingStrategy reporter, final String additionalMessage) {
        this.file = file;
        this.settingsSupplier = elementsSupplier;
        this.reporter = reporter;
        this.additionalMessage = additionalMessage;
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        return Range.atLeast(new RobotVersion(3, 2));
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        for (final T setting : settingsSupplier.get()) {
            // tokens without setting name and comment tokens
            final List<RobotToken> values = setting.getElementTokens()
                    .stream()
                    .skip(1)
                    .filter(t -> !t.getTypes().contains(RobotTokenType.START_HASH_COMMENT)
                            && !t.getTypes().contains(RobotTokenType.COMMENT_CONTINUE))
                    .collect(toList());

            if (values.size() > 1) {
                final RobotProblem problem = RobotProblem
                        .causedBy(GeneralSettingsProblem.INVALID_NUMBER_OF_SETTING_VALUES)
                        .formatMessageWith(setting.getDeclaration().getText(), values.size(), additionalMessage);
                reporter.handleProblem(problem, file, setting.getDeclaration());
            }
        }
    }
}
