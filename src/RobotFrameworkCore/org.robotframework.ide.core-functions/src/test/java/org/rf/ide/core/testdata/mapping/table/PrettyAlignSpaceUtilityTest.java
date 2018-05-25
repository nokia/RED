/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.table;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

/**
 * @author wypych
 */
public class PrettyAlignSpaceUtilityTest {

    private PrettyAlignSpaceUtility prettyAlignExtractor;

    @Test
    public void test_method_extractPrettyAlignWhitespaces_givenTokenWithPrettyAlign_SuffixAndPreffix_whenShouldDoExtraction_thenCheckIfTextWithoutSpaces() {
        // prepare
        final RobotLine line = new RobotLine(1, null);
        final String rawAndText = "   data   ";
        final RobotToken token = RobotToken.create(rawAndText);
        final FilePosition fp = new FilePosition(10, 10, 20);
        token.setStartOffset(fp.getOffset());
        token.setLineNumber(fp.getLine());
        token.setStartColumn(fp.getColumn());
        token.setType(RobotTokenType.UNKNOWN);
        line.addLineElement(token);

        // execute
        prettyAlignExtractor.extractPrettyAlignWhitespaces(line, token, rawAndText);

        // verify
        assertThat(line.getLineElements()).hasSize(3);
        assertToken(line.getLineElements().get(0), "   ", RobotTokenType.PRETTY_ALIGN_SPACE,
                new FilePosition(10, 10, 20));
        assertToken(line.getLineElements().get(1), "data", RobotTokenType.UNKNOWN, new FilePosition(10, 13, 23));
        assertThat(line.getLineElements().get(1)).isSameAs(token);
        assertToken(line.getLineElements().get(2), "   ", RobotTokenType.PRETTY_ALIGN_SPACE,
                new FilePosition(10, 17, 27));
    }

    @Test
    public void test_method_extractPrettyAlignWhitespaces_givenTokenWithPrettyAlignPreffix_whenShouldDoExtraction_thenCheckIfTextWithoutSpaces() {
        // prepare
        final RobotLine line = new RobotLine(1, null);
        final String rawAndText = "data   ";
        final RobotToken token = RobotToken.create(rawAndText);
        final FilePosition fp = new FilePosition(10, 10, 20);
        token.setStartOffset(fp.getOffset());
        token.setLineNumber(fp.getLine());
        token.setStartColumn(fp.getColumn());
        token.setType(RobotTokenType.UNKNOWN);
        line.addLineElement(token);

        // execute
        prettyAlignExtractor.extractPrettyAlignWhitespaces(line, token, rawAndText);

        // verify
        assertThat(line.getLineElements()).hasSize(2);
        assertToken(line.getLineElements().get(0), "data", RobotTokenType.UNKNOWN, new FilePosition(10, 10, 20));
        assertThat(line.getLineElements().get(0)).isSameAs(token);
        assertToken(line.getLineElements().get(1), "   ", RobotTokenType.PRETTY_ALIGN_SPACE,
                new FilePosition(10, 14, 24));
    }

    @Test
    public void test_method_extractPrettyAlignWhitespaces_givenTokenWithPrettyAlignSuffix_whenShouldDoExtraction_thenCheckIfTextWithoutSpaces() {
        // prepare
        final RobotLine line = new RobotLine(1, null);
        final String rawAndText = "   data";
        final RobotToken token = RobotToken.create(rawAndText);
        final FilePosition fp = new FilePosition(10, 10, 20);
        token.setStartOffset(fp.getOffset());
        token.setLineNumber(fp.getLine());
        token.setStartColumn(fp.getColumn());
        token.setType(RobotTokenType.UNKNOWN);
        line.addLineElement(token);

        // execute
        prettyAlignExtractor.extractPrettyAlignWhitespaces(line, token, rawAndText);

        // verify
        assertThat(line.getLineElements()).hasSize(2);
        assertToken(line.getLineElements().get(0), "   ", RobotTokenType.PRETTY_ALIGN_SPACE,
                new FilePosition(10, 10, 20));
        assertToken(line.getLineElements().get(1), "data", RobotTokenType.UNKNOWN, new FilePosition(10, 13, 23));
        assertThat(line.getLineElements().get(1)).isSameAs(token);
    }

    @Test
    public void test_method_extractPrettyAlignWhitespaces_givenPrettyAlignToken_whenShouldNotDoAnything_thenCheckIfTextIsReturnAsItWas() {
        // prepare
        final RobotLine line = new RobotLine(1, null);
        final String rawAndText = " ";
        final RobotToken token = RobotToken.create(rawAndText);
        final FilePosition fp = new FilePosition(10, 10, 10);
        token.setStartOffset(fp.getOffset());
        token.setLineNumber(fp.getLine());
        token.setStartColumn(fp.getColumn());
        token.setType(RobotTokenType.PRETTY_ALIGN_SPACE);
        line.addLineElement(token);

        // execute
        prettyAlignExtractor.extractPrettyAlignWhitespaces(line, token, rawAndText);

        // verify
        assertThat(line.getLineElements()).hasSize(1);
        assertThat(line.getLineElements()).containsExactly(token);
        assertToken(token, rawAndText, RobotTokenType.PRETTY_ALIGN_SPACE, fp);
    }

    private void assertToken(final IRobotLineElement token, final String text, final RobotTokenType type,
            final FilePosition fp) {
        assertThat(token.getText()).isEqualTo(text);
        assertThat(token.getTypes().get(0)).isEqualTo(type);

        assertThat(token.getFilePosition().isSamePlace(fp)).as("got position %s", token.getFilePosition()).isTrue();
    }

    @Before
    public void setUp() {
        this.prettyAlignExtractor = new PrettyAlignSpaceUtility();
    }
}
