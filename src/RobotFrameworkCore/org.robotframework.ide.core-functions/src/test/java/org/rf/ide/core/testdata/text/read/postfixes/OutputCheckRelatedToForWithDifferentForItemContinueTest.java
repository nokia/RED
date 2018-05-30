/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.postfixes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.testdata.RobotParser;
import org.rf.ide.core.testdata.RobotParser.RobotParserConfig;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotFileOutput.RobotFileType;
import org.rf.ide.core.testdata.model.RobotFileOutput.Status;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor.ERowType;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.ForLoopContinueRowDescriptor;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

/**
 * @author wypych
 */
public class OutputCheckRelatedToForWithDifferentForItemContinueTest {

    @Test
    public void test_normalFor_withoutAnyCommentAndPreviousLineContinue_forRobotFormat() throws Exception {
        // execute
        final RobotFile modelFile = getModelFile("ForWithoutAnyCommentConnectedToPreviousLineContinue.robot");

        // verify
        assertThatOnlyKeywordTableIsPresent(modelFile);
        final KeywordTable keywordTable = modelFile.getKeywordTable();
        final List<UserKeyword> keywords = keywordTable.getKeywords();
        assertThat(keywords).hasSize(1);
        final UserKeyword userKeyword = keywords.get(0);
        final List<RobotExecutableRow<UserKeyword>> executionContext = userKeyword.getExecutionContext();
        assertThat(executionContext).hasSize(4);
        assertExecutableLine(executionContext.get(0), ":FOR", Arrays.asList("${x}", "IN", "a", "b", "c"),
                new ArrayList<String>(0));
        assertThat(executionContext.get(0).buildLineDescription().getRowType()).isEqualTo(ERowType.FOR);
        assertExecutableLine(executionContext.get(1), "\\", Arrays.asList("Log", "ok"), Arrays.asList("#cm1"));
        assertThat(executionContext.get(1).buildLineDescription().getRowType()).isEqualTo(ERowType.FOR_CONTINUE);
        assertExecutableLine(executionContext.get(2), "\\", Arrays.asList("Log", "ok2"), Arrays.asList("#cm2"));
        assertThat(executionContext.get(2).buildLineDescription().getRowType()).isEqualTo(ERowType.FOR_CONTINUE);
        assertExecutableLine(executionContext.get(3), "Log", Arrays.asList("done"), new ArrayList<String>(0));
        assertThat(executionContext.get(3).buildLineDescription().getRowType()).isEqualTo(ERowType.SIMPLE);
    }

    @Test
    public void test_normalFor_withoutAnyCommentAndPreviousLineContinue_forTsvFormat_withPythonLikeSyntax()
            throws Exception {
        final RobotFile modelFile = getModelFile(
                "ForWithoutAnyCommentConnectedToPreviousLineContinue_PythonLikeForItem.tsv");

        // verify
        assertThatOnlyKeywordTableIsPresent(modelFile);
        final KeywordTable keywordTable = modelFile.getKeywordTable();
        final List<UserKeyword> keywords = keywordTable.getKeywords();
        assertThat(keywords).hasSize(1);
        final UserKeyword userKeyword = keywords.get(0);
        final List<RobotExecutableRow<UserKeyword>> executionContext = userKeyword.getExecutionContext();
        assertThat(executionContext).hasSize(4);
        assertExecutableLine(executionContext.get(0), ":FOR", Arrays.asList("${x}", "IN", "a", "b", "c"),
                new ArrayList<String>(0));
        assertThat(executionContext.get(0).buildLineDescription().getRowType()).isEqualTo(ERowType.FOR);
        assertExecutableLine(executionContext.get(1), "\\", Arrays.asList("Log", "ok"), Arrays.asList("#cm1"));
        assertThat(executionContext.get(1).buildLineDescription().getRowType()).isEqualTo(ERowType.FOR_CONTINUE);
        final ForLoopContinueRowDescriptor<UserKeyword> forLineOne = (ForLoopContinueRowDescriptor<UserKeyword>) executionContext
                .get(1).buildLineDescription();
        assertThat(forLineOne.getKeywordAction().getToken().getText()).isEqualTo("Log");
        assertThat(forLineOne.getAction().getToken().getText()).isEqualTo("\\");
        assertExecutableLine(executionContext.get(2), "\\", Arrays.asList("Log", "ok2"), Arrays.asList("#cm2"));
        assertThat(executionContext.get(2).buildLineDescription().getRowType()).isEqualTo(ERowType.FOR_CONTINUE);
        final ForLoopContinueRowDescriptor<UserKeyword> forLineTwo = (ForLoopContinueRowDescriptor<UserKeyword>) executionContext
                .get(2).buildLineDescription();
        assertThat(forLineTwo.getKeywordAction().getToken().getText()).isEqualTo("Log");
        assertThat(forLineTwo.getAction().getToken().getText()).isEqualTo("\\");
        assertExecutableLine(executionContext.get(3), "Log", Arrays.asList("done"), new ArrayList<String>(0));
        assertThat(executionContext.get(3).buildLineDescription().getRowType()).isEqualTo(ERowType.SIMPLE);
    }

