/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.variables;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.Separator;

public class CommonVariableHelperTest {

    private final CommonVariableHelper helper = new CommonVariableHelper();

    @Test
    public void testIncorrectVariables() throws Exception {
        assertIncorrectVariable("#abc", RobotTokenType.START_HASH_COMMENT);
        assertIncorrectVariable("#abc", RobotTokenType.COMMENT_CONTINUE);

        assertIncorrectVariable(null, RobotTokenType.VARIABLES_SCALAR_DECLARATION);
        assertIncorrectVariable("", RobotTokenType.VARIABLES_SCALAR_DECLARATION);
        assertIncorrectVariable("@{var}", RobotTokenType.VARIABLES_SCALAR_DECLARATION);
        assertIncorrectVariable(null, RobotTokenType.VARIABLES_LIST_DECLARATION);
        assertIncorrectVariable("", RobotTokenType.VARIABLES_LIST_DECLARATION);
        assertIncorrectVariable("${var}", RobotTokenType.VARIABLES_LIST_DECLARATION);
        assertIncorrectVariable(null, RobotTokenType.VARIABLES_DICTIONARY_DECLARATION);
        assertIncorrectVariable("", RobotTokenType.VARIABLES_DICTIONARY_DECLARATION);
        assertIncorrectVariable("${var}", RobotTokenType.VARIABLES_DICTIONARY_DECLARATION);

        assertIncorrectVariable(null, RobotTokenType.VARIABLE_USAGE);
        assertIncorrectVariable("", RobotTokenType.VARIABLE_USAGE);
        assertIncorrectVariable("invalid", RobotTokenType.VARIABLE_USAGE);

        assertIncorrectVariable("text", RobotTokenType.UNKNOWN);
    }

    @Test
    public void testCorrectVariables() throws Exception {
        assertCorrectVariable("${var}", RobotTokenType.VARIABLES_SCALAR_DECLARATION);
        assertCorrectVariable("@{var}", RobotTokenType.VARIABLES_LIST_DECLARATION);
        assertCorrectVariable("&{var}", RobotTokenType.VARIABLES_DICTIONARY_DECLARATION);
        assertCorrectVariable("${var}", RobotTokenType.VARIABLE_USAGE);
        assertCorrectVariable("@{var}", RobotTokenType.VARIABLE_USAGE);
        assertCorrectVariable("&{var}", RobotTokenType.VARIABLE_USAGE);
    }

    @Test
    public void testNameExtraction() throws Exception {
        assertThat(helper.extractVariableName("")).isEmpty();
        assertThat(helper.extractVariableName("unknown")).isEmpty();
        assertThat(helper.extractVariableName("{incorrect")).isEmpty();

        assertThat(helper.extractVariableName("${scalar}")).isEqualTo("scalar");
        assertThat(helper.extractVariableName("@{list}")).isEqualTo("list");
        assertThat(helper.extractVariableName("&{dict}")).isEqualTo("dict");

        assertThat(helper.extractVariableName("${  v   a  r\t}")).isEqualTo(" v a r ");
    }

    @Test
    public void testBracketsConditions() throws Exception {
        assertThat(helper.matchesBracketsConditionsForCorrectVariable("")).isFalse();
        assertThat(helper.matchesBracketsConditionsForCorrectVariable("${var")).isFalse();
        assertThat(helper.matchesBracketsConditionsForCorrectVariable("$var}")).isFalse();
        assertThat(helper.matchesBracketsConditionsForCorrectVariable("${va}r")).isFalse();

        assertThat(helper.matchesBracketsConditionsForCorrectVariable("${var}")).isTrue();
        assertThat(helper.matchesBracketsConditionsForCorrectVariable(" ${var} ")).isTrue();
        assertThat(helper.matchesBracketsConditionsForCorrectVariable("${var}=")).isTrue();
        assertThat(helper.matchesBracketsConditionsForCorrectVariable("${var} =")).isTrue();
        assertThat(helper.matchesBracketsConditionsForCorrectVariable("${var} = ")).isTrue();
    }

    @Test
    public void assignmentPartIsNotMarked_forNonVariableDeclaration() throws Exception {
        final List<IRobotLineElement> elements = newArrayList(
                RobotToken.create("abc", new FilePosition(1, 2, 2), RobotTokenType.KEYWORD_ACTION_NAME),
                new Separator(),
                RobotToken.create("def", new FilePosition(1, 7, 7), RobotTokenType.KEYWORD_ACTION_ARGUMENT));

        final RobotLine line = createLine(elements);

        helper.markVariableAssignmentPart(line);

        assertThat(elements.get(0).getTypes()).doesNotContain(RobotTokenType.ASSIGNMENT);
    }

    @Test
    public void assignmentPartIsNotMarked_forIncorrectListDeclaration() throws Exception {
        final List<IRobotLineElement> elements = newArrayList(
                RobotToken.create("@{var}[0]= ", new FilePosition(1, 0, 0), RobotTokenType.VARIABLES_LIST_DECLARATION),
                new Separator(),
                RobotToken.create("kw", new FilePosition(1, 14, 14), RobotTokenType.KEYWORD_ACTION_ARGUMENT));

        final RobotLine line = createLine(elements);

        helper.markVariableAssignmentPart(line);

        assertThat(elements.get(0).getText().trim()).endsWith("=");
        assertThat(elements.get(0).getTypes()).doesNotContain(RobotTokenType.ASSIGNMENT);
    }

