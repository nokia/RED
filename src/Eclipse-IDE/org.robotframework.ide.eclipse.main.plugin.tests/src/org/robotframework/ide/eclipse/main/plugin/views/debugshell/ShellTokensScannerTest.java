/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.debugshell;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.List;

import org.junit.Test;
import org.rf.ide.core.execution.server.response.EvaluateExpression.ExpressionType;
import org.rf.ide.core.testdata.text.read.EndOfLineBuilder.EndOfLineTypes;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.Separator.SeparatorType;

@SuppressWarnings("unchecked")
public class ShellTokensScannerTest {

    @Test
    public void testDocumentParsing_inRobotModeWithEvaluationResult() {
        final ShellDocument document = new ShellDocumentSession("\n")
                .type("keyword  arg1  arg2")
                .continueExpr()
                .type("arg3  arg${x}")
                .execute("keyword passed!")
                .get();

        final List<RobotLine> lines = new ShellTokensScanner().getLines(document);
        assertThat(lines).hasSize(4);
        
        final List<IRobotLineElement> elements = lines.stream().flatMap(RobotLine::elementsStream).collect(toList());

        assertNoHolesBetweenTokens(elements, document.getLength());

        assertThat(elements).extracting(IRobotLineElement::getText)
                .containsExactly(
                        "ROBOT> ", "keyword", "  ", "arg1", "  ", "arg2", "\n",
                        "...... ", "arg3", "  ", "arg${x}", "\n",
                        "PASS: ", "keyword passed!", "\n",
                        "ROBOT> ", "");

        assertThat(elements).extracting(IRobotLineElement::getTypes)
                .containsExactly(newArrayList(ShellTokenType.MODE_FLAG), newArrayList(ShellTokenType.CALL_KW),
                        newArrayList(SeparatorType.TABULATOR_OR_DOUBLE_SPACE), newArrayList(ShellTokenType.CALL_ARG),
                        newArrayList(SeparatorType.TABULATOR_OR_DOUBLE_SPACE), newArrayList(ShellTokenType.CALL_ARG),
                        newArrayList(EndOfLineTypes.LF), newArrayList(ShellTokenType.MODE_CONTINUATION),
                        newArrayList(ShellTokenType.CALL_ARG), newArrayList(SeparatorType.TABULATOR_OR_DOUBLE_SPACE),
                        newArrayList(ShellTokenType.CALL_ARG, RobotTokenType.VARIABLE_USAGE),
                        newArrayList(EndOfLineTypes.LF), newArrayList(ShellTokenType.PASS),
                        newArrayList(RobotTokenType.UNKNOWN), newArrayList(EndOfLineTypes.LF),
                        newArrayList(ShellTokenType.MODE_FLAG), newArrayList(EndOfLineTypes.EOF));
    }

    @Test
    public void testDocumentParsing_inVariableModeWithEvaluationResult() {
        final ShellDocument document = new ShellDocumentSession("\n").changeMode(ExpressionType.VARIABLE)
                .type("${variable}")
                .execute("1729")
                .get();

        final List<RobotLine> lines = new ShellTokensScanner().getLines(document);
        assertThat(lines).hasSize(3);

        final List<IRobotLineElement> elements = lines.stream().flatMap(RobotLine::elementsStream).collect(toList());

        assertNoHolesBetweenTokens(elements, document.getLength());

        assertThat(elements).extracting(IRobotLineElement::getText)
                .containsExactly(
                        "VARIABLE> ", "${variable}", "\n",
                        "1729", "\n",
                        "VARIABLE> ", "");

        assertThat(elements).extracting(IRobotLineElement::getTypes)
                .containsExactly(newArrayList(ShellTokenType.MODE_FLAG), newArrayList(RobotTokenType.VARIABLE_USAGE),
                        newArrayList(EndOfLineTypes.LF), newArrayList(RobotTokenType.UNKNOWN),
                        newArrayList(EndOfLineTypes.LF), newArrayList(ShellTokenType.MODE_FLAG),
                        newArrayList(EndOfLineTypes.EOF));
    }

    @Test
    public void testDocumentParsing_inPythonModeWithEvaluationResult() {
        final ShellDocument document = new ShellDocumentSession("\n").changeMode(ExpressionType.PYTHON)
                .type("[for x in range(10)]")
                .execute("[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]")
                .get();

        final List<RobotLine> lines = new ShellTokensScanner().getLines(document);
        assertThat(lines).hasSize(3);
        
        final List<IRobotLineElement> elements = lines.stream().flatMap(RobotLine::elementsStream).collect(toList());

        assertNoHolesBetweenTokens(elements, document.getLength());

        assertThat(elements).extracting(IRobotLineElement::getText)
                .containsExactly(
                        "PYTHON> ", "[for x in range(10)]", "\n",
                        "[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]", "\n",
                        "PYTHON> ", "");

        assertThat(elements).extracting(IRobotLineElement::getTypes)
                .containsExactly(newArrayList(ShellTokenType.MODE_FLAG), newArrayList(RobotTokenType.UNKNOWN),
                        newArrayList(EndOfLineTypes.LF), newArrayList(RobotTokenType.UNKNOWN),
                        newArrayList(EndOfLineTypes.LF), newArrayList(ShellTokenType.MODE_FLAG),
                        newArrayList(EndOfLineTypes.EOF));
    }

    private static void assertNoHolesBetweenTokens(final List<IRobotLineElement> elements, final int totalLenght) {
        int currentOffset = 0;
        for (final IRobotLineElement element : elements) {
            if (element.getStartOffset() != currentOffset) {
                fail("Previous token ended at offset: " + currentOffset + ", but current starts at: "
                        + element.getStartOffset());
            }
            currentOffset = element.getEndOffset();
        }
        if (currentOffset != totalLenght) {
            fail("Last token ended at offset: " + currentOffset + ", the total lenght of text is: " + totalLenght);
        }
    }
}
