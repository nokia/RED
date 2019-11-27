/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.debugshell;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.Test;
import org.rf.ide.core.execution.server.response.EvaluateExpression.ExpressionType;
import org.robotframework.ide.eclipse.main.plugin.views.debugshell.ShellDocument.CategorizedPosition;

public class ShellDocumentTest {

    @Test
    public void newlyCreatedDocumentIsInRobotMode() {
        final ShellDocument doc = new ShellDocument();

        assertThat(doc.getMode()).isEqualTo(ExpressionType.ROBOT);
        assertThat(doc.get()).isEqualTo("ROBOT> ");

        assertThat(doc.getPositionCategories()).contains(ShellDocument.CATEGORY_MODE_PROMPT,
                ShellDocument.CATEGORY_PROMPT_CONTINUATION, ShellDocument.CATEGORY_RESULT_SUCC,
                ShellDocument.CATEGORY_RESULT_ERROR);
        assertThat(doc.getModePromptPositionsStream()).containsExactly(promptPosition(0, 7));
        assertThat(doc.getPromptContinuationPositionsStream()).isEmpty();
        assertThat(doc.getAwaitingResultsPositionsStream()).isEmpty();
        assertThat(doc.getResultSuccessPositionsStream()).isEmpty();
        assertThat(doc.getResultErrorPositionsStream()).isEmpty();
    }

    @Test
    public void documentIsClearedButStaysInCurrentMode_whenResetIsDone() {
        final ShellDocument doc = new ShellDocument("\n");
        doc.append("robot expression");
        doc.executeExpression(s -> 1);
        doc.putEvaluationResult(1, ExpressionType.ROBOT, Optional.of("result"), Optional.empty());
        doc.switchToMode(ExpressionType.PYTHON);
        doc.append("python expression");
        doc.executeExpression(s -> 2);
        doc.putEvaluationResult(2, ExpressionType.PYTHON, Optional.empty(), Optional.of("error"));

        doc.reset();

        assertThat(doc.getMode()).isEqualTo(ExpressionType.PYTHON);
        assertThat(doc.get()).isEqualTo("PYTHON> ");

        assertThat(doc.getPositionCategories()).contains(ShellDocument.CATEGORY_MODE_PROMPT,
                ShellDocument.CATEGORY_PROMPT_CONTINUATION, ShellDocument.CATEGORY_RESULT_SUCC,
                ShellDocument.CATEGORY_RESULT_ERROR);
        assertThat(doc.getModePromptPositionsStream()).containsExactly(promptPosition(0, 8));
        assertThat(doc.getPromptContinuationPositionsStream()).isEmpty();
        assertThat(doc.getAwaitingResultsPositionsStream()).isEmpty();
        assertThat(doc.getResultSuccessPositionsStream()).isEmpty();
        assertThat(doc.getResultErrorPositionsStream()).isEmpty();
    }

    @Test
    public void modeChangeIsDoneProperly() {
        final ShellDocument doc = new ShellDocument("\n");
        doc.append("expression");

        assertThat(doc.getMode()).isEqualTo(ExpressionType.ROBOT);
        assertThat(doc.get()).isEqualTo("ROBOT> expression");
        assertThat(doc.getModePromptPositionsStream()).containsExactly(promptPosition(0, 7));

        doc.switchToMode(ExpressionType.PYTHON);
        assertThat(doc.getMode()).isEqualTo(ExpressionType.PYTHON);
        assertThat(doc.get()).isEqualTo("PYTHON> expression");
        assertThat(doc.getModePromptPositionsStream()).containsExactly(promptPosition(0, 8));

        doc.switchToMode(ExpressionType.VARIABLE);
        assertThat(doc.getMode()).isEqualTo(ExpressionType.VARIABLE);
        assertThat(doc.get()).isEqualTo("VARIABLE> expression");
        assertThat(doc.getModePromptPositionsStream()).containsExactly(promptPosition(0, 10));
    }

