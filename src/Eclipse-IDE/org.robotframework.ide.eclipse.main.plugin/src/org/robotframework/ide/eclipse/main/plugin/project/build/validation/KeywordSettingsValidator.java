/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.model.table.keywords.KeywordArguments;
import org.rf.ide.core.testdata.model.table.keywords.KeywordDocumentation;
import org.rf.ide.core.testdata.model.table.keywords.KeywordReturn;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTags;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTeardown;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTimeout;
import org.rf.ide.core.testdata.model.table.keywords.KeywordUnknownSettings;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.keywords.names.EmbeddedKeywordNamesSupport;
import org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.validation.RobotTimeFormat;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

/**
 * @author Michal Anglart
 *
 */
class KeywordSettingsValidator implements ModelUnitValidator {

    private static final Pattern VAR_ARG_PATTERN = Pattern.compile("^[$@&]\\{[^\\}]+\\}");

    private final FileValidationContext validationContext;

    private final UserKeyword keyword;

    private final ValidationReportingStrategy reporter;

    KeywordSettingsValidator(final FileValidationContext validationContext, final UserKeyword keyword,
            final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.keyword = keyword;
        this.reporter = reporter;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        reportUnknownSettings();

        reportReturnProblems();
        reportTagsProblems();
        reportTimeoutsProblems();
        reportDocumentationsProblems();
        reportTeardownProblems();
        reportArgumentsProblems();

        reportOutdatedSettingsSynonyms();
        reportUnknownVariablesInNonExecutables();
    }

