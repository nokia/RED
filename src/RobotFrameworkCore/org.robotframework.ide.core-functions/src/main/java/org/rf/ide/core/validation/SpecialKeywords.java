/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.validation;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.collect.Streams;

public class SpecialKeywords {

    // keywords which creates variables from given arguments
    private static final Set<QualifiedKeywordName> VARS_CREATING_KEYWORDS = new HashSet<>();
    static {
        VARS_CREATING_KEYWORDS.add(QualifiedKeywordName.create("Set Test Variable", "BuiltIn"));
        VARS_CREATING_KEYWORDS.add(QualifiedKeywordName.create("Set Suite Variable", "BuiltIn"));
        VARS_CREATING_KEYWORDS.add(QualifiedKeywordName.create("Set Global Variable", "BuiltIn"));
    }

    // keywords for which some variables should not be checked for existence
    private static final Set<QualifiedKeywordName> VARS_OMITTING_KEYWORDS = new HashSet<>();
    static {
        VARS_OMITTING_KEYWORDS.add(QualifiedKeywordName.create("Comment", "BuiltIn"));
        VARS_OMITTING_KEYWORDS.add(QualifiedKeywordName.create("Set Test Variable", "BuiltIn"));
        VARS_OMITTING_KEYWORDS.add(QualifiedKeywordName.create("Set Suite Variable", "BuiltIn"));
        VARS_OMITTING_KEYWORDS.add(QualifiedKeywordName.create("Set Global Variable", "BuiltIn"));
        VARS_OMITTING_KEYWORDS.add(QualifiedKeywordName.create("Get Variable Value", "BuiltIn"));
        VARS_OMITTING_KEYWORDS.add(QualifiedKeywordName.create("Variable Should Exist", "BuiltIn"));
        VARS_OMITTING_KEYWORDS.add(QualifiedKeywordName.create("Variable Should Not Exist", "BuiltIn"));
    }
    
    // keywords for which some arguments should be validated for variable syntax
    private static final Set<QualifiedKeywordName> VARS_SYNTAX_CHECKING_KEYWORDS = new HashSet<>();
    static {
        VARS_SYNTAX_CHECKING_KEYWORDS.add(QualifiedKeywordName.create("Set Test Variable", "BuiltIn"));
        VARS_SYNTAX_CHECKING_KEYWORDS.add(QualifiedKeywordName.create("Set Suite Variable", "BuiltIn"));
        VARS_SYNTAX_CHECKING_KEYWORDS.add(QualifiedKeywordName.create("Set Global Variable", "BuiltIn"));
        VARS_SYNTAX_CHECKING_KEYWORDS.add(QualifiedKeywordName.create("Get Variable Value", "BuiltIn"));
        VARS_SYNTAX_CHECKING_KEYWORDS.add(QualifiedKeywordName.create("Variable Should Exist", "BuiltIn"));
        VARS_SYNTAX_CHECKING_KEYWORDS.add(QualifiedKeywordName.create("Variable Should Not Exist", "BuiltIn"));
    }