    @Test
    public void assignmentPartIsNotMarked_forIncorrectDictionaryDeclaration() throws Exception {
        final List<IRobotLineElement> elements = newArrayList(
                RobotToken.create("&{var}['key']= ", new FilePosition(1, 0, 0),
                        RobotTokenType.VARIABLES_LIST_DECLARATION),
                new Separator(),
                RobotToken.create("kw", new FilePosition(1, 18, 18), RobotTokenType.KEYWORD_ACTION_ARGUMENT));

        final RobotLine line = createLine(elements);

        helper.markVariableAssignmentPart(line);

        assertThat(elements.get(0).getText().trim()).endsWith("=");
        assertThat(elements.get(0).getTypes()).doesNotContain(RobotTokenType.ASSIGNMENT);
    }

    @Test
    public void assignmentPartIsNotMarked_forScalarVariableDeclaration() throws Exception {
        final List<IRobotLineElement> elements = newArrayList(
                RobotToken.create("${var}", new FilePosition(1, 0, 0), RobotTokenType.VARIABLES_SCALAR_DECLARATION),
                new Separator(),
                RobotToken.create("kw", new FilePosition(1, 9, 9), RobotTokenType.KEYWORD_ACTION_ARGUMENT));

        final RobotLine line = createLine(elements);

        helper.markVariableAssignmentPart(line);

        assertThat(elements.get(0).getTypes()).doesNotContain(RobotTokenType.ASSIGNMENT);
    }

    @Test
    public void assignmentPartIsMarked_forScalarVariableDeclarationWithEqualSign() throws Exception {
        final List<IRobotLineElement> elements = newArrayList(
                RobotToken.create("${var}= ", new FilePosition(1, 0, 0), RobotTokenType.VARIABLES_SCALAR_DECLARATION),
                new Separator(),
                RobotToken.create("kw", new FilePosition(1, 11, 11), RobotTokenType.KEYWORD_ACTION_ARGUMENT));

        final RobotLine line = createLine(elements);

        helper.markVariableAssignmentPart(line);

        assertThat(elements.get(0).getText().trim()).endsWith("=");
        assertThat(elements.get(0).getTypes()).contains(RobotTokenType.ASSIGNMENT);
    }

    @Test
    public void assignmentPartIsNotMarked_forListVariableDeclaration() throws Exception {
        final List<IRobotLineElement> elements = newArrayList(
                RobotToken.create("@{var}", new FilePosition(1, 0, 0), RobotTokenType.VARIABLES_SCALAR_DECLARATION),
                new Separator(),
                RobotToken.create("kw", new FilePosition(1, 9, 9), RobotTokenType.KEYWORD_ACTION_ARGUMENT));

        final RobotLine line = createLine(elements);

        helper.markVariableAssignmentPart(line);

        assertThat(elements.get(0).getTypes()).doesNotContain(RobotTokenType.ASSIGNMENT);
    }

    @Test
    public void assignmentPartIsMarked_forListVariableDeclarationWithEqualSign() throws Exception {
        final List<IRobotLineElement> elements = newArrayList(
                RobotToken.create("@{var} =", new FilePosition(1, 0, 0), RobotTokenType.VARIABLES_LIST_DECLARATION),
                new Separator(),
                RobotToken.create("kw", new FilePosition(1, 11, 11), RobotTokenType.KEYWORD_ACTION_ARGUMENT));

        final RobotLine line = createLine(elements);

        helper.markVariableAssignmentPart(line);

        assertThat(elements.get(0).getText()).isEqualTo("@{var} =");
        assertThat(elements.get(0).getTypes()).contains(RobotTokenType.ASSIGNMENT);
    }

    @Test
    public void assignmentPartIsNotMarked_forDictionaryVariableDeclaration() throws Exception {
        final List<IRobotLineElement> elements = newArrayList(
                RobotToken.create("&{var}", new FilePosition(1, 0, 0), RobotTokenType.VARIABLES_SCALAR_DECLARATION),
                new Separator(),
                RobotToken.create("kw", new FilePosition(1, 9, 9), RobotTokenType.KEYWORD_ACTION_ARGUMENT));

        final RobotLine line = createLine(elements);

        helper.markVariableAssignmentPart(line);

        assertThat(elements.get(0).getTypes()).doesNotContain(RobotTokenType.ASSIGNMENT);
    }

    @Test
    public void assignmentPartIsMarked_forDictionaryVariableDeclarationWithEqualSign() throws Exception {
        final List<IRobotLineElement> elements = newArrayList(
                RobotToken.create("&{var}=", new FilePosition(1, 0, 0),
                        RobotTokenType.VARIABLES_DICTIONARY_DECLARATION),
                new Separator(),
                RobotToken.create("kw", new FilePosition(1, 0, 0), RobotTokenType.KEYWORD_ACTION_ARGUMENT));

        final RobotLine line = createLine(elements);

        helper.markVariableAssignmentPart(line);

        assertThat(elements.get(0).getText().trim()).endsWith("=");
        assertThat(elements.get(0).getTypes()).contains(RobotTokenType.ASSIGNMENT);
    }

    private void assertIncorrectVariable(final String text, final RobotTokenType... types) {
        assertThat(helper.isVariable(RobotToken.create(text, types))).isFalse();
    }

    private void assertCorrectVariable(final String text, final RobotTokenType... types) {
        assertThat(helper.isVariable(RobotToken.create(text, types))).isTrue();
    }

    private RobotLine createLine(final List<IRobotLineElement> elements) {
        final RobotLine line = new RobotLine(1, null);
        for (final IRobotLineElement element : elements) {
            line.addLineElement(element);
        }
        return line;
    }
}