    private void reportUnknownSettings() {
        final List<KeywordUnknownSettings> unknownSettings = keyword.getUnknownSettings();
        for (final KeywordUnknownSettings unknownSetting : unknownSettings) {
            final RobotToken token = unknownSetting.getDeclaration();
            final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.UNKNOWN_KEYWORD_SETTING)
                    .formatMessageWith(token.getText());
            reporter.handleProblem(problem, validationContext.getFile(), token);
        }
    }

    private void reportReturnProblems() {
        final Map<RobotToken, Boolean> declarationIsEmpty = keyword.getReturns().stream().collect(
                toMap(KeywordReturn::getDeclaration, ret -> ret.getReturnValues().isEmpty()));

        reportCommonProblems(declarationIsEmpty);
    }

    private void reportTagsProblems() {
        final Map<RobotToken, Boolean> declarationIsEmpty = keyword.getTags().stream().collect(
                toMap(KeywordTags::getDeclaration, tag -> tag.getTags().isEmpty()));

        reportCommonProblems(declarationIsEmpty);
    }

    private void reportTimeoutsProblems() {
        final Map<RobotToken, Boolean> declarationIsEmpty = keyword.getTimeouts().stream().collect(
                toMap(KeywordTimeout::getDeclaration, timeout -> timeout.getTimeout() == null));

        reportCommonProblems(declarationIsEmpty);
        reportInvalidTimeoutSyntax(keyword.getTimeouts());
    }

    private void reportInvalidTimeoutSyntax(final List<KeywordTimeout> timeouts) {
        for (final KeywordTimeout kwTimeout : timeouts) {
            final RobotToken timeoutToken = kwTimeout.getTimeout();
            if (timeoutToken != null) {
                final String timeout = timeoutToken.getText();
                if (!timeoutToken.getTypes().contains(RobotTokenType.VARIABLE_USAGE)
                        && !RobotTimeFormat.isValidRobotTimeArgument(timeout.trim())) {
                    final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.INVALID_TIME_FORMAT)
                            .formatMessageWith(timeout);
                    reporter.handleProblem(problem, validationContext.getFile(), timeoutToken);
                }
            }
        }
    }

    private void reportDocumentationsProblems() {
        final Map<RobotToken, Boolean> declarationIsEmpty = keyword.getDocumentation().stream().collect(
                toMap(KeywordDocumentation::getDeclaration, tag -> tag.getDocumentationText().isEmpty()));

        reportCommonProblems(declarationIsEmpty);
    }

    private void reportTeardownProblems() {
        final Map<RobotToken, Boolean> declarationIsEmpty = keyword.getTeardowns().stream().collect(
                toMap(KeywordTeardown::getDeclaration, teardown -> teardown.getKeywordName() == null));

        reportCommonProblems(declarationIsEmpty);
    }

    private void reportCommonProblems(final Map<RobotToken, Boolean> declarationTokens) {
        final String kwName = keyword.getKeywordName().getText();
        final IFile file = validationContext.getFile();
        final boolean tooManySettings = declarationTokens.size() > 1;

        declarationTokens.forEach((defToken, isEmpty) -> {
            if (tooManySettings) {
                final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.DUPLICATED_KEYWORD_SETTING)
                        .formatMessageWith(kwName, defToken.getText());
                reporter.handleProblem(problem, file, defToken);
            }

            if (isEmpty) {
                final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.EMPTY_KEYWORD_SETTING)
                        .formatMessageWith(defToken.getText());
                reporter.handleProblem(problem, file, defToken);
            }
        });
    }

    private void reportOutdatedSettingsSynonyms() {
        reportOutdatedSettings(keyword.getDocumentation(), KeywordsProblem.DOCUMENT_SYNONYM, "documentation");
        reportOutdatedSettings(keyword.getTeardowns(), KeywordsProblem.POSTCONDITION_SYNONYM, "teardown");
    }

    private void reportOutdatedSettings(final List<? extends AModelElement<?>> settings, final IProblemCause cause,
            final String correctRepresentation) {
        for (final AModelElement<?> setting : settings) {
            final RobotToken declarationToken = setting.getDeclaration();
            final String text = declarationToken.getText();
            final String canonicalText = text.replaceAll("\\s", "").toLowerCase();
            final String canonicalCorrectRepresentation = correctRepresentation.replaceAll("\\s", "").toLowerCase();
            if (!canonicalText.contains(canonicalCorrectRepresentation)) {
                reporter.handleProblem(RobotProblem.causedBy(cause).formatMessageWith(text),
                        validationContext.getFile(), declarationToken);
            }
        }
    }

    private void reportUnknownVariablesInNonExecutables() {
        final UnknownVariables unknownVarsValidator = new UnknownVariables(validationContext, reporter);

        for (final KeywordTimeout kwTimeout : keyword.getTimeouts()) {
            unknownVarsValidator.reportUnknownVars(kwTimeout.getTimeout());
        }
        for (final KeywordTags tag : keyword.getTags()) {
            unknownVarsValidator.reportUnknownVars(tag.getTags());
        }
        // KeywordReturn are validated in KeywordValidator, since it requires variables created by executables
    }

    private void reportArgumentsProblems() {
        final IFile file = validationContext.getFile();
        final String fileName = file.getName();

        final Map<RobotToken, Boolean> declarationTokens = newHashMap();
        for (final KeywordArguments arg : keyword.getArguments()) {
            declarationTokens.put(arg.getDeclaration(), arg.getArguments().isEmpty());
        }
        if (hasEmbeddedArguments(fileName, keyword)) {
            declarationTokens.put(keyword.getKeywordName(), false);
        }
        reportCommonProblems(declarationTokens);

        final boolean shouldContinue = reportArgumentsSyntaxProblems();
        if (shouldContinue) {
            reportDuplicatedArguments();
            reportArgumentsOrderProblems();
            reportArgumentsDefaultValuesUnknownVariables();
        }
    }

    private boolean reportArgumentsSyntaxProblems() {
        boolean shouldContinue = true;
        for (final KeywordArguments argSetting : keyword.getArguments()) {
            for (final RobotToken argToken : argSetting.getArguments()) {
                final boolean isCorrect = hasValidArgumentSyntax(argToken);
                if (!isCorrect) {
                    final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.INVALID_KEYWORD_ARG_SYNTAX)
                            .formatMessageWith(argToken.getText());
                    reporter.handleProblem(problem, validationContext.getFile(), argToken);
                }
                shouldContinue &= isCorrect;
            }
        }
        return shouldContinue;
    }

    private boolean hasValidArgumentSyntax(final RobotToken argToken) {
        final Matcher matcher = VAR_ARG_PATTERN.matcher(argToken.getText());
        if (matcher.find() && matcher.start() == 0) {
            final String rest = argToken.getText().substring(matcher.end());
            return rest.isEmpty() || rest.startsWith("=") && argToken.getText().startsWith("$");
        }
        return false;
    }

    private void reportDuplicatedArguments() {
        final String fileName = validationContext.getFile().getName();

        final Multimap<String, RobotToken> arguments = extractArgumentVariables(keyword, new VariableExtractor(),
                fileName);

        for (final String arg : arguments.keySet()) {
            final Collection<RobotToken> tokens = arguments.get(arg);
            if (tokens.size() > 1) {
                for (final RobotToken token : tokens) {
                    final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.ARGUMENT_DEFINED_TWICE)
                            .formatMessageWith(token.getText());
                    reporter.handleProblem(problem, validationContext.getFile(), token);
                }
            }
        }
    }

    private Multimap<String, RobotToken> extractArgumentVariables(final UserKeyword keyword,
            final VariableExtractor extractor, final String fileName) {
        final Multimap<String, RobotToken> arguments = ArrayListMultimap.create();

        // first add arguments embedded in name, then from [Arguments] setting
        final Multimap<String, RobotToken> embeddedArguments = VariableNamesSupport
                .extractUnifiedVariables(keyword.getKeywordName(), extractor, fileName);
        for (final String argName : embeddedArguments.keySet()) {
            arguments.putAll(EmbeddedKeywordNamesSupport.removeRegex(argName), embeddedArguments.get(argName));
        }
        for (final KeywordArguments argument : keyword.getArguments()) {
            for (final RobotToken token : argument.getArguments()) {

                final boolean hasDefault = token.getText().contains("=");
                if (hasDefault) {
                    final List<String> splitted = Splitter.on('=').limit(2).splitToList(token.getText());
                    final String def = splitted.get(0);
                    final String unifiedDefinitionName = VariableNamesSupport.extractUnifiedVariableName(def);
                    final Multimap<String, RobotToken> usedVariables = VariableNamesSupport
                            .extractUnifiedVariables(token, new VariableExtractor(), null);
                    arguments.put(unifiedDefinitionName,
                            Iterables.getFirst(usedVariables.get(unifiedDefinitionName), null));
                } else {
                    arguments.putAll(
                            VariableNamesSupport.extractUnifiedVariables(token, extractor, fileName));
                }
            }
        }
        return arguments;
    }

    private boolean hasEmbeddedArguments(final String fileName, final UserKeyword keyword) {
        final VariableExtractor variableExtractor = new VariableExtractor();
        final List<VariableDeclaration> extractedVariables = variableExtractor
                .extract(keyword.getKeywordName(), fileName).getCorrectVariables();
        return !extractedVariables.isEmpty();
    }

    private void reportArgumentsOrderProblems() {
        final List<KeywordArguments> arguments = keyword.getArguments();
        if (arguments == null || arguments.isEmpty()) {
            return;
        }
        boolean wasVararg = false;
        boolean wasKwarg = false;
        boolean wasDefault = false;
        for (final RobotToken argumentToken : arguments.get(arguments.size() - 1).getArguments()) {
            final boolean isDefault = isDefaultArgument(argumentToken);
            final boolean isVararg = argumentToken.getTypes().contains(RobotTokenType.VARIABLES_LIST_DECLARATION);
            final boolean isKwarg = argumentToken.getTypes().contains(RobotTokenType.VARIABLES_DICTIONARY_DECLARATION);

            if (wasDefault && !isDefault && !isVararg && !isKwarg) {
                final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.NON_DEFAULT_ARGUMENT_AFTER_DEFAULT)
                        .formatMessageWith(argumentToken.getText());
                reporter.handleProblem(problem, validationContext.getFile(), argumentToken);
            }
            if (wasVararg && !isKwarg) {
                final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.ARGUMENT_AFTER_VARARG)
                        .formatMessageWith(argumentToken.getText());
                reporter.handleProblem(problem, validationContext.getFile(), argumentToken);
            }
            if (wasKwarg) {
                final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.ARGUMENT_AFTER_KWARG)
                        .formatMessageWith(argumentToken.getText());
                reporter.handleProblem(problem, validationContext.getFile(), argumentToken);
            }
            wasVararg |= isVararg;
            wasDefault |= isDefault;
            wasKwarg |= isKwarg;
        }
    }

    private void reportArgumentsDefaultValuesUnknownVariables() {
        for (final KeywordArguments argSetting : keyword.getArguments()) {
            final Set<String> additionalKnownVariables = new HashSet<>();

            for (final RobotToken argToken : argSetting.getArguments()) {
                final boolean hasDefault = argToken.getText().contains("=");
                if (hasDefault) {
                    final List<String> splitted = Splitter.on('=').limit(2).splitToList(argToken.getText());
                    final String def = splitted.get(0);

                    final String unifiedDefinitionName = VariableNamesSupport.extractUnifiedVariableName(def);
                    final Multimap<String, RobotToken> usedVariables = VariableNamesSupport.extractUnifiedVariables(
                            argToken, new VariableExtractor(), validationContext.getFile().getName());

                    final List<RobotToken> varTokens = usedVariables.values()
                            .stream()
                            .filter(varToken -> varToken.getStartOffset() != argToken.getStartOffset()
                                    || varToken.getEndOffset() != argToken.getStartOffset() + def.length())
                            .collect(toList());
                    new UnknownVariables(validationContext, reporter).reportUnknownVars(additionalKnownVariables,
                            varTokens);
                    additionalKnownVariables.add(unifiedDefinitionName);
                } else {
                    additionalKnownVariables.add(VariableNamesSupport.extractUnifiedVariableName(argToken.getText()));
                }
            }
        }
    }

    private boolean isDefaultArgument(final RobotToken argumentToken) {
        return argumentToken.getText().contains("}=");
    }
}
