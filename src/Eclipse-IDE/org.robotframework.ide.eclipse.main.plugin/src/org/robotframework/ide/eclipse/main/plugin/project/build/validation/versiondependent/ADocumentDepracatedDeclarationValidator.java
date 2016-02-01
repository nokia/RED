/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;

import com.google.common.collect.Range;

public abstract class ADocumentDepracatedDeclarationValidator extends VersionDependentModelUnitValidator {

    private final IFile file;

    private final ProblemsReportingStrategy reporter;

    public ADocumentDepracatedDeclarationValidator(final IFile file, ProblemsReportingStrategy reporter) {
        this.file = file;
        this.reporter = reporter;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        for (final RobotToken docToken : getDocumentationDeclaration()) {
            String raw = docToken.getRaw();
            final String rawWihtoutWithspaces = raw.replaceAll("\\s", "");
            if (!rawWihtoutWithspaces.toLowerCase().contains("documentation")) {
                reporter.handleProblem(RobotProblem.causedBy(getSettingProblemId()).formatMessageWith(raw), file,
                        docToken);
            }
        }
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        return Range.all();
    }

    public abstract IProblemCause getSettingProblemId();

    public abstract List<RobotToken> getDocumentationDeclaration();
}
