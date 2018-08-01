/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import java.util.List;
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

class SettingsDuplicationValidator<T extends AModelElement<?>> extends VersionDependentModelUnitValidator {

    private final IFile file;

    private Supplier<List<T>> elementsSupplier;

    private final ValidationReportingStrategy reporter;

    private String problemDescriptionDetail;


    SettingsDuplicationValidator(final IFile file, final Supplier<List<T>> elementsSupplier,
            final ValidationReportingStrategy reporter) {
        this(file, elementsSupplier, reporter, "");
    }

    public SettingsDuplicationValidator(final IFile file, final Supplier<List<T>> elementsSupplier,
            final ValidationReportingStrategy reporter, final String problemDescriptionDetail) {
        this.file = file;
        this.elementsSupplier = elementsSupplier;
        this.reporter = reporter;
        this.problemDescriptionDetail = problemDescriptionDetail;
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        return Range.atLeast(new RobotVersion(3, 0));
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        final List<T> elems = elementsSupplier.get();

        if (elems.size() > 1) {
            for (final T elem : elems) {
                final RobotToken token = elem.getDeclaration();
                reporter.handleProblem(RobotProblem.causedBy(GeneralSettingsProblem.DUPLICATED_SETTING)
                        .formatMessageWith(token.getText(), problemDescriptionDetail), file, token);
            }
        }
    }
}