    @Test
    public void modeChangeIsDoneProperly_whenExpressionIsContinued() {
        final ShellDocument doc = new ShellDocument("\n");
        doc.append("expression");
        doc.continueExpressionInNewLine();
        doc.append("continuation");

        assertThat(doc.getMode()).isEqualTo(ExpressionType.ROBOT);
        assertThat(doc.get()).isEqualTo(content("ROBOT> expression", "...... continuation"));
        assertThat(doc.getModePromptPositionsStream()).containsExactly(promptPosition(0, 7));
        assertThat(doc.getPromptContinuationPositionsStream()).containsExactly(continuationPosition(18, 7));

        doc.switchToMode(ExpressionType.PYTHON);
        assertThat(doc.getMode()).isEqualTo(ExpressionType.PYTHON);
        assertThat(doc.get()).isEqualTo(content("PYTHON> expression", "....... continuation"));
        assertThat(doc.getModePromptPositionsStream()).containsExactly(promptPosition(0, 8));
        assertThat(doc.getPromptContinuationPositionsStream()).containsExactly(continuationPosition(19, 8));

        doc.switchToMode(ExpressionType.VARIABLE);
        assertThat(doc.getMode()).isEqualTo(ExpressionType.VARIABLE);
        assertThat(doc.get()).isEqualTo(content("VARIABLE> expression", "......... continuation"));
        assertThat(doc.getModePromptPositionsStream()).containsExactly(promptPosition(0, 10));
        assertThat(doc.getPromptContinuationPositionsStream()).containsExactly(continuationPosition(21, 10));
    }

    @Test
    public void modeChangeWithNewExpressionIsDoneProperly() {
        final ShellDocument doc = new ShellDocument("\n");
        doc.append("expression");
        doc.continueExpressionInNewLine();
        doc.append("continuation");

        assertThat(doc.getMode()).isEqualTo(ExpressionType.ROBOT);
        assertThat(doc.get()).isEqualTo(content("ROBOT> expression", "...... continuation"));
        assertThat(doc.getModePromptPositionsStream()).containsExactly(promptPosition(0, 7));
        assertThat(doc.getPromptContinuationPositionsStream()).containsExactly(continuationPosition(18, 7));

        doc.switchTo(ExpressionType.VARIABLE, "variable");
        assertThat(doc.getMode()).isEqualTo(ExpressionType.VARIABLE);
        assertThat(doc.get()).isEqualTo("VARIABLE> variable");
        assertThat(doc.getModePromptPositionsStream()).containsExactly(promptPosition(0, 10));
        assertThat(doc.getPromptContinuationPositionsStream()).isEmpty();
    }

    @Test
    public void modeChangeWithNewExpressionIsDoneProperly_whenNewExpressionIsMultiline() {
        final ShellDocument doc = new ShellDocument("\n");
        doc.append("expression");

        assertThat(doc.getMode()).isEqualTo(ExpressionType.ROBOT);
        assertThat(doc.get()).isEqualTo("ROBOT> expression");
        assertThat(doc.getModePromptPositionsStream()).containsExactly(promptPosition(0, 7));
        assertThat(doc.getPromptContinuationPositionsStream()).isEmpty();

        doc.switchTo(ExpressionType.PYTHON, "1\n+\n2");
        assertThat(doc.getMode()).isEqualTo(ExpressionType.PYTHON);
        assertThat(doc.get()).isEqualTo(content("PYTHON> 1", "....... +", "....... 2"));
        assertThat(doc.getModePromptPositionsStream()).containsExactly(promptPosition(0, 8));
        assertThat(doc.getPromptContinuationPositionsStream()).containsExactly(continuationPosition(10, 8),
                continuationPosition(20, 8));
    }

