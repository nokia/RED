/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

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

    private static final Set<String> SPECIAL_KEYWORD_NAMES = Streams
            .concat(VARS_CREATING_KEYWORDS.stream(), VARS_SYNTAX_CHECKING_KEYWORDS.stream(),
                    VARS_OMITTING_KEYWORDS.stream(), RUN_KEYWORD_VARIANTS.keySet().stream())
            .map(QualifiedKeywordName::getKeywordName)
            .collect(toSet());

    public static boolean mayBeSpecialKeyword(final String keywordName) {
        return SPECIAL_KEYWORD_NAMES.contains(QualifiedKeywordName.unifyDefinition(keywordName));
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
        if (VARS_CREATING_KEYWORDS.contains(keywordName)) {
            return lineDescriptor.getKeywordArguments()
                    .stream()
                    .findFirst()
                    .map(Stream::of)
                    .orElseGet(() -> Stream.empty())
                    .filter(arg -> !arg.getTypes().contains(RobotTokenType.VARIABLES_WRONG_DEFINED))
                    .map(RobotToken::getText)
                    .filter(VariableNamesSupport::isCleanVariable)
                    .collect(toSet());
        }
        return new HashSet<>();
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
}
