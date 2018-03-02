/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.variables;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.IVariableHolder;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.VariablesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.VersionDependentModelUnitValidator;

import com.google.common.collect.Range;

public class DictionaryExistenceValidator extends VersionDependentModelUnitValidator {

    private final IFile file;

    private final IVariableHolder variable;

    private final ValidationReportingStrategy reporter;

    public DictionaryExistenceValidator(final IFile file, final IVariableHolder variable,
            final ValidationReportingStrategy reporter) {
        this.file = file;
        this.variable = variable;
        this.reporter = reporter;
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        return Range.lessThan(new RobotVersion(2, 9));
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        if (variable.getType() == VariableType.DICTIONARY) {
            final ProblemPosition position = toPositionOfWholeDefinition(variable);
            reporter.handleProblem(RobotProblem.causedBy(VariablesProblem.DICTIONARY_NOT_AVAILABLE)
                    .formatMessageWith(variable.getName()), file, position);
        }
    }

    static ProblemPosition toPositionOfWholeDefinition(final IVariableHolder variable) {
        final List<RobotToken> tokens = ((AVariable) variable).getElementTokens();
        final RobotToken lastToken = tokens.isEmpty() ? variable.getDeclaration() : tokens.get(tokens.size() - 1);

        return new ProblemPosition(variable.getDeclaration().getLineNumber(), Range.closed(
                variable.getDeclaration().getStartOffset(), lastToken.getStartOffset() + lastToken.getText().length()));
    }
}