    @Test
    public void modeChangeWithNewExpressionIsDoneProperly_whenExpressionIsContinued() {
        final ShellDocument doc = new ShellDocument("\n");
        doc.append("expression");

        assertThat(doc.getMode()).isEqualTo(ExpressionType.ROBOT);
        assertThat(doc.get()).isEqualTo("ROBOT> expression");
        assertThat(doc.getModePromptPositionsStream()).containsExactly(promptPosition(0, 7));

        doc.switchTo(ExpressionType.PYTHON, "python expression");
        assertThat(doc.getMode()).isEqualTo(ExpressionType.PYTHON);
        assertThat(doc.get()).isEqualTo("PYTHON> python expression");
        assertThat(doc.getModePromptPositionsStream()).containsExactly(promptPosition(0, 8));

        doc.switchTo(ExpressionType.VARIABLE, "var");
        assertThat(doc.getMode()).isEqualTo(ExpressionType.VARIABLE);
        assertThat(doc.get()).isEqualTo("VARIABLE> var");
        assertThat(doc.getModePromptPositionsStream()).containsExactly(promptPosition(0, 10));
    }

    @Test
    public void continuationPromptIsEnterInNewLine_whenExpressionIsContinued() {
        final ShellDocument doc = new ShellDocument("\n");

        doc.continueExpressionInNewLine();

        assertThat(doc.get()).isEqualTo(content("ROBOT> ", "...... "));
        assertThat(doc.getPromptContinuationPositionsStream()).containsExactly(continuationPosition(8, 7));
    }

    @Test
    public void switchingBetweenExpressionsHistoryWorksProperly() {
        final ShellDocument doc = new ShellDocument("\n");
        doc.append("expr1");
        doc.executeExpression(s -> 1);
        doc.putEvaluationResult(1, ExpressionType.ROBOT, Optional.of("result1"), Optional.empty());
        doc.switchTo(ExpressionType.PYTHON, "expr2");
        doc.executeExpression(s -> 2);
        doc.putEvaluationResult(2, ExpressionType.PYTHON, Optional.of("result2"), Optional.empty());
        doc.switchTo(ExpressionType.VARIABLE, "expr3");
        doc.executeExpression(s -> 3);
        doc.putEvaluationResult(3, ExpressionType.VARIABLE, Optional.of("result3"), Optional.empty());
        
        doc.append("current expr");
        
        assertThat(doc.getMode()).isEqualTo(ExpressionType.VARIABLE);
        assertThat(doc.get()).isEqualTo(content(
                "ROBOT> expr1", "PASS: result1",
                "PYTHON> expr2", "result2",
                "VARIABLE> expr3", "result3",
                "VARIABLE> current expr"));
        
        doc.switchToPreviousExpression();
        assertThat(doc.getMode()).isEqualTo(ExpressionType.VARIABLE);
        assertThat(doc.get()).isEqualTo(content(
                "ROBOT> expr1", "PASS: result1",
                "PYTHON> expr2", "result2",
                "VARIABLE> expr3", "result3",
                "VARIABLE> expr3"));

        doc.switchToPreviousExpression();
        assertThat(doc.getMode()).isEqualTo(ExpressionType.PYTHON);
        assertThat(doc.get()).isEqualTo(content(
                "ROBOT> expr1", "PASS: result1",
                "PYTHON> expr2", "result2",
                "VARIABLE> expr3", "result3",
                "PYTHON> expr2"));

        doc.switchToPreviousExpression();
        assertThat(doc.getMode()).isEqualTo(ExpressionType.ROBOT);
        assertThat(doc.get()).isEqualTo(content(
                "ROBOT> expr1", "PASS: result1",
                "PYTHON> expr2", "result2",
                "VARIABLE> expr3", "result3",
                "ROBOT> expr1"));

        doc.switchToPreviousExpression();
        assertThat(doc.getMode()).isEqualTo(ExpressionType.ROBOT);
        assertThat(doc.get()).isEqualTo(content(
                "ROBOT> expr1", "PASS: result1",
                "PYTHON> expr2", "result2",
                "VARIABLE> expr3", "result3",
                "ROBOT> expr1"));

        doc.switchToNextExpression();
        assertThat(doc.getMode()).isEqualTo(ExpressionType.PYTHON);
        assertThat(doc.get()).isEqualTo(content(
                "ROBOT> expr1", "PASS: result1",
                "PYTHON> expr2", "result2",
                "VARIABLE> expr3", "result3",
                "PYTHON> expr2"));

        doc.switchToNextExpression();
        assertThat(doc.getMode()).isEqualTo(ExpressionType.VARIABLE);
        assertThat(doc.get()).isEqualTo(content(
                "ROBOT> expr1", "PASS: result1",
                "PYTHON> expr2", "result2",
                "VARIABLE> expr3", "result3",
                "VARIABLE> expr3"));

        doc.switchToNextExpression();
        assertThat(doc.getMode()).isEqualTo(ExpressionType.VARIABLE);
        assertThat(doc.get()).isEqualTo(content(
                "ROBOT> expr1", "PASS: result1",
                "PYTHON> expr2", "result2",
                "VARIABLE> expr3", "result3",
                "VARIABLE> expr3"));
    }

