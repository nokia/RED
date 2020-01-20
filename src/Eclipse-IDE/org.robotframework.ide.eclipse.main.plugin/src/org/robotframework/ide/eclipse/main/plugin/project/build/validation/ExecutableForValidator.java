/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.RobotEmptyRow;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor.RowType;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.ForLoopDeclarationRowDescriptor;
import org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.AttributesAugmentingReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.VersionDependentValidators;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;


public class ExecutableForValidator implements ExecutableValidator {

    private final FileValidationContext validationContext;
    private final Set<String> additionalVariables;
    private final ValidationReportingStrategy reporter;

    private final IExecutableRowDescriptor<?> descriptor;

    private RobotVersion robotVersion;

    public ExecutableForValidator(final FileValidationContext validationContext, final Set<String> additionalVariables,
            final IExecutableRowDescriptor<?> descriptor, final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.additionalVariables = additionalVariables;
        this.descriptor = descriptor;
        this.reporter = reporter;
        this.robotVersion = validationContext.getVersion();
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        reportVersionDependentProblems();
        reportEmptyLoop();
        reportForLoopBuildMessages();
        reportUnknownVariables();
        if (robotVersion.isOlderThan(new RobotVersion(3, 2))) {
            reportInconsistentName();
        } else {
            reportDeprecatedLoop();
        }

        descriptor.getCreatedVariables()
                .forEach(var -> additionalVariables.add(VariableNamesSupport.extractUnifiedVariableName(var)));
    }

    private void reportVersionDependentProblems() {
        new VersionDependentValidators(validationContext, reporter)
                .getForLoopValidators((ForLoopDeclarationRowDescriptor<?>) descriptor)
                .forEach(ModelUnitValidator::validate);

    }

    private void reportInconsistentName() {
        final RobotToken forToken = descriptor.getAction().getToken();
        final String actualText = forToken.getText();
        if (!actualText.equals(":FOR") && !actualText.equals("FOR")) {
            final RobotProblem problem = RobotProblem
                    .causedBy(KeywordsProblem.FOR_OCCURRENCE_NOT_CONSISTENT_WITH_DEFINITION)
                    .formatMessageWith(actualText);
            reporter.handleProblem(problem, validationContext.getFile(), forToken);
        }
    }
    
    private void reportDeprecatedLoop() {
        final RobotToken forToken = descriptor.getAction().getToken();
        final String actualText = forToken.getText();

        final AModelElement<?> row = descriptor.getRow();
        final IExecutableStepsHolder<?> holder = (IExecutableStepsHolder<?>) row.getParent();
        final List<?> children = holder.getElements();
        final int index = children.indexOf(row);

        if (isDeprecated(actualText, row, children, index)) {
            final int forLenght = countForLoopLenghtWithoutForIdentation(row, children, index, forToken
                    .getStartOffset());
            final Map<String, Object> additionalAttributes = ImmutableMap.of(AdditionalMarkerAttributes.VALUE,
                    forLenght);

            final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.DEPRECATED_FOR);
            reporter.handleProblem(problem, validationContext.getFile(), descriptor.getAction().getToken(),
                    additionalAttributes);
        }
    }
    
    private boolean isDeprecated(final String actualText, final AModelElement<?> row, final List<?> children,
            final int index) {
        return (actualText.toUpperCase().replaceAll("\\s", "").equals(":FOR") || (index >= 0
                && isFollowedByForContinuationRow(children, index, shouldSettingBreakRule(row), true)));
    }

    private int countForLoopLenghtWithoutForIdentation(final AModelElement<?> row, final List<?> children,
            final int index, final int forOffset) {

        RobotExecutableRow<?> lastRow = null;
        for (int i = index + 1; i < children.size(); i++) {
            if (children.get(i) instanceof RobotExecutableRow<?>) {
                final RobotExecutableRow<?> nextRow = (RobotExecutableRow<?>) children.get(i);
                final RowType type = nextRow.buildLineDescription().getRowType();
                if (type == RowType.FOR_CONTINUE) {
                    lastRow = (RobotExecutableRow<?>) children.get(i);
                } else if (type == RowType.COMMENTED_HASH) {
                    continue;
                }
            } else {
                continue;
            }
        }
        if (lastRow == null) {
            return row.getEndPosition().getOffset() - forOffset;
        }
        return lastRow.getEndPosition().getOffset() - forOffset;
    }

    private void reportEmptyLoop() {
        final AModelElement<?> row = descriptor.getRow();
        final IExecutableStepsHolder<?> holder = (IExecutableStepsHolder<?>) row.getParent();
        final List<?> children = holder.getElements();
        final int index = children.indexOf(row);

        if (index >= 0 && !isFollowedByForContinuationRow(children, index, shouldSettingBreakRule(row))) {
            final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.FOR_IS_EMPTY);
            reporter.handleProblem(problem, validationContext.getFile(), descriptor.getAction().getToken());
        }
    }

    private boolean shouldSettingBreakRule(final AModelElement<?> row) {
        return !row.getDeclaration().getTypes().contains(RobotTokenType.FOR_WITH_END);
    }

    private boolean isFollowedByForContinuationRow(final List<?> children, final int index,
            final boolean settingEndsLoop) {
        return isFollowedByForContinuationRow(children, index, settingEndsLoop, false);
    }

    private boolean isFollowedByForContinuationRow(final List<?> children, final int index,
            final boolean settingEndsLoop, final boolean deprecationFlag) {
        for (int i = index + 1; i < children.size(); i++) {
            if (children.get(i) instanceof RobotExecutableRow<?>) {
                final RobotExecutableRow<?> nextRow = (RobotExecutableRow<?>) children.get(i);
                final RowType type = nextRow.buildLineDescription().getRowType();
                if (type == RowType.FOR_CONTINUE) {
                    if (deprecationFlag) {
                        return nextRow.getAction().getText().equals("\\");
                    }
                    return true;
                } else if (type == RowType.COMMENTED_HASH) {
                    continue;
                }
                break;
            } else if (children.get(i) instanceof RobotEmptyRow<?>) {
                continue;
            } else if (children.get(i) instanceof LocalSetting<?>) {
                if (settingEndsLoop) {
                    return false;
                } else {
                    continue;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    private void reportForLoopBuildMessages() {
        for (final BuildMessage buildMessage : descriptor.getMessages()) {
            final ProblemPosition position = new ProblemPosition(buildMessage.getFileRegion().getStart().getLine(),
                    Range.closed(buildMessage.getFileRegion().getStart().getOffset(),
                            buildMessage.getFileRegion().getEnd().getOffset()));
            final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.INVALID_FOR_KEYWORD)
                    .formatMessageWith(buildMessage.getMessage());
            reporter.handleProblem(problem, validationContext.getFile(), position);
        }
    }

    private void reportUnknownVariables() {
        final UnknownVariables unknownVarsValidator = new UnknownVariables(validationContext,
                AttributesAugmentingReportingStrategy.withLocalVarFixer(reporter));
        unknownVarsValidator.reportUnknownVarsDeclarations(additionalVariables, descriptor.getUsedVariables());
    }
}
