/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.VariablesProblem;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

/**
 * @author Michal Anglart
 *
 */
class KeywordSettingsValidator implements ModelUnitValidator {

    private final FileValidationContext validationContext;

    private final UserKeyword keyword;

    private final ProblemsReportingStrategy reporter;

    KeywordSettingsValidator(final FileValidationContext validationContext, final UserKeyword keyword,
            final ProblemsReportingStrategy reporter) {
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

        reportKeywordUsageProblemsInUserKeywordSettings();
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
        final Map<RobotToken, Boolean> declarationIsEmpty = newHashMap();
        for (final KeywordReturn returns : keyword.getReturns()) {
            declarationIsEmpty.put(returns.getDeclaration(), returns.getReturnValues().isEmpty());
        }
        reportCommonProblems(declarationIsEmpty);
    }

    private void reportTagsProblems() {
        final Map<RobotToken, Boolean> declarationIsEmpty = newHashMap();
        for (final KeywordTags tag : keyword.getTags()) {
            declarationIsEmpty.put(tag.getDeclaration(), tag.getTags().isEmpty());
        }
        reportCommonProblems(declarationIsEmpty);
    }

    private void reportTimeoutsProblems() {
        final Map<RobotToken, Boolean> declarationIsEmpty = newHashMap();
        for (final KeywordTimeout timeout : keyword.getTimeouts()) {
            declarationIsEmpty.put(timeout.getDeclaration(), timeout.getTimeout() == null);
        }
        reportCommonProblems(declarationIsEmpty);
    }

    private void reportDocumentationsProblems() {
        final Map<RobotToken, Boolean> declarationIsEmpty = newHashMap();
        for (final KeywordDocumentation doc : keyword.getDocumentation()) {
            declarationIsEmpty.put(doc.getDeclaration(), doc.getDocumentationText().isEmpty());
        }
        reportCommonProblems(declarationIsEmpty);
    }

    private void reportTeardownProblems() {
        final Map<RobotToken, Boolean> declarationIsEmpty = newHashMap();
        for (final KeywordTeardown teardown : keyword.getTeardowns()) {
            declarationIsEmpty.put(teardown.getDeclaration(), teardown.getKeywordName() == null);
        }
        reportCommonProblems(declarationIsEmpty);
    }

    private void reportCommonProblems(final Map<RobotToken, Boolean> declarationTokens) {
        final String kwName = keyword.getKeywordName().getText();
        final IFile file = validationContext.getFile();
        final boolean tooManySettings = declarationTokens.size() > 1;

        for (final RobotToken defToken : declarationTokens.keySet()) {
            if (tooManySettings) {
                final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.DUPLICATED_KEYWORD_SETTING)
                        .formatMessageWith(kwName, defToken.getText());
                reporter.handleProblem(problem, file, defToken);
            }

            if (declarationTokens.get(defToken).booleanValue()) {
                final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.EMPTY_KEYWORD_SETTING)
                        .formatMessageWith(defToken.getText());
                reporter.handleProblem(problem, file, defToken);
            }
        }
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

        reportDuplicatedArguments();
        reportArgumentsOrderProblems();
        reportArgumentsDefaultValuesUnknownVariables();
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
                .extractUnifiedVariables(newArrayList(keyword.getKeywordName()), extractor, fileName);
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
                            .extractUnifiedVariables(newArrayList(token), new VariableExtractor(), null);
                    arguments.put(unifiedDefinitionName,
                            Iterables.getFirst(usedVariables.get(unifiedDefinitionName), null));
                } else {
                    arguments.putAll(
                            VariableNamesSupport.extractUnifiedVariables(newArrayList(token), extractor, fileName));
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
        final List<KeywordArguments> arguments = keyword.getArguments();
        if (arguments == null || arguments.isEmpty()) {
            return;
        }
        for (final KeywordArguments argSetting : arguments) {
            final Set<String> definedVariables = newHashSet(validationContext.getAccessibleVariables());
            for (final RobotToken argToken : argSetting.getArguments()) {
                if (!argToken.getText().contains("}") || (!argToken.getText().startsWith("${")
                        && !argToken.getText().startsWith("@{") && !argToken.getText().startsWith("&{"))) {
                    final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.INVALID_KEYWORD_ARG_SYNTAX)
                            .formatMessageWith(argToken.getText());
                    reporter.handleProblem(problem, validationContext.getFile(), argToken);
                } else {
                    final boolean hasDefault = argToken.getText().contains("=");
                    if (hasDefault) {
                        final List<String> splitted = Splitter.on('=').limit(2).splitToList(argToken.getText());
                        final String def = splitted.get(0);
                        if (!def.trim().equals(def)) {
                            final RobotProblem problem = RobotProblem
                                    .causedBy(KeywordsProblem.INVALID_KEYWORD_ARG_SYNTAX)
                                    .formatMessageWith(argToken.getText());
                            reporter.handleProblem(problem, validationContext.getFile(), argToken);
                        }

                        final String unifiedDefinitionName = VariableNamesSupport.extractUnifiedVariableName(def);
                        final Multimap<String, RobotToken> usedVariables = VariableNamesSupport.extractUnifiedVariables(
                                newArrayList(argToken), new VariableExtractor(), validationContext.getFile().getName());

                        for (final Entry<String, RobotToken> entry : usedVariables.entries()) {
                            if (!unifiedDefinitionName.equals(entry.getKey())
                                    && !VariableNamesSupport.isDefinedVariable(VariableNamesSupport
                                            .extractUnifiedVariableNameWithoutBrackets(entry.getKey()), "$",
                                            definedVariables)) {
                                final RobotProblem problem = RobotProblem
                                        .causedBy(VariablesProblem.UNDECLARED_VARIABLE_USE)
                                        .formatMessageWith(entry.getKey());
                                final Map<String, Object> additional = ImmutableMap
                                        .<String, Object> of(AdditionalMarkerAttributes.NAME, entry.getKey());
                                reporter.handleProblem(problem, validationContext.getFile(), entry.getValue(),
                                        additional);
                            }
                        }
                        definedVariables.add(unifiedDefinitionName);
                    } else {
                        definedVariables.add(VariableNamesSupport.extractUnifiedVariableName(argToken.getText()));
                    }
                }
            }
        }
    }

    private boolean isDefaultArgument(final RobotToken argumentToken) {
        return argumentToken.getText().contains("}=");
    }

    private void reportKeywordUsageProblemsInUserKeywordSettings() {
        for (final KeywordTeardown keywordTeardown : keyword.getTeardowns()) {
            final RobotToken keywordNameToken = keywordTeardown.getKeywordName();
            if (keywordNameToken != null) {
                TestCasesTableValidator.reportKeywordUsageProblemsInSetupAndTeardownSetting(validationContext, reporter,
                        keywordNameToken, Optional.of(keywordTeardown.getArguments()));
            }
        }
    }
}