    private static final Map<QualifiedKeywordName, Integer> NESTED_EXECUTABLE_KEYWORDS = new HashMap<>();
    static {
        NESTED_EXECUTABLE_KEYWORDS.put(QualifiedKeywordName.create("Repeat Keyword", "BuiltIn"), 1);
        NESTED_EXECUTABLE_KEYWORDS.put(QualifiedKeywordName.create("Run Keyword", "BuiltIn"), 0);
        NESTED_EXECUTABLE_KEYWORDS.put(QualifiedKeywordName.create("Run Keyword And Continue On Failure", "BuiltIn"), 0);
        NESTED_EXECUTABLE_KEYWORDS.put(QualifiedKeywordName.create("Run Keyword And Expect Error", "BuiltIn"), 1);
        NESTED_EXECUTABLE_KEYWORDS.put(QualifiedKeywordName.create("Run Keyword And Ignore Error", "BuiltIn"), 0);
        NESTED_EXECUTABLE_KEYWORDS.put(QualifiedKeywordName.create("Run Keyword And Return", "BuiltIn"), 0);
        NESTED_EXECUTABLE_KEYWORDS.put(QualifiedKeywordName.create("Run Keyword And Return If", "BuiltIn"), 1);
        NESTED_EXECUTABLE_KEYWORDS.put(QualifiedKeywordName.create("Run Keyword And Return Status", "BuiltIn"), 0);
        NESTED_EXECUTABLE_KEYWORDS.put(QualifiedKeywordName.create("Run Keyword If All Critical Tests Passed", "BuiltIn"), 0);
        NESTED_EXECUTABLE_KEYWORDS.put(QualifiedKeywordName.create("Run Keyword If All Tests Passed", "BuiltIn"), 0);
        NESTED_EXECUTABLE_KEYWORDS.put(QualifiedKeywordName.create("Run Keyword If Any Critical Tests Failed", "BuiltIn"), 0);
        NESTED_EXECUTABLE_KEYWORDS.put(QualifiedKeywordName.create("Run Keyword If Any Tests Failed", "BuiltIn"), 0);
        NESTED_EXECUTABLE_KEYWORDS.put(QualifiedKeywordName.create("Run Keyword If Test Failed", "BuiltIn"), 0);
        NESTED_EXECUTABLE_KEYWORDS.put(QualifiedKeywordName.create("Run Keyword If Test Passed", "BuiltIn"), 0);
        NESTED_EXECUTABLE_KEYWORDS.put(QualifiedKeywordName.create("Run Keyword If Timeout Occurred", "BuiltIn"), 0);
        NESTED_EXECUTABLE_KEYWORDS.put(QualifiedKeywordName.create("Run Keyword Unless", "BuiltIn"), 1);
        NESTED_EXECUTABLE_KEYWORDS.put(QualifiedKeywordName.create("Wait Until Keyword Succeeds", "BuiltIn"), 2);

        // those have more complicated logic with ELSE-IF/ELSE, AND operands
        NESTED_EXECUTABLE_KEYWORDS.put(QualifiedKeywordName.create("Run Keyword If", "BuiltIn"), -1);
        NESTED_EXECUTABLE_KEYWORDS.put(QualifiedKeywordName.create("Run Keywords", "BuiltIn"), -1);
    }

