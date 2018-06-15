/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.base.Predicates.not;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;
import org.rf.ide.core.testdata.model.table.keywords.names.GherkinStyleSupport;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext.ValidationKeywordEntity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Range;

class KeywordCallValidator implements ModelUnitValidator {

    protected final FileValidationContext validationContext;
    protected final ValidationReportingStrategy reporter;

    private final RobotToken keywordNameToken;

    private final List<RobotToken> arguments;

    private ValidationKeywordEntity foundKeyword;

    KeywordCallValidator(final FileValidationContext validationContext, final RobotToken keywordNameToken,
            final List<RobotToken> arguments, final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.reporter = reporter;
        this.keywordNameToken = keywordNameToken;
        this.arguments = arguments;
    }

    @Override
    public void validate(final IProgressMonitor monitor) {
        foundKeyword = null;
        validateKeywordCall();
    }

    void validateKeywordCall() {
        final String kwName = keywordNameToken.getText();
        final ListMultimap<String, KeywordEntity> keywordProposal = validationContext.findPossibleKeywords(kwName);

        final Optional<String> nameToUse = GherkinStyleSupport.firstNameTransformationResult(kwName,
                gherkinNameVariant -> validationContext.isKeywordAccessible(keywordProposal, gherkinNameVariant)
                        ? Optional.of(gherkinNameVariant)
                        : Optional.empty());
        final String name = nameToUse.filter(not(String::isEmpty)).orElse(kwName);
        final int offset = keywordNameToken.getStartOffset() + (kwName.length() - name.length());
        final ProblemPosition position = new ProblemPosition(keywordNameToken.getLineNumber(),
                Range.closed(offset, offset + name.length()));

        if (!nameToUse.isPresent()) {
            reporter.handleProblem(RobotProblem.causedBy(KeywordsProblem.UNKNOWN_KEYWORD).formatMessageWith(name),
                    validationContext.getFile(), position, ImmutableMap.of(AdditionalMarkerAttributes.NAME, name,
                            AdditionalMarkerAttributes.ORIGINAL_NAME, keywordNameToken.getText()));
        } else {
            final ListMultimap<KeywordScope, KeywordEntity> keywords = validationContext
                    .getPossibleKeywords(keywordProposal, name);

            for (final KeywordScope scope : KeywordScope.defaultOrder()) {
                final List<KeywordEntity> keywordEntities = keywords.get(scope);
                if (keywordEntities.size() == 0) {
                    continue;

                } else if (keywordEntities.size() == 1) {
                    this.foundKeyword = (ValidationKeywordEntity) keywordEntities.get(0);
                    validateFoundKeyword(name, position);

                } else {
                    final List<String> sources = keywordEntities.stream()
                            .map(KeywordEntity::getSourceNameInUse)
                            .collect(toList());
                    reporter.handleProblem(
                            RobotProblem.causedBy(KeywordsProblem.AMBIGUOUS_KEYWORD)
                                    .formatMessageWith(name, "[" + String.join(", ", sources) + "]"),
                            validationContext.getFile(), position,
                            ImmutableMap.of(AdditionalMarkerAttributes.NAME, name,
                                    AdditionalMarkerAttributes.ORIGINAL_NAME, kwName,
                                    AdditionalMarkerAttributes.SOURCES, String.join(";", sources)));
                }
            }
        }
    }

    private void validateFoundKeyword(final String actualName, final ProblemPosition position) {

        final IFile file = validationContext.getFile();
        if (foundKeyword.isDeprecated()) {
            final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.DEPRECATED_KEYWORD)
                    .formatMessageWith(actualName);
            reporter.handleProblem(problem, file, position);
        }
        if (foundKeyword.isFromNestedLibrary(file)) {
            final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.KEYWORD_FROM_NESTED_LIBRARY)
                    .formatMessageWith(actualName);
            reporter.handleProblem(problem, file, position);
        }
        if (foundKeyword.hasInconsistentName(actualName)) {
            final RobotProblem problem = RobotProblem
                    .causedBy(KeywordsProblem.KEYWORD_OCCURRENCE_NOT_CONSISTENT_WITH_DEFINITION)
                    .formatMessageWith(actualName, foundKeyword.getNameFromDefinition());
            reporter.handleProblem(problem, file, position,
                    ImmutableMap.of(AdditionalMarkerAttributes.NAME, actualName,
                            AdditionalMarkerAttributes.ORIGINAL_NAME, foundKeyword.getNameFromDefinition(),
                            AdditionalMarkerAttributes.SOURCES, foundKeyword.getSourceNameInUse()));
        }
        validateArguments();
    }

    protected void validateArguments() {
        final QualifiedKeywordName qualifiedKeywordName = getFoundKeywordName().get();
        final IFile file = validationContext.getFile();

        // validate the arguments
        final KeywordCallArgumentsValidator argsValidator;
        if (SpecialKeywords.isRunKeywordVariant(qualifiedKeywordName)) {
            argsValidator = new KeywordCallArgumentsOfRunKwVariantValidator(file, keywordNameToken, reporter,
                    foundKeyword.getArgumentsDescriptor(), arguments);
        } else {
            argsValidator = new KeywordCallArgumentsValidator(file, keywordNameToken, reporter,
                    foundKeyword.getArgumentsDescriptor(), arguments);
        }
        argsValidator.validate(null);

        // validate arguments variable syntax for some special keywords
        final List<RobotToken> varsForSyntaxCheck = SpecialKeywords
                .getArgumentsToValidateForVariablesSyntax(qualifiedKeywordName, arguments);
        for (final RobotToken varToken : varsForSyntaxCheck) {
            if (!hasValidVarSyntax(varToken)) {
                final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.INVALID_VARIABLE_SYNTAX)
                        .formatMessageWith(varToken.getText());
                reporter.handleProblem(problem, file, varToken);
            }
        }
    }

    private static boolean hasValidVarSyntax(final RobotToken varToken) {
        final List<IRobotTokenType> types = varToken.getTypes();
        final String text = varToken.getText();
        return (types.contains(RobotTokenType.VARIABLES_SCALAR_DECLARATION)
                || types.contains(RobotTokenType.VARIABLES_LIST_DECLARATION)
                || types.contains(RobotTokenType.VARIABLES_DICTIONARY_DECLARATION))
                && (text.startsWith("${") || text.startsWith("@{") || text.startsWith("&{")) && text.endsWith("}");
    }

    Optional<QualifiedKeywordName> getFoundKeywordName() {
        return Optional.ofNullable(foundKeyword)
                .map(kw -> QualifiedKeywordName.create(kw.getKeywordName(), kw.getSourceName()));
    }
}
