/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.names;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.rf.ide.core.testdata.model.table.exec.descs.TextPosition;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.MappingResult;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * @author mmarzec
 */
public class VariableNamesSupport {

    private static final Pattern VAR_PATTERN = Pattern.compile("^[@\\$&]\\{[^\\}]+\\}$");

    public static boolean isCleanVariable(final String input) {
        return VAR_PATTERN.matcher(input).matches();
    }

    public static Multimap<String, RobotToken> extractUnifiedVariables(final RobotToken assignment,
            final VariableExtractor extractor, final String fileName) {
        final Multimap<String, RobotToken> vars = ArrayListMultimap.create();
        final MappingResult mappingResult = extractor.extract(assignment, fileName);
        for (final VariableDeclaration variableDeclaration : mappingResult.getCorrectVariables()) {
            vars.put(extractUnifiedVariableName(variableDeclaration.asToken().getText()),
                    variableDeclaration.asToken());
        }
        return vars;
    }

    public static String extractUnifiedVariableName(final VariableDeclaration variable) {
        return extractUnifiedVariableName(variable.asToken().getText());
    }

    public static String extractUnifiedVariableName(final String variable) {
        return variable != null ? variable.toLowerCase().replaceAll("_", "").replaceAll(" ", "") : "";
    }

    public static String extractUnifiedVariableNameWithoutBrackets(final String variable) {
        String name = "";
        if (variable != null && variable.length() >= 3 && variable.contains("{") && variable.contains("}")) {
            name = variable.substring(2, variable.length() - 1);
        }
        return extractUnifiedVariableName(name);
    }

    public static boolean hasEqualNames(final String firstVariable, final String secondVariable) {
        return extractUnifiedVariableNameWithoutBrackets(firstVariable)
                .equals(extractUnifiedVariableNameWithoutBrackets(secondVariable));
    }

    public static boolean isDefinedVariable(final VariableDeclaration variableDeclaration,
            final Set<String> definedVariablesWithUnifiedNames) {
        return isDefinedVariable(variableDeclaration.getVariableName().getText(),
                variableDeclaration.getTypeIdentificator().getText(), definedVariablesWithUnifiedNames);
    }

    public static boolean isDefinedVariable(final String variableName, final String variableTypeIdentificator,
            final Set<String> definedVariablesWithUnifiedNames) {

        final List<String> possibleVariableDefinitions = extractPossibleVariableDefinitions(variableName);
        for (final String variableDefinition : possibleVariableDefinitions) {
            if (containsVariable(definedVariablesWithUnifiedNames, variableTypeIdentificator, variableDefinition)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDefinedVariableInsideComputation(final VariableDeclaration variableDeclaration,
            final Set<String> definedVariables) {
        final Optional<TextPosition> variableWithoutComputation = variableDeclaration.getTextWithoutComputation();
        if (variableWithoutComputation.isPresent() && (isNumber(variableWithoutComputation.get())
                || isDefinedVariable(variableWithoutComputation.get().getText(),
                        variableDeclaration.getTypeIdentificator().getText(), definedVariables))) {
            return true;
        }
        return false;
    }

    private static boolean isNumber(final TextPosition pos) {
        try {
            final String text = pos.getText();
            if (!text.isEmpty()) {
                Double.parseDouble(text);
                return true;
            }
        } catch (final NumberFormatException nfe) {
        }

        return false;
    }

    private static List<String> extractPossibleVariableDefinitions(final String variableName) {
        final List<String> possibleVariableDefinitions = new ArrayList<>();
        final String variableDefinition = extractUnifiedVariableName(variableName);

        final String varDefinitionWithBrackets = createVariableDefinitionWithBrackets(variableDefinition);
        possibleVariableDefinitions.add(varDefinitionWithBrackets);
        if (varDefinitionWithBrackets.contains(".")) {
            possibleVariableDefinitions.add(extractVariableFromDotsRepresentation(varDefinitionWithBrackets));
        }
        return possibleVariableDefinitions;
    }

    private static boolean containsVariable(final Set<String> definedVariables, final String varTypeIdentificator,
            final String varDefinitionWithBrackets) {
        return definedVariables.contains(varTypeIdentificator + varDefinitionWithBrackets)
                || (!varTypeIdentificator.equals("@") && definedVariables.contains("@" + varDefinitionWithBrackets))
                || (!varTypeIdentificator.equals("&") && definedVariables.contains("&" + varDefinitionWithBrackets))
                || (!varTypeIdentificator.equals("$") && definedVariables.contains("$" + varDefinitionWithBrackets));
    }

    private static String extractVariableFromDotsRepresentation(final String varDefinitionWithBrackets) {
        return varDefinitionWithBrackets.split("\\.")[0] + "}";
    }

    private static String createVariableDefinitionWithBrackets(final String varName) {
        return "{" + varName + "}";
    }
}
