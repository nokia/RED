/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;

public abstract class ADeprecatedSettingElement implements ModelUnitValidator {

    private final IFile file;

    private final ValidationReportingStrategy reporter;

    private final String representation;

    public ADeprecatedSettingElement(final IFile file, final ValidationReportingStrategy reporter,
            final String representation) {
        this.file = file;
        this.reporter = reporter;
        this.representation = representation.toLowerCase().replaceAll("\\s", "");
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        for (final RobotToken docToken : getDeclaration()) {
            final String text = docToken.getText();
            final String textWithoutWhiteSpaces = text.replaceAll("\\s", "");
            if (textWithoutWhiteSpaces.toLowerCase().contains(representation)) {
                reporter.handleProblem(RobotProblem.causedBy(getProblemId()).formatMessageWith(text), file, docToken);
            }
        }
    }

    public abstract IProblemCause getProblemId();

    public abstract List<RobotToken> getDeclaration();
}
