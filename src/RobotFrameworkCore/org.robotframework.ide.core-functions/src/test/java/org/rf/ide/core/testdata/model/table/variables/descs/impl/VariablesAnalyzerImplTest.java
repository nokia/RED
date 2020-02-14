/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs.impl;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.table.variables.descs.ExpressionVisitor;
import org.rf.ide.core.testdata.model.table.variables.descs.VariableUse;
import org.rf.ide.core.testdata.model.table.variables.descs.VariablesAnalyzer;
import org.rf.ide.core.testdata.model.table.variables.descs.VariablesVisitor;

@DisplayName("When expression is analyzed")
public class VariablesAnalyzerImplTest {

    private static final int NO_FLAGS = 0;
    private static final int DYNAMIC = 1 << 0;
    private static final int INDEXED = 1 << 1;
    private static final int INVALID = 1 << 2;
    private static final int PLAIN_VAR = 1 << 3;
    private static final int PLAIN_VAR_ASSIGN = 1 << 4;

    @DisplayName("for valid static variables usages")
    @Nested
    class UsedVariablesTest {

        @DisplayName("they cannot be found in plain text expression")
        @Test
        public void noUsesFoundInOrdinaryExpressions() {
            assertThat(extractUsedVariables("")).isEmpty();
            assertThat(extractUsedVariables("1")).isEmpty();
            assertThat(extractUsedVariables("12")).isEmpty();
            assertThat(extractUsedVariables("1[2")).isEmpty();
            assertThat(extractUsedVariables("1]2")).isEmpty();
            assertThat(extractUsedVariables("1[[2")).isEmpty();
            assertThat(extractUsedVariables("1]]2")).isEmpty();
            assertThat(extractUsedVariables("1[]2")).isEmpty();
            assertThat(extractUsedVariables("1][2")).isEmpty();
            assertThat(extractUsedVariables("1[0]2")).isEmpty();
        }

        @DisplayName("they cannot be found in expressions with invalid variables")
        @Test
        public void noUsesFoundInExpressionsWithInvalidVariables() {
            assertThat(extractUsedVariables("${")).isEmpty();
            assertThat(extractUsedVariables("${x")).isEmpty();
            assertThat(extractUsedVariables("${x${y")).isEmpty();
        }

        @DisplayName("they are found when plain variables are used")
        @Test
        public void usesAreFoundInSimpleExpressions() {
            assertThat(extractUsedVariables("${}")).containsExactly(
                    tuple(0, 3, "${}", "", PLAIN_VAR | PLAIN_VAR_ASSIGN));
            assertThat(extractUsedVariables("${x}")).containsExactly(
                    tuple(0, 4, "${x}", "x", PLAIN_VAR | PLAIN_VAR_ASSIGN));
            assertThat(extractUsedVariables("1${x}")).containsExactly(tuple(1, 5, "${x}", "x", NO_FLAGS));
            assertThat(extractUsedVariables("${x}2")).containsExactly(tuple(0, 4, "${x}", "x", NO_FLAGS));
            assertThat(extractUsedVariables("1${x}2")).containsExactly(tuple(1, 5, "${x}", "x", NO_FLAGS));
            assertThat(extractUsedVariables("${x}}")).containsExactly(tuple(0, 4, "${x}", "x", NO_FLAGS));
            assertThat(extractUsedVariables("{${x}}")).containsExactly(tuple(1, 5, "${x}", "x", NO_FLAGS));
            assertThat(extractUsedVariables("${x}[0]")).containsExactly(tuple(0, 7, "${x}[0]", "x", INDEXED));
            assertThat(extractUsedVariables("${x[0]}")).containsExactly(tuple(0, 7, "${x[0]}", "x", NO_FLAGS));
            assertThat(extractUsedVariables("1${x}2${y}3")).containsExactly(
                    tuple(1, 5, "${x}", "x", NO_FLAGS),
                    tuple(6, 10, "${y}", "y", NO_FLAGS));
            assertThat(extractUsedVariables("${a b c}")).containsExactly(
                    tuple(0, 8, "${a b c}", "a b c", PLAIN_VAR | PLAIN_VAR_ASSIGN));
            assertThat(extractUsedVariables("${a_b_c}")).containsExactly(
                    tuple(0, 8, "${a_b_c}", "a_b_c", PLAIN_VAR | PLAIN_VAR_ASSIGN));
        }

