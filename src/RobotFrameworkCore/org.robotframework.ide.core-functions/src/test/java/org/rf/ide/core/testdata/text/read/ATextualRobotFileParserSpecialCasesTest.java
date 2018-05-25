/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.rf.ide.core.testdata.IRobotFileParser;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotFileOutput.Status;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.text.read.EndOfLineBuilder.EndOfLineTypes;
import org.rf.ide.core.testdata.text.read.LineReader.Constant;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class ATextualRobotFileParserSpecialCasesTest {

    @Test
    public void test_forByteOrderMark_relatedIssue() {
        // prepare
        final String text = "\uFEFF*** Test Cases ***";
        final InputStream inputStream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        final TxtRobotFileParser parser = new TxtRobotFileParser();
        final File robotFile = new File("OK.txt");
        final RobotFileOutput output = new RobotFileOutput(RobotVersion.from("1.0.0"));

        // execute
        parser.parse(output, inputStream, robotFile);

        // verify
        assertThat(output.getStatus()).isEqualTo(Status.PASSED);
        final List<RobotLine> fileContent = output.getFileModel().getFileContent();
        assertThat(fileContent).hasSize(1);
        final RobotLine robotLine = fileContent.get(0);
        final List<IRobotLineElement> lineElements = robotLine.getLineElements();
        assertThat(lineElements).hasSize(1);
        final IRobotLineElement testCaseHeader = lineElements.get(0);
        assertThat(testCaseHeader.getFilePosition().isSamePlace(new FilePosition(1, 0, 1))).isTrue();
        assertThat(testCaseHeader.getStartColumn()).isEqualTo(0);
        assertThat(testCaseHeader.getText()).isEqualTo("*** Test Cases ***");
        assertThat(testCaseHeader.getTypes()).containsExactly(RobotTokenType.TEST_CASES_TABLE_HEADER);
    }

    @Test
    public void test_handleCRLFcaseSplittedBetweenBuffers_CR_LF_splittedBetweenBuffers() {
        // prepare
        final int currentOffset = 0;
        final LineReader lineHolder = mock(LineReader.class);
        when(lineHolder.getLineEnd(currentOffset)).thenReturn(Arrays.asList(Constant.CR, Constant.LF));
        final RobotFileOutput parsingOutput = new RobotFileOutput(RobotVersion.from("1.0.0"));

        final RobotFile fileModel = parsingOutput.getFileModel();
        final RobotLine line = new RobotLine(2, fileModel);
        line.setEndOfLine(Arrays.asList(Constant.CR), currentOffset, 0);
        assertThat(line.getEndOfLine().getTypes()).containsExactly(EndOfLineTypes.CR);

        fileModel.addNewLine(null);
        fileModel.addNewLine(line);

        // execute
        final ATextualRobotFileParser fileParser = new DummyATextualRobotFileParser();

        final int newOffset = fileParser.handleCRLFcaseSplittedBetweenBuffers(parsingOutput, lineHolder, 3,
                currentOffset + 1);

        // verify
        assertThat(newOffset).isEqualTo(2);
        assertThat(line.getEndOfLine().getStartOffset()).isEqualTo(0);
        assertThat(line.getEndOfLine().getTypes()).containsExactly(EndOfLineTypes.CRLF);
    }

    private static class DummyATextualRobotFileParser extends ATextualRobotFileParser {

        public DummyATextualRobotFileParser() {
            super(null);
        }

        @Override
        public boolean canParseFile(final File file, final boolean isFromStringContent) {
            return false;
        }

        @Override
        public IRobotFileParser newInstance() {
            return new DummyATextualRobotFileParser();
        }

        @Override
        public boolean isPrettyAlignLineOnly(final String currentLineText) {
            return false;
        }
    }
}
