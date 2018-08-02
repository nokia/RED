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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.rf.ide.core.testdata.RobotParser.RobotParserConfig;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotFileOutput.RobotFileType;
import org.rf.ide.core.testdata.model.RobotFileOutput.Status;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

/**
 * @author wypych
 */
public class RobotParserSimpleExecutableContinueTest {

    @Test
    public void givenTwoKeywords_oneWithNormalName_andSecondWithTripleDotsName_shouldReturnTwoKeywords()
            throws Exception {
        // prepare
        final RobotProjectHolder projectHolder = mock(RobotProjectHolder.class);
        final String mainPath = "parser/bugs/";
        final File file = new File(this.getClass().getResource(mainPath + "KeywordNameAsTripleDots.robot").toURI());
        when(projectHolder.shouldBeLoaded(file)).thenReturn(true);

        // execute
        final RobotParser parser = RobotParser.create(projectHolder,
                RobotParserConfig.allImportsLazy(new RobotVersion(2, 9)));
        final List<RobotFileOutput> parsed = parser.parse(file);

        // verify
        assertThat(parsed).hasSize(1);
        final RobotFileOutput robotFileOutput = parsed.get(0);
        assertThat(robotFileOutput.getStatus()).isEqualTo(Status.PASSED);
        assertThat(robotFileOutput.getType()).isEqualTo(RobotFileType.RESOURCE);
        final RobotFile fileModel = robotFileOutput.getFileModel();

        assertThatOnlyKeywordTableIsIncluded(fileModel);
        final KeywordTable keywordTable = fileModel.getKeywordTable();
        final List<UserKeyword> keywords = keywordTable.getKeywords();
        assertThat(keywords).hasSize(2);
        assertThat(keywords.get(0).getName().getText()).isEqualTo("Key1");
        assertThat(keywords.get(1).getName().getText()).isEqualTo("...");
    }

    @Test
    public void test_givenMultipleRobotExecutableLines_withCommentsJoinedByPreviouseLineContinue_shouldGives_4RobotExecutableLines()
            throws Exception {
        // prepare
        final RobotProjectHolder projectHolder = mock(RobotProjectHolder.class);
        final String mainPath = "parser/bugs/";
        final File file = new File(this.getClass()
                .getResource(mainPath + "KeywordsExecWithHashCommentAndPreviousLineContinue.robot")
                .toURI());
        when(projectHolder.shouldBeLoaded(file)).thenReturn(true);

        // execute
        final RobotParser parser = RobotParser.create(projectHolder,
                RobotParserConfig.allImportsLazy(new RobotVersion(2, 9)));
        final List<RobotFileOutput> parsed = parser.parse(file);

        // verify
        assertThat(parsed).hasSize(1);
        final RobotFileOutput robotFileOutput = parsed.get(0);
        assertThat(robotFileOutput.getStatus()).isEqualTo(Status.PASSED);
        assertThat(robotFileOutput.getType()).isEqualTo(RobotFileType.RESOURCE);
        final RobotFile fileModel = robotFileOutput.getFileModel();

        assertThatOnlyKeywordTableIsIncluded(fileModel);
        final KeywordTable keywordTable = fileModel.getKeywordTable();
        assertThat(keywordTable.getKeywords()).hasSize(1);
        final UserKeyword userKeyword = keywordTable.getKeywords().get(0);
        assertThat(userKeyword.getExecutionContext()).hasSize(4);
        final List<RobotExecutableRow<UserKeyword>> executionContext = userKeyword.getExecutionContext();
        assertExecutableLine(executionContext.get(0), "Log", Arrays.asList("parameter1"), new ArrayList<String>(0));
        assertExecutableLine(executionContext.get(1), "Log X", Arrays.asList("contArg1", "pnotable"),
                Arrays.asList("#a", "data1", "#b", "data2"));
        assertExecutableLine(executionContext.get(2), null, new ArrayList<String>(0), Arrays.asList("#a", "poor"));
        assertExecutableLine(executionContext.get(3), "Log", Arrays.asList("me"), new ArrayList<String>(0));
    }

    private void assertExecutableLine(final RobotExecutableRow<UserKeyword> robotExecutableRow, final String actionText,
            final List<String> args, final List<String> comment) {
        if (actionText == null) {
            assertToken(robotExecutableRow.getAction(), "", RobotTokenType.KEYWORD_ACTION_NAME);
        } else {
            assertToken(robotExecutableRow.getAction(), actionText, RobotTokenType.KEYWORD_ACTION_NAME);
        }
        final List<RobotToken> arguments = robotExecutableRow.getArguments();
        final int argsSize = args.size();
        assertThat(arguments).hasSize(argsSize);
        for (int i = 0; i < argsSize; i++) {
            assertToken(arguments.get(i), args.get(i), RobotTokenType.KEYWORD_ACTION_ARGUMENT);
        }

        final List<RobotToken> comments = robotExecutableRow.getComment();
        final int commentsSize = comment.size();
        assertThat(comments).hasSize(commentsSize);
        for (int i = 0; i < commentsSize; i++) {
            final RobotToken token = comments.get(i);
            assertThat(token.getText()).isEqualTo(comment.get(i));
            assertThat(token.getTypes().contains(RobotTokenType.START_HASH_COMMENT)
                    || token.getTypes().contains(RobotTokenType.COMMENT_CONTINUE)).isTrue();
        }
    }

    private void assertToken(final RobotToken token, final String text, final RobotTokenType... types) {
        assertThat(token.getText()).isEqualTo(text);
        assertThat(token.getTypes()).containsAll(new ArrayList<>(Arrays.asList(types)));
    }

    private void assertThatOnlyKeywordTableIsIncluded(final RobotFile fileModel) {
        assertThat(fileModel.getSettingTable().isPresent()).isFalse();
        assertThat(fileModel.getVariableTable().isPresent()).isFalse();
        assertThat(fileModel.getTestCaseTable().isPresent()).isFalse();
        assertThat(fileModel.getKeywordTable().isPresent()).isTrue();
    }
}