        @DisplayName("they are found when plain variables are used inside indexes")
        @Test
        public void usesAreFoundInsideIndexes() {
            assertThat(extractUsedVariables("${x}[${y}]")).containsExactly(
                    tuple(0, 10, "${x}[${y}]", "x", INDEXED),
                    tuple(5, 9, "${y}", "y", NO_FLAGS));
            assertThat(extractUsedVariables("${x}[${y}][0]")).containsExactly(
                    tuple(0, 13, "${x}[${y}][0]", "x", INDEXED),
                    tuple(5, 9, "${y}", "y", NO_FLAGS));
            assertThat(extractUsedVariables("${x}[${y}][${z}]")).containsExactly(
                    tuple(0, 16, "${x}[${y}][${z}]", "x", INDEXED),
                    tuple(5, 9, "${y}", "y", NO_FLAGS),
                    tuple(11, 15, "${z}", "z", NO_FLAGS));
            assertThat(extractUsedVariables("${x}[${y}[0]][${z}[1]]")).containsExactly(
                    tuple(0, 22, "${x}[${y}[0]][${z}[1]]", "x", INDEXED),
                    tuple(5, 12, "${y}[0]", "y", INDEXED),
                    tuple(14, 21, "${z}[1]", "z", INDEXED));
        }

        @DisplayName("they are found when plain variables are nested inside other variables")
        @Test
        public void usesAreFoundInNonNestedExpressions() {
            assertThat(extractUsedVariables("${x${y}}")).containsExactly(tuple(3, 7, "${y}", "y", NO_FLAGS));
            assertThat(extractUsedVariables("${x[${y}]}")).containsExactly(
                    tuple(0, 10, "${x[${y}]}", "x", NO_FLAGS),
                    tuple(4, 8, "${y}", "y", NO_FLAGS));
            assertThat(extractUsedVariables("${x${y}[${z[${w}]}]}")).containsExactly(
                    tuple(3, 19, "${y}[${z[${w}]}]", "y", INDEXED),
                    tuple(8, 18, "${z[${w}]}", "z", NO_FLAGS),
                    tuple(12, 16, "${w}", "w", NO_FLAGS));
        }

        @DisplayName("they are found when variables with extended syntax are used")
        @Test
        public void usesAreFoundInExtendedSyntaxVariables() {
            assertThat(extractUsedVariables("${x+2}")).containsExactly(
                    tuple(0, 6, "${x+2}", "x", PLAIN_VAR | PLAIN_VAR_ASSIGN));
            assertThat(extractUsedVariables("${x*3}")).containsExactly(
                    tuple(0, 6, "${x*3}", "x", PLAIN_VAR | PLAIN_VAR_ASSIGN));
            assertThat(extractUsedVariables("${x+'abc'}")).containsExactly(
                    tuple(0, 10, "${x+'abc'}", "x", PLAIN_VAR | PLAIN_VAR_ASSIGN));
            assertThat(extractUsedVariables("${x+\"abc\"}")).containsExactly(
                    tuple(0, 10, "${x+\"abc\"}", "x", PLAIN_VAR | PLAIN_VAR_ASSIGN));
            assertThat(extractUsedVariables("${x.field}")).containsExactly(
                    tuple(0, 10, "${x.field}", "x", PLAIN_VAR | PLAIN_VAR_ASSIGN));
            assertThat(extractUsedVariables("${x.call()}")).containsExactly(
                    tuple(0, 11, "${x.call()}", "x", PLAIN_VAR | PLAIN_VAR_ASSIGN));
        }