    @Test
    public void test_normalFor_withoutAnyCommentAndPreviousLineContinue_forTsvFormat_withMoreRobotLikeSyntax()
            throws Exception {
        final RobotFile modelFile = getModelFile(
                "ForWithoutAnyCommentConnectedToPreviousLineContinue_RobotLikeForItem.tsv");

        // verify
        assertThatOnlyKeywordTableIsPresent(modelFile);
        final KeywordTable keywordTable = modelFile.getKeywordTable();
        final List<UserKeyword> keywords = keywordTable.getKeywords();
        assertThat(keywords).hasSize(1);
        final UserKeyword userKeyword = keywords.get(0);
        final List<RobotExecutableRow<UserKeyword>> executionContext = userKeyword.getExecutionContext();
        assertThat(executionContext).hasSize(4);
        assertExecutableLine(executionContext.get(0), ":FOR", Arrays.asList("${x}", "IN", "a", "b", "c"),
                new ArrayList<String>(0));
        assertThat(executionContext.get(0).buildLineDescription().getRowType()).isEqualTo(ERowType.FOR);
        assertExecutableLine(executionContext.get(1), "\\", Arrays.asList("Log", "ok"), Arrays.asList("#cm1"));
        assertThat(executionContext.get(1).buildLineDescription().getRowType()).isEqualTo(ERowType.FOR_CONTINUE);
        final ForLoopContinueRowDescriptor<UserKeyword> forLineOne = (ForLoopContinueRowDescriptor<UserKeyword>) executionContext
                .get(1).buildLineDescription();
        assertThat(forLineOne.getKeywordAction().getToken().getText()).isEqualTo("Log");
        assertThat(forLineOne.getAction().getToken().getText()).isEqualTo("\\");
        assertExecutableLine(executionContext.get(2), "\\", Arrays.asList("Log", "ok2"), Arrays.asList("#cm2"));
        assertThat(executionContext.get(2).buildLineDescription().getRowType()).isEqualTo(ERowType.FOR_CONTINUE);
        final ForLoopContinueRowDescriptor<UserKeyword> forLineTwo = (ForLoopContinueRowDescriptor<UserKeyword>) executionContext
                .get(1).buildLineDescription();
        assertThat(forLineTwo.getKeywordAction().getToken().getText()).isEqualTo("Log");
        assertThat(forLineTwo.getAction().getToken().getText()).isEqualTo("\\");
        assertExecutableLine(executionContext.get(3), "Log", Arrays.asList("done"), new ArrayList<String>(0));
        assertThat(executionContext.get(3).buildLineDescription().getRowType()).isEqualTo(ERowType.SIMPLE);
    }

