/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.model.table.keywords.KeywordArguments;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTeardown;
import org.rf.ide.core.testdata.model.table.keywords.KeywordUnknownSettings;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.keywords.names.EmbeddedKeywordNamesSupport;
import org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;

import com.google.common.collect.ArrayListMultimap;
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
        reportDuplicatedArgumentSettings();
        reportDuplicatedArguments();
        reportArgumentsOrderProblems();
        reportKeywordUsageProblemsInUserKeywordSettings();
    }
    
    private void reportKeywordUsageProblemsInUserKeywordSettings() {
        for (final KeywordTeardown keywordTeardown : keyword.getTeardowns()) {
            final RobotToken keywordNameToken = keywordTeardown.getKeywordName();
            if (keywordNameToken != null) {
                TestCasesTableValidator.validateExistingKeywordCall(validationContext, reporter, keywordNameToken);
            }
        }
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

    private void reportDuplicatedArgumentSettings() {
        final String kwName = keyword.getKeywordName().getText();

        final IFile file = validationContext.getFile();
        final List<KeywordArguments> args = keyword.getArguments();
        if (args != null && args.size() > 1) {
            for (final KeywordArguments arg : args) {
                final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.ARGUMENT_SETTING_DEFINED_TWICE)
                        .formatMessageWith(kwName);
                reporter.handleProblem(problem, file, arg.getDeclaration());
            }
        }
        final String fileName = file.getName();
        if (args != null && !args.isEmpty() && hasEmbeddedArguments(fileName, keyword)) {
            final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.ARGUMENT_SETTING_DEFINED_TWICE)
                    .formatMessageWith(kwName);
            reporter.handleProblem(problem, file, keyword.getKeywordName());
        }
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
            arguments
                    .putAll(VariableNamesSupport.extractUnifiedVariables(argument.getArguments(), extractor, fileName));
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

    private boolean isDefaultArgument(final RobotToken argumentToken) {
        return argumentToken.getText().contains("}=");
    }
}