        @DisplayName("they are found when numbers are used")
        @Test
        public void usesAreFoundInNumbers() {
            assertThat(extractUsedVariables("${42}"))
                    .containsExactly(tuple(0, 5, "${42}", "42", PLAIN_VAR | PLAIN_VAR_ASSIGN));
            assertThat(extractUsedVariables("${1729}"))
                    .containsExactly(tuple(0, 7, "${1729}", "1729", PLAIN_VAR | PLAIN_VAR_ASSIGN));
            assertThat(extractUsedVariables("${13.8}"))
                    .containsExactly(tuple(0, 7, "${13.8}", "13", PLAIN_VAR | PLAIN_VAR_ASSIGN));
            assertThat(extractUsedVariables("${10+12}"))
                    .containsExactly(tuple(0, 8, "${10+12}", "10", PLAIN_VAR | PLAIN_VAR_ASSIGN));
            assertThat(extractUsedVariables("${3*3}"))
                    .containsExactly(tuple(0, 6, "${3*3}", "3", PLAIN_VAR | PLAIN_VAR_ASSIGN));
        }
    }

    @DisplayName("for top-level expression parts")
    @Nested
    class VisitingExpression {

        @DisplayName("nothing is found in empty expression")
        @Test
        public void noPartsAreFoundInEmptyExpression() {
            assertThat(extractVisitedExpressionParts("")).isEmpty();
        }

        @DisplayName("there is a single text element when no variable is used")
        @Test
        public void singleTextPartIsFoundInTextOnlyExpression() {
            assertThat(extractVisitedExpressionParts("1")).containsExactly(tuple(0, 1, "1"));
            assertThat(extractVisitedExpressionParts("12")).containsExactly(tuple(0, 2, "12"));
            assertThat(extractVisitedExpressionParts("123")).containsExactly(tuple(0, 3, "123"));
            assertThat(extractVisitedExpressionParts("1[2")).containsExactly(tuple(0, 3, "1[2"));
            assertThat(extractVisitedExpressionParts("1[[2")).containsExactly(tuple(0, 4, "1[[2"));
            assertThat(extractVisitedExpressionParts("1]2")).containsExactly(tuple(0, 3, "1]2"));
            assertThat(extractVisitedExpressionParts("1]]2")).containsExactly(tuple(0, 4, "1]]2"));
            assertThat(extractVisitedExpressionParts("1][2")).containsExactly(tuple(0, 4, "1][2"));
            assertThat(extractVisitedExpressionParts("1[2]3")).containsExactly(tuple(0, 5, "1[2]3"));
            assertThat(extractVisitedExpressionParts("1{2")).containsExactly(tuple(0, 3, "1{2"));
            assertThat(extractVisitedExpressionParts("1{{2")).containsExactly(tuple(0, 4, "1{{2"));
            assertThat(extractVisitedExpressionParts("1}2")).containsExactly(tuple(0, 3, "1}2"));
            assertThat(extractVisitedExpressionParts("1}}2")).containsExactly(tuple(0, 4, "1}}2"));
            assertThat(extractVisitedExpressionParts("1}{2")).containsExactly(tuple(0, 4, "1}{2"));
            assertThat(extractVisitedExpressionParts("1{2}3")).containsExactly(tuple(0, 5, "1{2}3"));
            assertThat(extractVisitedExpressionParts("1$3")).containsExactly(tuple(0, 3, "1$3"));
            assertThat(extractVisitedExpressionParts("1@3")).containsExactly(tuple(0, 3, "1@3"));
            assertThat(extractVisitedExpressionParts("1&3")).containsExactly(tuple(0, 3, "1&3"));
            assertThat(extractVisitedExpressionParts("1%3")).containsExactly(tuple(0, 3, "1%3"));
            assertThat(extractVisitedExpressionParts("12=")).containsExactly(tuple(0, 3, "12="));
        }

