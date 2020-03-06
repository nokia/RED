/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs;

import java.util.stream.Stream;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.table.variables.descs.impl.VariablesAnalyzerImpl;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

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
        return new VariablesAnalyzerImpl(version, possibleVariableMarks);
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

    public static RobotToken asRobotToken(final String expression) {
        return RobotToken.create(expression, new FilePosition(0, 0, 0), RobotTokenType.VARIABLE_USAGE);
    }

    /**
     * Checks if given expression token contain variable.
     * 
     * @param token
     *            Expression token to be analysed for variable presence.
     * @return True when given expression token contain variable usage.
     */
    public default boolean containsVariables(final RobotToken token) {
        return getDefinedVariablesUses(token).findAny().isPresent();
    }

    /**
     * Gets the stream of {@link VariableUse} elements consisting of all defined variables. A
     * defined variable is a variable which:
     * <ul>
     * <li>has valid syntax,</li>
     * <li>it's name is known - it is not dynamic (e.g. <code>${var}</code> has known name, while
     * <code>${var${i}}</code> do not, although nested <code>${i}</code> has).
     * </ul>
     * 
     * @param token
     *            Expression token to be analysed for defined variable uses
     * @return Stream of valid defined variable uses inside given expression token. Note that
     *         regions of returned variable uses are adjusted with token position.
     */
    public Stream<VariableUse> getDefinedVariablesUses(final RobotToken token);

    /**
     * Analyses given expression token for variable usages and calls given visitor
     * {@link VariablesVisitor#visit(VariableUse)} method for each of those usages. The visited
     * usages may be invalid (e.g. having syntax errors like in <code>${var</code> expression) and
     * can be nested inside other variables (so that visitor will visit two variables in case of
     * <code>${var_{$i}}</code> expression).
     * 
     * @param token
     *            Expression token in which all variables should be visited.
     * @param visitor
     *            A visitor object responsible for handling variable visit.
     */
    public void visitVariables(RobotToken token, VariablesVisitor visitor);

    /**
     * Analysys given expression token for python expressions usages (in form
     * ${{expr}}/@{{expr}}/&{{expr}}
     * and calls given visitor {@link PythonExpressionVisitor#visit(PythonExpression)} method for
     * each of those usages.
     * 
     * @param token
     *            Expression token in which all python expressions should be visited.
     * @param visitor
     *            A visitor object responsible for handling expression visit.
     */
    public void visitPythonExpressions(RobotToken token, PythonExpressionVisitor visitor);

    /**
     * Analyses given expression token for variable and non-variable parts and calls given visitor
     * proper methods for var/non-var parts. The visitor will only be called for top-level elements
     * without entering nested elements.
     * <p>
     * For example in case of <code>a${x}b${var_${i}}c</code> the
     * visitor will first visit non-var part {@code a}, then var part <code>${x}</code>, non-var
     * part {@code b} followed by last var part <code>${var_${i}}</code> without going into nested
     * <code>${i}</code> and non-var part {@code c}.
     * </p>
     * 
     * @param token
     *            Expression token in which all variable/non-variable parts should be visited.
     * @param visitor
     *            A visitor object responsible for handling variable/non-variable visits.
     */
    public void visitExpression(RobotToken token, ExpressionVisitor visitor);

}