    @Test
    public void test_forWithContinueAfterForItem_shouldBeMerge_toForDeclaration_robotSyntaxStyle() throws Exception {
        final RobotFile modelFile = getModelFile("ForLoopWithContinueIterationDeclaration_robotStyleFor.txt");

        // verify
        assertThatOnlyKeywordTableIsPresent(modelFile);
        final KeywordTable keywordTable = modelFile.getKeywordTable();
        final List<UserKeyword> keywords = keywordTable.getKeywords();
        assertThat(keywords).hasSize(1);
        final UserKeyword userKeyword = keywords.get(0);
        final List<RobotExecutableRow<UserKeyword>> executionContext = userKeyword.getExecutionContext();
        assertThat(executionContext).hasSize(2);
        assertExecutableLine(executionContext.get(0), ":for", Arrays.asList("${x}", "IN", "elem1", "elem2", "${c}"),
                new ArrayList<String>(0));
        assertThat(executionContext.get(0).buildLineDescription().getRowType()).isEqualTo(ERowType.FOR);
        assertExecutableLine(executionContext.get(1), "Log", Arrays.asList("done"), new ArrayList<String>(0));
        assertThat(executionContext.get(1).buildLineDescription().getRowType()).isEqualTo(ERowType.SIMPLE);
    }

    @Test
    public void test_forWithContinueThreeTimesAfterForItem_shouldBeMerge_toForDeclaration_robotSyntaxStyle()
            throws Exception {
        // execute
        final RobotFile modelFile = getModelFile(
                "ForLoopWithContinueIterationDeclarationAndManyEmptyCellSlashAtBeginning_robotStyleFor.txt");

        // verify
        assertThatOnlyKeywordTableIsPresent(modelFile);
        final KeywordTable keywordTable = modelFile.getKeywordTable();
        final List<UserKeyword> keywords = keywordTable.getKeywords();
        assertThat(keywords).hasSize(1);
        final UserKeyword userKeyword = keywords.get(0);
        final List<RobotExecutableRow<UserKeyword>> executionContext = userKeyword.getExecutionContext();
        assertThat(executionContext).hasSize(2);
        assertExecutableLine(executionContext.get(0), ":for", Arrays.asList("${x}", "IN", "elem1", "elem2", "${c}"),
                new ArrayList<String>(0));
        assertThat(executionContext.get(0).buildLineDescription().getRowType()).isEqualTo(ERowType.FOR);
        assertExecutableLine(executionContext.get(1), "Log", Arrays.asList("done"), new ArrayList<String>(0));
        assertThat(executionContext.get(1).buildLineDescription().getRowType()).isEqualTo(ERowType.SIMPLE);
    }

    @Test
    public void test_forWithTheFirstForContinueEmpty() throws Exception {
        final RobotFile modelFile = getModelFile("CommentsNotInTheSameLineAsExecs_firstLineAfterForIsEmpty.txt");

        // verify
        assertThatOnlyKeywordTableIsPresent(modelFile);
        final KeywordTable keywordTable = modelFile.getKeywordTable();
        final List<UserKeyword> keywords = keywordTable.getKeywords();
        assertThat(keywords).hasSize(1);
        final UserKeyword userKeyword = keywords.get(0);
        final List<RobotExecutableRow<UserKeyword>> executionContext = userKeyword.getExecutionContext();
        assertThat(executionContext).hasSize(7);
        assertExecutableLine(executionContext.get(0), ": FoR", Arrays.asList("${x}", "IN", "cebula"),
                new ArrayList<String>(0));
        assertThat(executionContext.get(0).buildLineDescription().getRowType()).isEqualTo(ERowType.FOR);
        assertExecutableLine(executionContext.get(1), "\\", new ArrayList<String>(0), new ArrayList<String>(0));
        assertThat(executionContext.get(1).buildLineDescription().getRowType()).isEqualTo(ERowType.COMMENTED_HASH);
        assertExecutableLine(executionContext.get(2), "\\", new ArrayList<String>(0), Arrays.asList("#woda3"));
        assertThat(executionContext.get(2).buildLineDescription().getRowType()).isEqualTo(ERowType.COMMENTED_HASH);
        assertExecutableLine(executionContext.get(3), "\\", Arrays.asList("Log", "ok"), Arrays.asList("#woda"));
        assertThat(executionContext.get(3).buildLineDescription().getRowType()).isEqualTo(ERowType.FOR_CONTINUE);
        assertExecutableLine(executionContext.get(4), "\\", Arrays.asList("Log", "${x}"), new ArrayList<String>(0));
        assertThat(executionContext.get(4).buildLineDescription().getRowType()).isEqualTo(ERowType.FOR_CONTINUE);
        assertExecutableLine(executionContext.get(5), null, new ArrayList<String>(0), Arrays.asList("#", "ok"));
        assertThat(executionContext.get(5).buildLineDescription().getRowType()).isEqualTo(ERowType.COMMENTED_HASH);
        assertExecutableLine(executionContext.get(6), "log", Arrays.asList("ok"), new ArrayList<String>(0));
        assertThat(executionContext.get(6).buildLineDescription().getRowType()).isEqualTo(ERowType.SIMPLE);
    }

