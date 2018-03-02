/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.setting;

import static com.google.common.collect.Lists.transform;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.VersionDependentModelUnitValidator;

import com.google.common.base.Function;
import com.google.common.base.Joiner;

abstract class ADuplicatedInOldValidator<T extends AModelElement<?>> extends VersionDependentModelUnitValidator {

    private final IFile file;

    protected final RobotSettingsSection section;

    private final ValidationReportingStrategy reporter;

    ADuplicatedInOldValidator(final IFile file, final RobotSettingsSection section,
            final ValidationReportingStrategy reporter) {
        this.file = file;
        this.section = section;
        this.reporter = reporter;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        final List<T> elems = getElements();

        final String inUse = Joiner.on(' ').skipNulls().join(transform(elems, getImportantElement()));

        if (elems.size() > 1) {
            for (int i = 1; i < elems.size(); i++) {
                final RobotToken token = elems.get(i).getDeclaration();
                reporter.handleProblem(
                        RobotProblem.causedBy(getSettingProblemId()).formatMessageWith(token.getText(), inUse), file,
                        token);
            }
        }
    }

    protected abstract List<T> getElements();

    protected abstract Function<T, String> getImportantElement();

    protected abstract GeneralSettingsProblem getSettingProblemId();
}