    @Test
    public void requestingExpressionExecutionPreparesAPlaceForResults_andGeneratesNewPrompt() {
        final ShellDocument doc = new ShellDocument("\n");
        doc.append("expr1");
        doc.executeExpression(s -> 1);
        doc.switchTo(ExpressionType.PYTHON, "expr2");
        doc.executeExpression(s -> 2);
        doc.switchTo(ExpressionType.VARIABLE, "expr3");
        doc.executeExpression(s -> 3);

        assertThat(doc.get()).isEqualTo(content(
                "ROBOT> expr1", "evaluating...",
                "PYTHON> expr2", "evaluating...",
                "VARIABLE> expr3", "evaluating...",
                "VARIABLE> "));
        assertThat(doc.getModePromptPositionsStream()).containsExactly(promptPosition(0, 7), promptPosition(27, 8),
                promptPosition(55, 10), promptPosition(85, 10));
        assertThat(doc.getAwaitingResultsPositionsStream()).containsOnly(awaitingPosition(1, 13, 13),
                awaitingPosition(2, 41, 13), awaitingPosition(3, 71, 13));
    }

    @Test
    public void writingResultsIsDoneInProperPlaces_independetlyOfOrderInWhichResultsAreComing() {
        final ShellDocument doc = new ShellDocument("\n");
        doc.append("robot pass expr");
        doc.executeExpression(s -> 1);
        doc.append("robot fail expr");
        doc.executeExpression(s -> 2);
        doc.switchTo(ExpressionType.PYTHON, "python pass expr");
        doc.executeExpression(s -> 3);
        doc.append("python fail expr");
        doc.executeExpression(s -> 4);
        doc.switchTo(ExpressionType.VARIABLE, "var pass expr");
        doc.executeExpression(s -> 5);
        doc.append("var fail expr");
        doc.executeExpression(s -> 6);

        assertThat(doc.get()).isEqualTo(content(
                "ROBOT> robot pass expr", "evaluating...",
                "ROBOT> robot fail expr", "evaluating...",
                "PYTHON> python pass expr", "evaluating...",
                "PYTHON> python fail expr", "evaluating...",
                "VARIABLE> var pass expr", "evaluating...",
                "VARIABLE> var fail expr", "evaluating...",
                "VARIABLE> "));
        assertThat(doc.getAwaitingResultCategoriesStream()).containsOnly(awaitingCategory(1), awaitingCategory(2),
                awaitingCategory(3), awaitingCategory(4), awaitingCategory(5), awaitingCategory(6));
        assertThat(doc.getAwaitingResultsPositionsStream()).containsOnly(awaitingPosition(1, 23, 13),
                awaitingPosition(2, 60, 13), awaitingPosition(3, 99, 13), awaitingPosition(4, 138, 13),
                awaitingPosition(5, 176, 13), awaitingPosition(6, 214, 13));
        assertThat(doc.getResultSuccessPositionsStream()).isEmpty();
        assertThat(doc.getResultErrorPositionsStream()).isEmpty();

        doc.putEvaluationResult(4, ExpressionType.PYTHON, Optional.empty(), Optional.of("py fail"));
        assertThat(doc.get()).isEqualTo(content(
                "ROBOT> robot pass expr", "evaluating...",
                "ROBOT> robot fail expr", "evaluating...",
                "PYTHON> python pass expr", "evaluating...",
                "PYTHON> python fail expr", "py fail",
                "VARIABLE> var pass expr", "evaluating...",
                "VARIABLE> var fail expr", "evaluating...",
                "VARIABLE> "));
        assertThat(doc.getAwaitingResultCategoriesStream()).containsOnly(awaitingCategory(1), awaitingCategory(2),
                awaitingCategory(3), awaitingCategory(5), awaitingCategory(6));
        assertThat(doc.getAwaitingResultsPositionsStream()).containsOnly(awaitingPosition(1, 23, 13),
                awaitingPosition(2, 60, 13), awaitingPosition(3, 99, 13), awaitingPosition(5, 170, 13),
                awaitingPosition(6, 208, 13));
        assertThat(doc.getResultSuccessPositionsStream()).isEmpty();
        assertThat(doc.getResultErrorPositionsStream()).containsOnly(failPosition(138, 7));


        doc.putEvaluationResult(6, ExpressionType.VARIABLE, Optional.empty(), Optional.of("var fail"));
        assertThat(doc.get()).isEqualTo(content(
                "ROBOT> robot pass expr", "evaluating...",
                "ROBOT> robot fail expr", "evaluating...",
                "PYTHON> python pass expr", "evaluating...",
                "PYTHON> python fail expr", "py fail",
                "VARIABLE> var pass expr", "evaluating...",
                "VARIABLE> var fail expr", "var fail",
                "VARIABLE> "));
        assertThat(doc.getAwaitingResultCategoriesStream()).containsOnly(awaitingCategory(1), awaitingCategory(2),
                awaitingCategory(3), awaitingCategory(5));
        assertThat(doc.getAwaitingResultsPositionsStream()).containsOnly(awaitingPosition(1, 23, 13),
                awaitingPosition(2, 60, 13), awaitingPosition(3, 99, 13), awaitingPosition(5, 170, 13));
        assertThat(doc.getResultSuccessPositionsStream()).isEmpty();
        assertThat(doc.getResultErrorPositionsStream()).containsOnly(failPosition(138, 7), failPosition(208, 8));
        
        doc.putEvaluationResult(5, ExpressionType.VARIABLE, Optional.of("var pass"), Optional.empty());
        assertThat(doc.get()).isEqualTo(content(
                "ROBOT> robot pass expr", "evaluating...",
                "ROBOT> robot fail expr", "evaluating...",
                "PYTHON> python pass expr", "evaluating...",
                "PYTHON> python fail expr", "py fail",
                "VARIABLE> var pass expr", "var pass",
                "VARIABLE> var fail expr", "var fail",
                "VARIABLE> "));
        assertThat(doc.getAwaitingResultCategoriesStream()).containsOnly(awaitingCategory(1), awaitingCategory(2),
                awaitingCategory(3));
        assertThat(doc.getAwaitingResultsPositionsStream()).containsOnly(awaitingPosition(1, 23, 13),
                awaitingPosition(2, 60, 13), awaitingPosition(3, 99, 13));
        assertThat(doc.getResultSuccessPositionsStream()).isEmpty();
        assertThat(doc.getResultErrorPositionsStream()).containsOnly(failPosition(138, 7), failPosition(203, 8));
        
        
        doc.putEvaluationResult(2, ExpressionType.ROBOT, Optional.empty(), Optional.of("robot fail"));
        assertThat(doc.get()).isEqualTo(content(
                "ROBOT> robot pass expr", "evaluating...",
                "ROBOT> robot fail expr", "FAIL: robot fail",
                "PYTHON> python pass expr", "evaluating...",
                "PYTHON> python fail expr", "py fail",
                "VARIABLE> var pass expr", "var pass",
                "VARIABLE> var fail expr", "var fail",
                "VARIABLE> "));
        assertThat(doc.getAwaitingResultCategoriesStream()).containsOnly(awaitingCategory(1), awaitingCategory(3));
        assertThat(doc.getAwaitingResultsPositionsStream()).containsOnly(awaitingPosition(1, 23, 13),
                awaitingPosition(3, 102, 13));
        assertThat(doc.getResultSuccessPositionsStream()).isEmpty();
        assertThat(doc.getResultErrorPositionsStream()).containsOnly(failPosition(60, 6), failPosition(141, 7),
                failPosition(206, 8));
        
        doc.putEvaluationResult(3, ExpressionType.PYTHON, Optional.of("py pass"), Optional.empty());
        assertThat(doc.get()).isEqualTo(content(
                "ROBOT> robot pass expr", "evaluating...",
                "ROBOT> robot fail expr", "FAIL: robot fail",
                "PYTHON> python pass expr", "py pass",
                "PYTHON> python fail expr", "py fail",
                "VARIABLE> var pass expr", "var pass",
                "VARIABLE> var fail expr", "var fail",
                "VARIABLE> "));
        assertThat(doc.getAwaitingResultCategoriesStream()).containsOnly(awaitingCategory(1));
        assertThat(doc.getAwaitingResultsPositionsStream()).containsOnly(awaitingPosition(1, 23, 13));
        assertThat(doc.getResultSuccessPositionsStream()).isEmpty();
        assertThat(doc.getResultErrorPositionsStream()).containsOnly(failPosition(60, 6), failPosition(135, 7),
                failPosition(200, 8));

        doc.putEvaluationResult(1, ExpressionType.ROBOT, Optional.of("robot pass"), Optional.empty());
        assertThat(doc.get()).isEqualTo(content(
                "ROBOT> robot pass expr", "PASS: robot pass",
                "ROBOT> robot fail expr", "FAIL: robot fail",
                "PYTHON> python pass expr", "py pass",
                "PYTHON> python fail expr", "py fail",
                "VARIABLE> var pass expr", "var pass",
                "VARIABLE> var fail expr", "var fail",
                "VARIABLE> "));
        assertThat(doc.getAwaitingResultCategoriesStream()).isEmpty();
        assertThat(doc.getAwaitingResultsPositionsStream()).isEmpty();
        assertThat(doc.getResultSuccessPositionsStream()).containsOnly(passPosition(23, 6));
        assertThat(doc.getResultErrorPositionsStream()).containsOnly(failPosition(63, 6), failPosition(138, 7),
                failPosition(203, 8));
    }

    private static CategorizedPosition promptPosition(final int offset, final int lenght) {
        return new CategorizedPosition(ShellDocument.CATEGORY_MODE_PROMPT, offset, lenght);
    }

    private static CategorizedPosition continuationPosition(final int offset, final int lenght) {
        return new CategorizedPosition(ShellDocument.CATEGORY_PROMPT_CONTINUATION, offset, lenght);
    }

    private static String awaitingCategory(final int id) {
        return ShellDocument.CATEGORY_AWAITING_RESULT_PREFIX + "_" + id;
    }

    private static CategorizedPosition awaitingPosition(final int id, final int offset, final int lenght) {
        return new CategorizedPosition(awaitingCategory(id), offset, lenght);
    }

    private static CategorizedPosition passPosition(final int offset, final int lenght) {
        return new CategorizedPosition(ShellDocument.CATEGORY_RESULT_SUCC, offset, lenght);
    }

    private static CategorizedPosition failPosition(final int offset, final int lenght) {
        return new CategorizedPosition(ShellDocument.CATEGORY_RESULT_ERROR, offset, lenght);
    }

    private static String content(final String... lines) {
        return String.join("\n", lines);
    }
}
