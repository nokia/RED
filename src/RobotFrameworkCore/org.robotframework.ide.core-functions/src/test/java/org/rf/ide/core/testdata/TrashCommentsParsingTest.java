/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.testdata.RobotParser.RobotParserConfig;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotFileOutput.RobotFileType;
import org.rf.ide.core.testdata.model.RobotFileOutput.Status;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.Separator;

/**
 * @author wypych
 */
public class TrashCommentsParsingTest {

    @Test
    public void test_givenMultipleRobotExecutableLines_withCommentsJoinedByPreviouseLineContinue_shouldGives_4RobotExecutableLines()
            throws Exception {
        // prepare
        final RobotRuntimeEnvironment runtime = mock(RobotRuntimeEnvironment.class);
        when(runtime.getVersion()).thenReturn("2.9");
        final RobotProjectHolder projectHolder = mock(RobotProjectHolder.class);
        when(projectHolder.getRobotRuntime()).thenReturn(runtime);
        final String mainPath = "parser/bugs/";
        final File file = new File(this.getClass().getResource(mainPath + "TrashCommentsAtTheBeginning.robot").toURI());
        when(projectHolder.shouldBeLoaded(file)).thenReturn(true);

        // execute
        final RobotParser parser = RobotParser.create(projectHolder, RobotParserConfig.allImportsLazy());
        final List<RobotFileOutput> parsed = parser.parse(file);

        // verify
        assertThat(parsed).hasSize(1);
        final RobotFileOutput robotFileOutput = parsed.get(0);
        assertThat(robotFileOutput.getStatus()).isEqualTo(Status.PASSED);
        assertThat(robotFileOutput.getType()).isEqualTo(RobotFileType.UNKNOWN);
        final RobotFile fileModel = robotFileOutput.getFileModel();

        assertThat(fileModel.getSettingTable().isPresent()).isFalse();
        assertThat(fileModel.getVariableTable().isPresent()).isFalse();
        assertThat(fileModel.getTestCaseTable().isPresent()).isFalse();
        assertThat(fileModel.getKeywordTable().isPresent()).isFalse();

        final List<RobotLine> fileContent = fileModel.getFileContent();
        assertThat(fileContent).hasSize(6);
        assertLine(fileContent.get(0),
                RobotToken.create("# comment", Arrays.asList(RobotTokenType.START_HASH_COMMENT)));
        assertLine(fileContent.get(1), RobotToken.create("#", Arrays.asList(RobotTokenType.START_HASH_COMMENT)),
                separator("\t"), RobotToken.create("*** Settings ***",
                        Arrays.asList(RobotTokenType.COMMENT_CONTINUE, RobotTokenType.SETTINGS_TABLE_HEADER)));
        assertLine(fileContent.get(2), RobotToken.create("#d", Arrays.asList(RobotTokenType.START_HASH_COMMENT)),
                separator("\t"),
                RobotToken.create("ok", Arrays.asList(RobotTokenType.COMMENT_CONTINUE, RobotTokenType.UNKNOWN)));
        assertLine(fileContent.get(3),
                RobotToken.create("*** unknown header ***", Arrays.asList(RobotTokenType.USER_OWN_TABLE_HEADER)));
        assertLine(fileContent.get(4), RobotToken.create("d", Arrays.asList(RobotTokenType.UNKNOWN)), separator("\t"),
                RobotToken.create("#start", Arrays.asList(RobotTokenType.START_HASH_COMMENT)), separator("\t"),
                RobotToken.create("continue", Arrays.asList(RobotTokenType.COMMENT_CONTINUE, RobotTokenType.UNKNOWN)));
    }

    private void assertLine(final RobotLine toTest, final IRobotLineElement... toks) {
        final List<IRobotLineElement> lineElements = toTest.getLineElements();
        final int size = toks.length;
        assertThat(lineElements).hasSize(size);
        for (int i = 0; i < size; i++) {
            final IRobotLineElement elem = lineElements.get(i);
            final IRobotLineElement expToken = toks[i];
            assertThat(expToken).hasSameClassAs(elem);
            assertThat(expToken.getText()).isEqualTo(elem.getText());
            assertThat(expToken.getTypes()).containsExactlyElementsOf(elem.getTypes());
        }
    }

    private static Separator separator(final String text) {
        return Separator.matchSeparator(text);
    }
}
