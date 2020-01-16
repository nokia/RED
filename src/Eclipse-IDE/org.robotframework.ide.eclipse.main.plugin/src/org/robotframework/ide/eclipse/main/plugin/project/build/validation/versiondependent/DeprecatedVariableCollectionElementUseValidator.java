/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.IElementDeclaration;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.IndexDeclaration;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.VariablesProblem;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;


/**
 * @author lwlodarc
 *
 */
class DeprecatedVariableCollectionElementUseValidator extends VersionDependentModelUnitValidator {

    private final IFile file;

    private final RobotSuiteFile robotSuiteFile;

    private final ValidationReportingStrategy reporter;

    DeprecatedVariableCollectionElementUseValidator(final IFile file, final RobotSuiteFile robotSuiteFile,
            final ValidationReportingStrategy reporter) {
        this.file = file;
        this.robotSuiteFile = robotSuiteFile;
        this.reporter = reporter;
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        return Range.atLeast(new RobotVersion(3, 2));
    }

    @Override
    public void validate(IProgressMonitor monitor) throws CoreException {
        if (robotSuiteFile.getLinkedElement() != null) {
            final List<RobotToken> variableTokens = robotSuiteFile.getLinkedElement()
                    .getFileContent()
                    .stream()
                    .flatMap(line -> line.getLineTokens()
                            .stream()
                            .filter(t -> t.getTypes().contains(RobotTokenType.VARIABLE_USAGE)))
                    .collect(Collectors.toList());

            for (final RobotToken token : variableTokens) {
                reportOldVariableElementUsePosition(token);
            }
        }
    }

    private void reportOldVariableElementUsePosition(final RobotToken token) {
        final String text = token.getText();
        final List<IElementDeclaration> elements = new VariableExtractor().extract(text).getMappedElements();
        for (int i = 0; i < elements.size() - 1; i++) {
            if (elements.get(i) instanceof VariableDeclaration && elements.get(i + 1) instanceof IndexDeclaration) {
                final VariableDeclaration varDec = (VariableDeclaration) elements.get(i);
                if (varDec.getRobotType() != VariableType.ENVIRONMENT && varDec.getRobotType() != VariableType.SCALAR) {
                    if (elements.get(i)
                            .getElementsDeclarationInside()
                            .stream()
                            .noneMatch(declaration -> declaration instanceof IndexDeclaration)) {
                        final int start = elements.get(i).getStart().getStart() - 1;
                        final int end = elements.get(i + 1).getEnd().getEnd() + 1;
                        reporter.handleProblem(
                                RobotProblem.causedBy(VariablesProblem.VARIABLE_ELEMENT_OLD_USE)
                                        .formatMessageWith(text.substring(start, end)),
                                file,
                                new ProblemPosition(token.getLineNumber(),
                                        Range.closed(token.getStartOffset() + start, token.getStartOffset() + end)),
                                ImmutableMap.of(AdditionalMarkerAttributes.VALUE,
                                        token.getText().substring(start, end)));
                    }
                }
            }
        }
    }
}