    @Test
    public void test_forWithLineContinueAndHashes() throws Exception {
        final RobotFile modelFile = getModelFile("ForWithLineContinueAndHashes.tsv");

        // verify
        assertThatOnlyKeywordTableIsPresent(modelFile);
        final KeywordTable keywordTable = modelFile.getKeywordTable();
        final List<UserKeyword> keywords = keywordTable.getKeywords();
        assertThat(keywords).hasSize(1);
        final UserKeyword userKeyword = keywords.get(0);
        final List<RobotExecutableRow<UserKeyword>> executionContext = userKeyword.getExecutionContext();
        assertThat(executionContext).hasSize(9);
        assertExecutableLine(executionContext.get(0), ":FOR",
                Arrays.asList("${x}", "IN", "1", "2", "", "3", "4", "Log"), new ArrayList<String>(0));
        assertThat(executionContext.get(0).buildLineDescription().getRowType()).isEqualTo(ERowType.FOR);
        assertExecutableLine(executionContext.get(1), "\\", Arrays.asList("${x}"), Arrays.asList("#   data"));
        assertThat(executionContext.get(1).buildLineDescription().getRowType()).isEqualTo(ERowType.FOR_CONTINUE);
        assertExecutableLine(executionContext.get(2), "\\", Arrays.asList("log"), Arrays.asList("#   hugo"));
        assertThat(executionContext.get(2).buildLineDescription().getRowType()).isEqualTo(ERowType.FOR_CONTINUE);
        assertExecutableLine(executionContext.get(3), "\\", new ArrayList<String>(0), Arrays.asList("#", "hash"));
        assertThat(executionContext.get(3).buildLineDescription().getRowType()).isEqualTo(ERowType.COMMENTED_HASH);
        assertExecutableLine(executionContext.get(4), "\\", Arrays.asList("\\", "kw_w"), Arrays.asList("#", "d"));
        assertThat(executionContext.get(4).buildLineDescription().getRowType()).isEqualTo(ERowType.FOR_CONTINUE);
        assertExecutableLine(executionContext.get(5), "\\", Arrays.asList("kw_w"), new ArrayList<String>(0));
        assertThat(executionContext.get(5).buildLineDescription().getRowType()).isEqualTo(ERowType.FOR_CONTINUE);
        assertExecutableLine(executionContext.get(6), "...  #ok4", new ArrayList<String>(0), new ArrayList<String>(0));
        assertThat(executionContext.get(6).buildLineDescription().getRowType()).isEqualTo(ERowType.SIMPLE);
        assertExecutableLine(executionContext.get(7), "log", Arrays.asList("ok"), new ArrayList<String>(0));
        assertThat(executionContext.get(7).buildLineDescription().getRowType()).isEqualTo(ERowType.SIMPLE);
        assertExecutableLine(executionContext.get(8), "...  #ok", new ArrayList<String>(0), new ArrayList<String>(0));
        assertThat(executionContext.get(8).buildLineDescription().getRowType()).isEqualTo(ERowType.SIMPLE);
    }

