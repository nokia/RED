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
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;

public abstract class ADuplicatedValidator<T extends AModelElement<?>> extends VersionDependentModelUnitValidator {

    protected final IFile file;

    protected final RobotSettingsSection section;

    protected final ProblemsReportingStrategy reporter;

    public ADuplicatedValidator(final IFile file, final RobotSettingsSection section) {
        this.file = file;
        this.section = section;
        this.reporter = new ProblemsReportingStrategy();
    }

    @Override
    public void validate(IProgressMonitor monitor) throws CoreException {
        final List<T> elems = getElements();

        if (elems.size() > 1) {
            for (int i = 1; i < elems.size(); i++) {
                final RobotToken token = elems.get(i).getDeclaration();
                reporter.handleProblem(RobotProblem.causedBy(GeneralSettingsProblem.DUPLICATED_SETTING)
                        .formatMessageWith(token.getText()), file, token);
            }
        }
    }

    protected abstract List<T> getElements();
}