        @DisplayName("there is a single variable element when plain invalid variable is used")
        @Test
        public void singleInvalidVarIsFoundInPlainInvalidVarExpression() {
            assertThat(extractVisitedExpressionParts("${")).containsExactly(tuple(0, 2, "${", "", INVALID));
            assertThat(extractVisitedExpressionParts("${x")).containsExactly(tuple(0, 3, "${x", "x", INVALID));
        }

        @DisplayName("there is a single variable element when plain variable is used")
        @Test
        public void singleVarIsFoundInPlainVarExpression() {
            assertThat(extractVisitedExpressionParts("${x}")).containsExactly(
                    tuple(0, 4, "${x}", "x", PLAIN_VAR | PLAIN_VAR_ASSIGN));
            assertThat(extractVisitedExpressionParts("${abc}")).containsExactly(
                    tuple(0, 6, "${abc}", "abc", PLAIN_VAR | PLAIN_VAR_ASSIGN));
        }

        @DisplayName("there is a single variable element when extended variable syntax is used")
        @Test
        public void singleVarIsFoundInExtendedSyntaxVariablesExpression() {
            assertThat(extractVisitedExpressionParts("${x+1}")).containsExactly(
                    tuple(0, 6, "${x+1}", "x", PLAIN_VAR | PLAIN_VAR_ASSIGN));
            assertThat(extractVisitedExpressionParts("${x+'a'}")).containsExactly(
                    tuple(0, 8, "${x+'a'}", "x", PLAIN_VAR | PLAIN_VAR_ASSIGN));
            assertThat(extractVisitedExpressionParts("${x.field}")).containsExactly(
                    tuple(0, 10, "${x.field}", "x", PLAIN_VAR | PLAIN_VAR_ASSIGN));
            assertThat(extractVisitedExpressionParts("${x.call()}")).containsExactly(
                    tuple(0, 11, "${x.call()}", "x", PLAIN_VAR | PLAIN_VAR_ASSIGN));
            assertThat(extractVisitedExpressionParts("${x[0]}"))
                    .containsExactly(tuple(0, 7, "${x[0]}", "x", NO_FLAGS));
        }

        @DisplayName("there is a single variable element when there are nested variables")
        @Test
        public void singleVarIsFoundInExpressionWhereVariablesAreNested() {
            assertThat(extractVisitedExpressionParts("${x${y}}")).containsExactly(
                    tuple(0, 8, "${x${y}}", "x", DYNAMIC));
            assertThat(extractVisitedExpressionParts("${x${y}z}")).containsExactly(
                    tuple(0, 9, "${x${y}z}", "x", DYNAMIC));
            assertThat(extractVisitedExpressionParts("${x${y${z}}}")).containsExactly(
                    tuple(0, 12, "${x${y${z}}}", "x", DYNAMIC));
            assertThat(extractVisitedExpressionParts("${x[${y}]}")).containsExactly(
                    tuple(0, 10, "${x[${y}]}", "x", NO_FLAGS));
        }

