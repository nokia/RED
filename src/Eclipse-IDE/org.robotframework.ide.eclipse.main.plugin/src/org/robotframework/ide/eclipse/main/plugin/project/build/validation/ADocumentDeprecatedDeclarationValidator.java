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
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;

public abstract class ADocumentDeprecatedDeclarationValidator implements ModelUnitValidator {

    private final IFile file;

    private final ValidationReportingStrategy reporter;

    public ADocumentDeprecatedDeclarationValidator(final IFile file, final ValidationReportingStrategy reporter) {
        this.file = file;
        this.reporter = reporter;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        for (final RobotToken docToken : getDocumentationDeclaration()) {
            final String raw = docToken.getRaw();
            final String rawWithoutWhiteSpaces = raw.replaceAll("\\s", "");
            if (!rawWithoutWhiteSpaces.toLowerCase().contains("documentation")) {
                reporter.handleProblem(RobotProblem.causedBy(getSettingProblemId()).formatMessageWith(raw), file,
                        docToken);
            }
        }
    }

    public abstract IProblemCause getSettingProblemId();

    public abstract List<RobotToken> getDocumentationDeclaration();
}