    private static final Map<QualifiedKeywordName, Integer> RUN_KEYWORD_VARIANTS = new HashMap<>();
    static {
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Create Dictionary", "BuiltIn"), 0);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Get Variable Value", "BuiltIn"), 0);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Variable Should Exist", "BuiltIn"), 0);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Variable Should Not Exist", "BuiltIn"), 0);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Set Test Variable", "BuiltIn"), 0);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Set Suite Variable", "BuiltIn"), 0);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Set Global Variable", "BuiltIn"), 0);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Run Keyword", "BuiltIn"), 1);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Run Keywords", "BuiltIn"), 0);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Run Keyword If", "BuiltIn"), 2);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Run Keyword Unless", "BuiltIn"), 2);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Run Keyword And Ignore Error", "BuiltIn"), 1);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Run Keyword And Return Status", "BuiltIn"), 1);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Run Keyword And Continue On Failure", "BuiltIn"), 1);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Run Keyword And Expect Error", "BuiltIn"), 2);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Repeat Keyword", "BuiltIn"), 2);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Wait Until Keyword Succeeds", "BuiltIn"), 3);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Set Variable If", "BuiltIn"), 1);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Run Keyword If Test Failed", "BuiltIn"), 1);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Run Keyword If Test Passed", "BuiltIn"), 1);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Run Keyword If Timeout Occurred", "BuiltIn"), 1);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Run Keyword If All Critical Tests Passed", "BuiltIn"), 1);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Run Keyword If Any Critical Tests Failed", "BuiltIn"), 1);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Run Keyword If All Tests Passed", "BuiltIn"), 1);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Run Keyword If Any Tests Failed", "BuiltIn"), 1);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Return From Keyword", "BuiltIn"), 0);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Return From Keyword If", "BuiltIn"), 1);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Run Keyword And Return", "BuiltIn"), 1);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Run Keyword And Return If", "BuiltIn"), 2);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Pass Execution If", "BuiltIn"), 1);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Log Many", "BuiltIn"), 0);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Comment", "BuiltIn"), 0);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Import Library", "BuiltIn"), 0);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Import Variables", "BuiltIn"), 0);
        RUN_KEYWORD_VARIANTS.put(QualifiedKeywordName.create("Import Resource", "BuiltIn"), 0);
    }

    public static boolean isRunKeywordVariant(final QualifiedKeywordName keywordName) {
        return RUN_KEYWORD_VARIANTS.containsKey(keywordName);
    }

    /**
     * Returns collection of variables which were created by given keyword call.
     * 
     * Currently supports:
     * BuiltIn.Set Test Variable
     * BuiltIn.Set Suite Variable
     * BuiltIn.Set Global Variable
     */
    public static Collection<String> getCreatedVariables(final QualifiedKeywordName keywordName,
            final IExecutableRowDescriptor<?> lineDescriptor) {
        final Set<String> createdVariables = new HashSet<>();

        lineDescriptor.getCreatedVariables().stream().map(var -> VariableNamesSupport.extractUnifiedVariableName(var)).forEach(createdVariables::add);
        if (VARS_CREATING_KEYWORDS.contains(keywordName)) {
            lineDescriptor.getKeywordArguments()
                    .stream()
                    .findFirst()
                    .map(Stream::of)
                    .orElseGet(() -> Stream.empty())
                    .filter(arg -> !arg.getTypes().contains(RobotTokenType.VARIABLES_WRONG_DEFINED))
                    .map(RobotToken::getText)
                    .filter(VariableNamesSupport::isCleanVariable)
                    .forEach(createdVariables::add);
        }
        return createdVariables;
    }

    /**
     * Returns variables used by given keyword call. Normally those are all the variables
     * used in arguments of a call, but for following keywords some variables are removed:
     * 
     * BuiltIn.Comment - all variables are removed
     * BuiltIn.Set Test Variable - variable from first argument is removed
     * BuiltIn.Set Suite Variable - as above
     * BuiltIn.Set Global Variable - as above
     * BuiltIn.Get Variable Value - as above
     */
    public static List<VariableDeclaration> getUsedVariables(final QualifiedKeywordName keywordName,
            final IExecutableRowDescriptor<?> lineDescriptor) {
        if (VARS_OMITTING_KEYWORDS.contains(keywordName)) {
            if (QualifiedKeywordName.create("Comment", "BuiltIn").equals(keywordName)) {
                return new ArrayList<>();
            } else {
                final Optional<RobotToken> firstArgVar = lineDescriptor.getKeywordArguments()
                        .stream()
                        .findFirst()
                        .map(Stream::of)
                        .orElseGet(() -> Stream.empty())
                        .filter(arg -> !arg.getTypes().contains(RobotTokenType.VARIABLES_WRONG_DEFINED))
                        .filter(token -> VariableNamesSupport.isCleanVariable(token.getText()))
                        .findFirst();
                if (firstArgVar.isPresent()) {
                    return lineDescriptor.getUsedVariables()
                            .stream()
                            .filter(varDec -> varDec.findRobotTokenPosition()
                                    .isAfter(firstArgVar.get().getFilePosition()))
                            .collect(toList());
                }
            }
        }
        return lineDescriptor.getUsedVariables();
    }

    /**
     * Returns tokens which should be checked if is provided with variable syntax. Currently
     * supports:
     * 
     * BuiltIn.Set Test Variable
     * BuiltIn.Set Suite Variable
     * BuiltIn.Set Global Variable
     * BuiltIn.Get Variable Value
     * BuiltIn.Variable Should Exist
     * BuiltIn.Variable Should Not Exist
     */
    public static List<RobotToken> getArgumentsToValidateForVariablesSyntax(final QualifiedKeywordName keywordName,
            final List<RobotToken> args) {
        if (VARS_SYNTAX_CHECKING_KEYWORDS.contains(keywordName) && !args.isEmpty()) {
            // as of now all those keywords takes variable as first argument
            return newArrayList(args.get(0));
        }
        return new ArrayList<>();
    }

    public static NestedExecutables getNestedExecutables(final QualifiedKeywordName qualifiedKeywordName,
            final Object nestedExecutableParent, final List<RobotToken> arguments)
            throws NestedKeywordsSyntaxException {


        if (QualifiedKeywordName.create("Run Keyword If", "BuiltIn").equals(qualifiedKeywordName)) {
            return parseRunKeywordIfNestedExecutables(nestedExecutableParent, arguments);

        } else if (QualifiedKeywordName.create("Run Keywords", "BuiltIn").equals(qualifiedKeywordName)) {
            return parseRunKeywordsNestedExecutables(nestedExecutableParent, arguments);

        } else if (NESTED_EXECUTABLE_KEYWORDS.containsKey(qualifiedKeywordName)) {
            final NestedExecutables nested = new NestedExecutables();
            final int indexOfKeywordName = NESTED_EXECUTABLE_KEYWORDS.get(qualifiedKeywordName);
            nested.addOmittedTokens(arguments.subList(0, Math.min(indexOfKeywordName + 1, arguments.size())));
            if (indexOfKeywordName < arguments.size()) {
                nested.addExecutable(createExecutable(nestedExecutableParent, arguments.get(indexOfKeywordName),
                        arguments.subList(indexOfKeywordName + 1, arguments.size())));
            }
            return nested;
        }
        return new NestedExecutables();
    }

    private static NestedExecutables parseRunKeywordIfNestedExecutables(final Object nestedExecutableParent,
            final List<RobotToken> arguments) {
        final NestedExecutables nested = new NestedExecutables();
        if (arguments.size() < 2) {
            return nested;
        }
        
        final List<RobotToken> types = new ArrayList<>();
        final List<List<RobotToken>> splitted = new ArrayList<>();

        types.add(RobotToken.create("IF"));
        splitted.add(new ArrayList<>(arguments.subList(0, Math.min(arguments.size(), 2))));
        for (int i = 2; i < arguments.size(); i++) {
            final RobotToken arg = arguments.get(i);

            if (arg.getText().equals("ELSE IF") || arg.getText().equals("ELSE")) {
                splitted.add(new ArrayList<>());
                types.add(arg);

            } else {
                splitted.get(splitted.size() - 1).add(arg);
            }
        }
        validateElseIfSyntax(types, splitted);

        for (int i = 0; i < types.size(); i++) {
            final String type = types.get(i).getText();
            final List<RobotToken> nestedExecTokens = splitted.get(i);

            if (type.equals("ELSE")) {
                nested.addExecutable(createExecutable(nestedExecutableParent, nestedExecTokens.get(0),
                        nestedExecTokens.subList(1, nestedExecTokens.size())));
            } else { // must be ELSE IF or IF
                nested.addOmittedToken(nestedExecTokens.get(0)); // condition does not constitute nested exec
                nested.addExecutable(createExecutable(nestedExecutableParent, nestedExecTokens.get(1),
                        nestedExecTokens.subList(2, nestedExecTokens.size())));
            }
        }
        return nested;
    }

    private static void validateElseIfSyntax(final List<RobotToken> types, final List<List<RobotToken>> splitted) {
        final List<RobotToken> elseBranches = types.stream()
                .filter(token -> token.getText().equals("ELSE"))
                .collect(toList());

        if (elseBranches.size() > 1) {
            throw new NestedKeywordsSyntaxException("Multiple ELSE branches are defined", elseBranches);

        } else if (elseBranches.size() == 1 && !types.get(types.size() - 1).getText().equals("ELSE")) {
            throw new NestedKeywordsSyntaxException("ELSE branch should not be followed by ELSE IF branches",
                    elseBranches);
        }

        final List<String> messages = new ArrayList<>();
        final List<RobotToken> problematicTokens = new ArrayList<>();
        for (int i = 0; i < types.size(); i++) {
            final RobotToken type = types.get(i);
            
            if (type.getText().equals("ELSE IF") && splitted.get(i).size() < 2) {
                messages.add("ELSE IF branch requires condition and keyword to be defined");
                problematicTokens.add(type);

            } else if (type.getText().equals("ELSE") && splitted.get(i).size() < 1) {
                messages.add("ELSE branch requires keyword to be defined");
                problematicTokens.add(type);
            }
        }
        if (!messages.isEmpty()) {
            throw new NestedKeywordsSyntaxException(messages, problematicTokens);
        }
    }

    private static NestedExecutables parseRunKeywordsNestedExecutables(final Object nestedExecutableParent,
            final List<RobotToken> arguments) {
        final NestedExecutables nested = new NestedExecutables();
        if (arguments.stream().anyMatch(token -> token.getText().equals("AND"))) {

            RobotToken action = null;
            final List<RobotToken> args = new ArrayList<>();

            for (int i = 0; i < arguments.size(); i++) {
                final RobotToken arg = arguments.get(i);

                if (arg.getText().equals("AND") && action != null) {
                    nested.addExecutable(createExecutable(nestedExecutableParent, action, args));
                    action = null;
                    args.clear();

                } else if (action == null) {
                    action = arg;
                } else {
                    args.add(arg);
                }
            }
            if (action != null) {
                nested.addExecutable(createExecutable(nestedExecutableParent, action, args));
            }

        } else {
            for (final RobotToken arg : arguments) {
                nested.addExecutable(createExecutable(nestedExecutableParent, arg, new ArrayList<>()));
            }
        }
        return nested;
    }

    private static RobotExecutableRow<Object> createExecutable(final Object parent, final RobotToken action,
            final List<RobotToken> arguments) {
        final RobotExecutableRow<Object> row = new RobotExecutableRow<>();
        row.setParent(parent);
        row.setAction(action.copy());
        for (final RobotToken arg : arguments) {
            row.addArgument(arg.copy());
        }
        return row;
    }

    public static class NestedExecutables {

        private final List<RobotToken> tokensToCheckForVariables = new ArrayList<>();

        private final List<RobotExecutableRow<?>> rows = new ArrayList<>();

        private void addOmittedToken(final RobotToken token) {
            tokensToCheckForVariables.add(token);
        }

        private void addOmittedTokens(final List<RobotToken> tokens) {
            tokensToCheckForVariables.addAll(tokens);
        }

        public List<RobotToken> getOmittedTokens() {
            return tokensToCheckForVariables;
        }

        private void addExecutable(final RobotExecutableRow<?> row) {
            if (!row.getAction().getText().isEmpty()) {
                rows.add(row);
            }
        }

        public boolean hasNestedExecutables() {
            return !rows.isEmpty();
        }

        public List<RobotExecutableRow<?>> getExecutables() {
            return rows;
        }
    }

    public static class NestedKeywordsSyntaxException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private final List<RobotToken> problematicTokens;

        private List<String> messages;

        private NestedKeywordsSyntaxException(final String message, final List<RobotToken> problematicTokens) {
            super(message);
            this.problematicTokens = problematicTokens;
        }

        public NestedKeywordsSyntaxException(final List<String> messages, final List<RobotToken> problematicTokens) {
            super("");
            this.messages = messages;
            this.problematicTokens = problematicTokens;
        }

        public void forEachProblem(final BiConsumer<String, RobotToken> problemsConsumer) {
            if (messages == null) {
                problematicTokens.forEach(token -> problemsConsumer.accept(getMessage(), token));
            } else {
                Streams.forEachPair(messages.stream(), problematicTokens.stream(), problemsConsumer);
            }
        }
    }
}