        @DisplayName("there are variables and text parts found")
        @Test
        public void multipleTextAndVariablePartsAreFoundInExpression() {
            assertThat(extractVisitedExpressionParts("${x}3")).containsExactly(
                    tuple(0, 4, "${x}", "x", NO_FLAGS),
                    tuple(4, 5, "3"));
            assertThat(extractVisitedExpressionParts("1${x}")).containsExactly(
                    tuple(0, 1, "1"),
                    tuple(1, 5, "${x}", "x", NO_FLAGS));
            assertThat(extractVisitedExpressionParts("1${x}3")).containsExactly(
                    tuple(0, 1, "1"),
                    tuple(1, 5, "${x}", "x", NO_FLAGS),
                    tuple(5, 6, "3"));
            assertThat(extractVisitedExpressionParts("1${x}3${y}5")).containsExactly(
                    tuple(0, 1, "1"),
                    tuple(1, 5, "${x}", "x", NO_FLAGS),
                    tuple(5, 6, "3"),
                    tuple(6, 10, "${y}", "y", NO_FLAGS),
                    tuple(10, 11, "5"));
            assertThat(extractVisitedExpressionParts("[${x}]")).containsExactly(
                    tuple(0, 1, "["),
                    tuple(1, 5, "${x}", "x", NO_FLAGS),
                    tuple(5, 6, "]"));
            assertThat(extractVisitedExpressionParts("${x}1[0]")).containsExactly(
                    tuple(0, 4, "${x}", "x", NO_FLAGS),
                    tuple(4, 8, "1[0]"));
            assertThat(extractVisitedExpressionParts("${x} [0]")).containsExactly(
                    tuple(0, 4, "${x}", "x", NO_FLAGS),
                    tuple(4, 8, " [0]"));
            assertThat(extractVisitedExpressionParts("1${x[0]}3${y}[${a}]5${z${w}}6")).containsExactly(
                    tuple(0, 1, "1"),
                    tuple(1, 8, "${x[0]}", "x", NO_FLAGS),
                    tuple(8, 9, "3"),
                    tuple(9, 19, "${y}[${a}]", "y", INDEXED),
                    tuple(19, 20, "5"),
                    tuple(20, 28, "${z${w}}", "z", DYNAMIC),
                    tuple(28, 29, "6"));
        }
        
        @DisplayName("there are variables and text parts found in expressions with different brackets interleaving like {[}]")
        @Test
        public void multipleTextVariablesAreFoundInExpressionWithInterleavingBrackets() {
            assertThat(extractVisitedExpressionParts("[${x]}")).containsExactly(
                    tuple(0, 1, "["),
                    tuple(1, 6, "${x]}", "x", NO_FLAGS));
            assertThat(extractVisitedExpressionParts("${x[}]")).containsExactly(
                    tuple(0, 5, "${x[}", "x", NO_FLAGS),
                    tuple(5, 6, "]"));
            assertThat(extractVisitedExpressionParts("${x${y}[}]")).containsExactly(
                    tuple(0, 9, "${x${y}[}", "x", DYNAMIC),
                    tuple(9, 10, "]"));
        }
    }

    @DisplayName("for all possible variables")
    @Nested
    class VistingVariables {

        @DisplayName("nothing is found in empty expression")
        @Test
        public void noVariablesFoundInEmptyExpression() {
            assertThat(extractVisitedVariables("")).isEmpty();
        }

        @DisplayName("nothing is found in text only expression")
        @Test
        public void noVariablesFoundInTextOnlyExpression() {
            assertThat(extractVisitedVariables("1")).isEmpty();
            assertThat(extractVisitedVariables("12")).isEmpty();
            assertThat(extractVisitedVariables("123")).isEmpty();
            assertThat(extractVisitedVariables("1[2")).isEmpty();
            assertThat(extractVisitedVariables("1[[2")).isEmpty();
            assertThat(extractVisitedVariables("1]2")).isEmpty();
            assertThat(extractVisitedVariables("1]]2")).isEmpty();
            assertThat(extractVisitedVariables("1][2")).isEmpty();
            assertThat(extractVisitedVariables("1[2]3")).isEmpty();
            assertThat(extractVisitedVariables("1{2")).isEmpty();
            assertThat(extractVisitedVariables("1{{2")).isEmpty();
            assertThat(extractVisitedVariables("1}2")).isEmpty();
            assertThat(extractVisitedVariables("1}}2")).isEmpty();
            assertThat(extractVisitedVariables("1}{2")).isEmpty();
            assertThat(extractVisitedVariables("1{2}3")).isEmpty();
            assertThat(extractVisitedVariables("1$3")).isEmpty();
            assertThat(extractVisitedVariables("1@3")).isEmpty();
            assertThat(extractVisitedVariables("1&3")).isEmpty();
            assertThat(extractVisitedVariables("1%3")).isEmpty();
            assertThat(extractVisitedVariables("12=")).isEmpty();
        }

