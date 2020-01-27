/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.descs.VariablesAnalyzer;
import org.rf.ide.core.testdata.model.table.variables.descs.VariablesVisitor;
import org.rf.ide.core.testdata.text.read.RobotLine;
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
    public void validate(final IProgressMonitor monitor) throws CoreException {
        if (robotSuiteFile.getLinkedElement() == null) {
            return;
        }
        final VariablesAnalyzer analyzer = VariablesAnalyzer.analyzer(robotSuiteFile.getRobotParserComplianceVersion());
        robotSuiteFile.getLinkedElement()
                .getFileContent()
                .stream()
                .flatMap(RobotLine::tokensStream)
                .filter(t -> t.getTypes().contains(RobotTokenType.VARIABLE_USAGE))
                .forEach(token -> {
                    analyzer.visitVariables(token, withIndexedListsOrDictsVisitor());
                });
    }

    private VariablesVisitor withIndexedListsOrDictsVisitor() {
        return VariablesVisitor.variableUsagesVisitor(usage -> {
            if (usage.isIndexed()
                    && (usage.getType() == VariableType.LIST || usage.getType() == VariableType.DICTIONARY)) {
                final String varContent = usage.asToken().getText();
                final RobotProblem problem = RobotProblem.causedBy(VariablesProblem.VARIABLE_ELEMENT_OLD_USE)
                        .formatMessageWith(varContent);
                reporter.handleProblem(problem, file, ProblemPosition.fromRegion(usage.getRegion()),
                        ImmutableMap.of(AdditionalMarkerAttributes.VALUE, varContent));
            }
        });
    }
}
