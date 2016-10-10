/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

/**
 * @author wypych
 */
public class FileRegionSplitterTest {

    @Test
    public void givenEmptyList_whenCallSplitter_thenShouldReturnUnsetFileRegion() {
        // given
        List<RobotToken> tokens = new ArrayList<>(0);

        // when
        List<FileRegion> splitContinouesRegions = new FileRegion.FileRegionSplitter().splitContinouesRegions(tokens);

        // then
        assertThat(splitContinouesRegions).hasSize(1);
        FileRegion fileRegion = splitContinouesRegions.get(0);
        assertThat(fileRegion.getStart().isSamePlace(FilePosition.createNotSet())).isTrue();
        assertThat(fileRegion.getEnd().isSamePlace(FilePosition.createNotSet())).isTrue();
    }

    @Test
    public void givenListWithNotSetElement_whenCallSplitter_thenShouldReturnUnsetFileRegion() {
        // given
        List<RobotToken> tokens = Arrays.asList(RobotToken.create(""));

        // when
        List<FileRegion> splitContinouesRegions = new FileRegion.FileRegionSplitter().splitContinouesRegions(tokens);

        // then
        assertThat(splitContinouesRegions).hasSize(1);
        FileRegion fileRegion = splitContinouesRegions.get(0);
        assertThat(fileRegion.getStart().isSamePlace(FilePosition.createNotSet())).isTrue();
        assertThat(fileRegion.getEnd().isSamePlace(FilePosition.createNotSet())).isTrue();
    }

    @Test
    public void givenListWithElementWithPosition_whenCallSplitter_thenShouldReturn_positionOfThisToken() {
        // given
        final RobotToken token = RobotToken.create("foobar");
        token.setStartOffset(10);
        token.setLineNumber(11);
        token.setStartColumn(12);
        List<RobotToken> tokens = Arrays.asList(token);

        // when
        List<FileRegion> splitContinouesRegions = new FileRegion.FileRegionSplitter().splitContinouesRegions(tokens);

        // then
        assertThat(splitContinouesRegions).hasSize(1);
        FileRegion fileRegion = splitContinouesRegions.get(0);
        assertThat(fileRegion.getStart().isSamePlace(token.getFilePosition())).isTrue();
        assertThat(fileRegion.getEnd()
                .isSamePlace(new FilePosition(token.getLineNumber(), token.getEndColumn(), token.getEndOffset())))
                        .isTrue();
    }

    @Test
    public void givenListWithThreeElementsWithPositionInSameLine_whenCallSplitter_thenShouldReturn_positionOfFirst_andLastToken() {
        // given
        final RobotToken tokenOne = RobotToken.create("foobar");
        tokenOne.setStartOffset(10);
        tokenOne.setLineNumber(11);
        tokenOne.setStartColumn(12);

        final RobotToken tokenTwo = RobotToken.create("foobar2");
        tokenTwo.setStartOffset(tokenOne.getEndOffset() + 1);
        tokenTwo.setLineNumber(tokenOne.getLineNumber());
        tokenTwo.setStartColumn(tokenOne.getEndColumn() + 1);

        final RobotToken tokenThree = RobotToken.create("foobar3");
        tokenThree.setStartOffset(tokenTwo.getEndOffset() + 1);
        tokenThree.setLineNumber(tokenTwo.getLineNumber());
        tokenThree.setStartColumn(tokenTwo.getEndColumn() + 1);

        List<RobotToken> tokens = Arrays.asList(tokenThree, tokenTwo, tokenOne);

        // when
        List<FileRegion> splitContinouesRegions = new FileRegion.FileRegionSplitter().splitContinouesRegions(tokens);

        // then
        assertThat(splitContinouesRegions).hasSize(1);
        FileRegion fileRegion = splitContinouesRegions.get(0);
        assertThat(fileRegion.getStart().isSamePlace(tokenOne.getFilePosition())).isTrue();
        assertThat(fileRegion.getEnd().isSamePlace(
                new FilePosition(tokenThree.getLineNumber(), tokenThree.getEndColumn(), tokenThree.getEndOffset())))
                        .isTrue();
    }

    @Test
    public void givenListWithThreeElementsWithPositions_andOneOnDifferentLine_whenCallSplitter_thenShouldReturn_threeRegions() {
        // given
        final RobotToken tokenOne = RobotToken.create("foobar");
        tokenOne.setStartOffset(10);
        tokenOne.setLineNumber(11);
        tokenOne.setStartColumn(12);

        final RobotToken tokenTwo = RobotToken.create("foobar2");
        tokenTwo.setStartOffset(tokenOne.getEndOffset() + 1);
        tokenTwo.setLineNumber(tokenOne.getLineNumber() + 1);
        tokenTwo.setStartColumn(tokenOne.getEndColumn() + 1);

        final RobotToken tokenThree = RobotToken.create("foobar3");
        tokenThree.setStartOffset(tokenTwo.getEndOffset() + 1);
        tokenThree.setLineNumber(tokenTwo.getLineNumber() + 3);
        tokenThree.setStartColumn(tokenTwo.getEndColumn() + 1);

        List<RobotToken> tokens = Arrays.asList(tokenThree, tokenTwo, tokenOne);

        // when
        List<FileRegion> splitContinouesRegions = new FileRegion.FileRegionSplitter().splitContinouesRegions(tokens);

        // then
        assertThat(splitContinouesRegions).hasSize(2);
        FileRegion fileRegionOne = splitContinouesRegions.get(0);
        assertThat(fileRegionOne.getStart().isSamePlace(tokenOne.getFilePosition())).isTrue();
        assertThat(fileRegionOne.getEnd().isSamePlace(
                new FilePosition(tokenTwo.getLineNumber(), tokenTwo.getEndColumn(), tokenTwo.getEndOffset()))).isTrue();

        FileRegion fileRegionTwo = splitContinouesRegions.get(1);
        assertThat(fileRegionTwo.getStart().isSamePlace(tokenThree.getFilePosition())).isTrue();
        assertThat(fileRegionTwo.getEnd().isSamePlace(
                new FilePosition(tokenThree.getLineNumber(), tokenThree.getEndColumn(), tokenThree.getEndOffset())))
                        .isTrue();
    }
}
