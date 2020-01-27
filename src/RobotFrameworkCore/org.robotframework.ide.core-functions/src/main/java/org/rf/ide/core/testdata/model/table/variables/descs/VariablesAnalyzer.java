/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs;

import java.util.function.Consumer;
import java.util.stream.Stream;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.table.variables.descs.impl.old.OldVariablesAnalyzer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Interface for analyzing variables usages in any tokens/expressions.
 * 
 * @author anglart
 */
public interface VariablesAnalyzer {

    public static final String ALL = "$@&%";
    public static final String ALL_ROBOT = "$@&";

    public static VariablesAnalyzer analyzer(final RobotVersion version) {
        return analyzer(version, ALL);
    }

    public static VariablesAnalyzer analyzer(final RobotVersion version, final String possibleVariableMarks) {
        return new OldVariablesAnalyzer(possibleVariableMarks);
    }

    public static String normalizeName(final RobotToken variableToken) {
        return normalizeName(variableToken.getText());
    }

    public static String normalizeName(final String variable) {
        return variable != null ? variable.toLowerCase().replaceAll("_", "").replaceAll(" ", "") : "";
    }

    public static String extractFromBrackets(final String variable) {
        String name = "";
        if (variable != null && variable.length() >= 3 && variable.charAt(1) == '{' && variable.endsWith("}")) {
            name = variable.substring(2, variable.length() - 1);
        }
        return name;
    }

    public static boolean hasEqualNormalizedNames(final String firstVariable, final String secondVariable) {
        return normalizeName(extractFromBrackets(firstVariable))
                .equals(normalizeName(extractFromBrackets(secondVariable)));
    }

    public default boolean containsVariables(final String text) {
        return containsVariables(RobotToken.create(text, new FilePosition(0, 0, 0), RobotTokenType.VARIABLE_USAGE));
    }

    public default boolean containsVariables(final RobotToken token) {
        return getVariables(token).findAny().isPresent();
    }

    public default Stream<VariableUse> getVariablesUses(final String text) {
        return getVariablesUses(text, msg -> {});
    }

    public default Stream<VariableUse> getVariablesUses(final RobotToken token) {
        return getVariablesUses(token, msg -> {});
    }

    public default Stream<VariableUse> getVariablesUses(final String text,
            final Consumer<BuildMessage> parseProblemsConsumer) {
        return getVariablesUses(RobotToken.create(text, new FilePosition(0, 0, 0), RobotTokenType.VARIABLE_USAGE),
                parseProblemsConsumer);
    }

    public Stream<VariableUse> getVariablesUses(final RobotToken token, Consumer<BuildMessage> parseProblemsConsumer);

    public default Stream<RobotToken> getVariables(final String text) {
        return getVariables(RobotToken.create(text, new FilePosition(0, 0, 0), RobotTokenType.VARIABLE_USAGE));
    }

    public default Stream<RobotToken> getVariables(final RobotToken token) {
        return getVariablesUses(token).map(VariableUse::asToken);
    }

    public default Multimap<String, RobotToken> getVariablesUnified(final RobotToken token) {
        final Multimap<String, RobotToken> vars = ArrayListMultimap.create();
        getVariables(token).forEach(varToken -> vars.put(normalizeName(varToken), varToken));
        return vars;
    }

    public default void visitVariables(final String text, final VariablesVisitor visitor) {
        visitVariables(RobotToken.create(text, new FilePosition(0, 0, 0), RobotTokenType.VARIABLE_USAGE), visitor);
    }

    public void visitVariables(RobotToken token, VariablesVisitor visitor);

    public default void visitExpression(final String text, final ExpressionVisitor visitor) {
        visitExpression(RobotToken.create(text, new FilePosition(0, 0, 0), RobotTokenType.VARIABLE_USAGE), visitor);
    }

    public void visitExpression(RobotToken token, ExpressionVisitor visitor);

}