    @Test
    public void test_simpleLineContinueInManyLines_execInSecondLine() throws Exception {
        final RobotFile modelFile = getModelFile("SimpleLineContinueInManyLines_ExecKeywordInSecondLine.tsv");

        // verify
        assertThatOnlyKeywordTableIsPresent(modelFile);
        final KeywordTable keywordTable = modelFile.getKeywordTable();
        final List<UserKeyword> keywords = keywordTable.getKeywords();
        assertThat(keywords).hasSize(1);
        final UserKeyword userKeyword = keywords.get(0);
        final List<RobotExecutableRow<UserKeyword>> executionContext = userKeyword.getExecutionContext();
        assertThat(executionContext).hasSize(1);
        assertExecutableLine(executionContext.get(0), "${x}", Arrays.asList("", "Set Variable", "2"),
                new ArrayList<String>(0));
        assertThat(executionContext.get(0).buildLineDescription().getRowType()).isEqualTo(ERowType.SIMPLE);
    }

    @Test
    public void test_simpleLineWithManyLineContinue() throws Exception {
        final RobotFile modelFile = getModelFile("SingleLineWithManyLineContinue.tsv");

        // verify
        assertThatOnlyKeywordTableIsPresent(modelFile);
        final KeywordTable keywordTable = modelFile.getKeywordTable();
        final List<UserKeyword> keywords = keywordTable.getKeywords();
        assertThat(keywords).hasSize(1);
        final UserKeyword userKeyword = keywords.get(0);
        final List<RobotExecutableRow<UserKeyword>> executionContext = userKeyword.getExecutionContext();
        assertThat(executionContext).hasSize(2);
        assertExecutableLine(executionContext.get(0), "", Arrays.asList("Log", "", "ok"), new ArrayList<String>(0));
        assertThat(executionContext.get(0).buildLineDescription().getRowType()).isEqualTo(ERowType.SIMPLE);
        assertExecutableLine(executionContext.get(1), "\\", Arrays.asList("log", "ok"), new ArrayList<String>(0));
        assertThat(executionContext.get(1).buildLineDescription().getRowType()).isEqualTo(ERowType.SIMPLE);
    }

    @Test
    public void test_forWithTwoCommentsAfterForDeclaration() throws Exception {
        final RobotFile modelFile = getModelFile("ForWithTwoCommentsAtTheBeginning.tsv");

        // verify
        assertThatOnlyKeywordTableIsPresent(modelFile);
        final KeywordTable keywordTable = modelFile.getKeywordTable();
        final List<UserKeyword> keywords = keywordTable.getKeywords();
        assertThat(keywords).hasSize(1);
        final UserKeyword userKeyword = keywords.get(0);
        final List<RobotExecutableRow<UserKeyword>> executionContext = userKeyword.getExecutionContext();
        assertThat(executionContext).hasSize(8);

        assertExecutableLine(executionContext.get(0), ": FOR", Arrays.asList("${x}", "IN", "1", "2", "", "3", "4"),
                new ArrayList<String>(0));
        assertThat(executionContext.get(0).buildLineDescription().getRowType()).isEqualTo(ERowType.FOR);
        assertExecutableLine(executionContext.get(1), null, new ArrayList<String>(0), Arrays.asList("#   data"));
        assertThat(executionContext.get(1).buildLineDescription().getRowType()).isEqualTo(ERowType.COMMENTED_HASH);
        assertExecutableLine(executionContext.get(2), null, new ArrayList<String>(0), Arrays.asList("#   hugo"));
        assertThat(executionContext.get(2).buildLineDescription().getRowType()).isEqualTo(ERowType.COMMENTED_HASH);
        assertExecutableLine(executionContext.get(3), "...  ok", Arrays.asList("", "kw_w"),
                Arrays.asList("#", "hash", "#", "d"));
        assertThat(executionContext.get(3).buildLineDescription().getRowType()).isEqualTo(ERowType.SIMPLE);
        assertExecutableLine(executionContext.get(4), "\\", Arrays.asList("kw_w"), new ArrayList<String>(0));
        assertThat(executionContext.get(4).buildLineDescription().getRowType()).isEqualTo(ERowType.SIMPLE);
        assertExecutableLine(executionContext.get(5), "...  #ok4", new ArrayList<String>(0), new ArrayList<String>(0));
        assertThat(executionContext.get(5).buildLineDescription().getRowType()).isEqualTo(ERowType.SIMPLE);
        assertExecutableLine(executionContext.get(6), "log", Arrays.asList("ok"), new ArrayList<String>(0));
        assertThat(executionContext.get(6).buildLineDescription().getRowType()).isEqualTo(ERowType.SIMPLE);
        assertExecutableLine(executionContext.get(7), "...  #ok", new ArrayList<String>(0), new ArrayList<String>(0));
        assertThat(executionContext.get(7).buildLineDescription().getRowType()).isEqualTo(ERowType.SIMPLE);
    }