        @DisplayName("there is a single variable element when plain invalid variable is used")
        @Test
        public void singleVariableIsFoundInPlainInvalidVarExpression() {
            assertThat(extractVisitedVariables("${")).containsExactly(tuple(0, 2, "${", "", INVALID));
            assertThat(extractVisitedVariables("${x")).containsExactly(tuple(0, 3, "${x", "x", INVALID));
        }
        
        @DisplayName("there is a single variable element when plain variable is used")
        @Test
        public void singleVariableIsFoundInPlainVarExpression() {
            assertThat(extractVisitedVariables("${x}")).containsExactly(
                    tuple(0, 4, "${x}", "x", PLAIN_VAR | PLAIN_VAR_ASSIGN));
            assertThat(extractVisitedVariables("${abc}")).containsExactly(
                    tuple(0, 6, "${abc}", "abc", PLAIN_VAR | PLAIN_VAR_ASSIGN));
        }

        @DisplayName("there is a single variable element when extended variable syntax is used")
        @Test
        public void singleVariableIsFoundInExtendedSyntaxVariablesExpression() {
            assertThat(extractVisitedVariables("${x+1}")).containsExactly(
                    tuple(0, 6, "${x+1}", "x", PLAIN_VAR | PLAIN_VAR_ASSIGN));
            assertThat(extractVisitedVariables("${x+'a'}")).containsExactly(
                    tuple(0, 8, "${x+'a'}", "x", PLAIN_VAR | PLAIN_VAR_ASSIGN));
            assertThat(extractVisitedVariables("${x.field}")).containsExactly(
                    tuple(0, 10, "${x.field}", "x", PLAIN_VAR | PLAIN_VAR_ASSIGN));
            assertThat(extractVisitedVariables("${x.call()}")).containsExactly(
                    tuple(0, 11, "${x.call()}", "x", PLAIN_VAR | PLAIN_VAR_ASSIGN));
            assertThat(extractVisitedVariables("${x[0]}"))
                    .containsExactly(tuple(0, 7, "${x[0]}", "x", NO_FLAGS));
        }

        @DisplayName("there are variables found inside other")
        @Test
        public void multipleVariablesAreFoundInExpressionWhenVariablesAreNested() {
            assertThat(extractVisitedVariables("${x${y}}")).containsExactly(
                    tuple(0, 8, "${x${y}}", "x", DYNAMIC),
                    tuple(3, 7, "${y}", "y", NO_FLAGS));
            assertThat(extractVisitedVariables("${x${y}z}")).containsExactly(
                    tuple(0, 9, "${x${y}z}", "x", DYNAMIC),
                    tuple(3, 7, "${y}", "y", NO_FLAGS));
            assertThat(extractVisitedVariables("${x${y${z}}}")).containsExactly(
                    tuple(0, 12, "${x${y${z}}}", "x", DYNAMIC),
                    tuple(3, 11, "${y${z}}", "y", DYNAMIC),
                    tuple(6, 10, "${z}", "z", NO_FLAGS));
            assertThat(extractVisitedVariables("${x[${y}]}")).containsExactly(
                    tuple(0, 10, "${x[${y}]}", "x", NO_FLAGS),
                    tuple(4, 8, "${y}", "y", NO_FLAGS));
        }