    @Test
    public void test_forLoopWithTwoContinueOnePythonLineOneHashComment() throws Exception {
        final RobotFile modelFile = getModelFile("ForLoopWithTwoContinueOnePythonLineOneHashComment.tsv");

        // verify
        assertThatOnlyKeywordTableIsPresent(modelFile);
        final KeywordTable keywordTable = modelFile.getKeywordTable();
        final List<UserKeyword> keywords = keywordTable.getKeywords();
        assertThat(keywords).hasSize(1);
        final UserKeyword userKeyword = keywords.get(0);
        final List<RobotExecutableRow<UserKeyword>> executionContext = userKeyword.getExecutionContext();
        assertThat(executionContext).hasSize(3);

        assertExecutableLine(executionContext.get(0), ":FOR", Arrays.asList("${x}", "IN", "10"),
                new ArrayList<String>(0));
        assertThat(executionContext.get(0).buildLineDescription().getRowType()).isEqualTo(ERowType.FOR);
        assertExecutableLine(executionContext.get(1), "\\", Arrays.asList("Log2", "ok"), new ArrayList<String>(0));
        assertThat(executionContext.get(1).buildLineDescription().getRowType()).isEqualTo(ERowType.FOR_CONTINUE);
        assertExecutableLine(executionContext.get(2), "\\", Arrays.asList("Log", "${x}"), Arrays.asList("#cm1"));
        assertThat(executionContext.get(2).buildLineDescription().getRowType()).isEqualTo(ERowType.FOR_CONTINUE);
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

    private void assertThatOnlyKeywordTableIsPresent(final RobotFile modelFile) {
        assertThat(modelFile.getSettingTable().isPresent()).isFalse();
        assertThat(modelFile.getVariableTable().isPresent()).isFalse();
        assertThat(modelFile.getTestCaseTable().isPresent()).isFalse();
        assertThat(modelFile.getKeywordTable().isPresent()).isTrue();
    }

    private RobotFile getModelFile(final String fileName) throws Exception {
        // prepare
        final RobotRuntimeEnvironment runtime = mock(RobotRuntimeEnvironment.class);
        when(runtime.getVersion()).thenReturn("2.9");
        final RobotProjectHolder projectHolder = mock(RobotProjectHolder.class);
        when(projectHolder.getRobotRuntime()).thenReturn(runtime);
        final String mainPath = "parser/bugs/for/";
        final File file = new File(RobotParser.class.getResource(mainPath + fileName).toURI());
        when(projectHolder.shouldBeLoaded(file)).thenReturn(true);

        // execute
        final RobotParser parser = RobotParser.create(projectHolder, RobotParserConfig.allImportsLazy());
        final List<RobotFileOutput> parsed = parser.parse(file);

        // verify
        assertThat(parsed).hasSize(1);
        final RobotFileOutput robotFileOutput = parsed.get(0);
        assertThat(robotFileOutput.getStatus()).isEqualTo(Status.PASSED);
        assertThat(robotFileOutput.getType()).isEqualTo(RobotFileType.RESOURCE);
        return robotFileOutput.getFileModel();
    }
}