        @DisplayName("there are variables found in joined expressions")
        @Test
        public void multipleVariablePartsAreFoundInExpression() {
            assertThat(extractVisitedVariables("${x}3")).containsExactly(
                    tuple(0, 4, "${x}", "x", NO_FLAGS));
            assertThat(extractVisitedVariables("1${x}")).containsExactly(
                    tuple(1, 5, "${x}", "x", NO_FLAGS));
            assertThat(extractVisitedVariables("1${x}3")).containsExactly(
                    tuple(1, 5, "${x}", "x", NO_FLAGS));
            assertThat(extractVisitedVariables("1${x}3${y}5")).containsExactly(
                    tuple(1, 5, "${x}", "x", NO_FLAGS),
                    tuple(6, 10, "${y}", "y", NO_FLAGS));
            assertThat(extractVisitedVariables("[${x}]")).containsExactly(
                    tuple(1, 5, "${x}", "x", NO_FLAGS));
            assertThat(extractVisitedVariables("${x}1[0]")).containsExactly(
                    tuple(0, 4, "${x}", "x", NO_FLAGS));
            assertThat(extractVisitedVariables("${x} [0]")).containsExactly(
                    tuple(0, 4, "${x}", "x", NO_FLAGS));
            assertThat(extractVisitedVariables("1${x[0]}3${y}[${a}]5${z${w}}6")).containsExactly(
                    tuple(1, 8, "${x[0]}", "x", NO_FLAGS),
                    tuple(9, 19, "${y}[${a}]", "y", INDEXED),
                    tuple(14, 18, "${a}", "a", NO_FLAGS),
                    tuple(20, 28, "${z${w}}", "z", DYNAMIC),
                    tuple(23, 27, "${w}", "w", NO_FLAGS));
        }
        
        @DisplayName("there are variables found in expressions with different brackets interleaving like {[}]")
        @Test
        public void multipleVariablesAreFoundInExpressionWithInterleavingBrackets() {
            assertThat(extractVisitedVariables("[${x]}")).containsExactly(
                    tuple(1, 6, "${x]}", "x", NO_FLAGS));
            assertThat(extractVisitedVariables("${x[}]")).containsExactly(
                    tuple(0, 5, "${x[}", "x", NO_FLAGS));
            assertThat(extractVisitedVariables("${x${y}[}]")).containsExactly(
                    tuple(0, 9, "${x${y}[}", "x", DYNAMIC),
                    tuple(3, 8, "${y}[", "y", INDEXED));
        }
    }

    private static List<Tuple> extractUsedVariables(final String expression) {
        final VariablesAnalyzerImpl analyzer = new VariablesAnalyzerImpl(VariablesAnalyzer.ALL);
        return analyzer.getDefinedVariablesUses(VariablesAnalyzer.asRobotToken(expression))
                .map(VariablesAnalyzerImplTest::extractUsageData)
                .collect(toList());
    }

    private static List<Tuple> extractVisitedVariables(final String expression) {
        final VariablesAnalyzerImpl analyzer = new VariablesAnalyzerImpl(VariablesAnalyzer.ALL);

        final List<Tuple> visited = new ArrayList<>();
        analyzer.visitVariables(VariablesAnalyzer.asRobotToken(expression),
                VariablesVisitor.variableUsagesVisitor(usage -> visited.add(extractUsageData(usage))));
        return visited;
    }

    private static List<Tuple> extractVisitedExpressionParts(final String expression) {
        final VariablesAnalyzerImpl analyzer = new VariablesAnalyzerImpl(VariablesAnalyzer.ALL);

        final List<Tuple> visited = new ArrayList<>();
        analyzer.visitExpression(VariablesAnalyzer.asRobotToken(expression), new ExpressionVisitor() {

            @Override
            public boolean visit(final String text, final FileRegion region) {
                visited.add(tuple(region.getStart().getOffset(), region.getEnd().getOffset(), text));
                return true;
            }

            @Override
            public boolean visit(final VariableUse usage) {
                visited.add(extractUsageData(usage));
                return true;
            }
        });
        return visited;
    }

    private static Tuple extractUsageData(final VariableUse usage) {
        final FileRegion region = usage.getRegion();
        return tuple(region.getStart().getOffset(), region.getEnd().getOffset(), usage.asToken().getText(),
                usage.getBaseName(), getFlags(usage));
    }

    private static int getFlags(final VariableUse use) {
        int result = 0;
        result += use.isDynamic() ? DYNAMIC : 0;
        result += use.isIndexed() ? INDEXED : 0;
        result += use.isInvalid() ? INVALID : 0;
        result += use.isPlainVariable() ? PLAIN_VAR : 0;
        result += use.isPlainVariableAssign() ? PLAIN_VAR_ASSIGN : 0;
        return result;
    }

}
