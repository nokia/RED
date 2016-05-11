/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.keywords.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.keywords.KeywordArguments;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public abstract class ACreationOfKeywordArgumentsTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "keywords//setting//arguments//new//";

    private final String extension;

    public ACreationOfKeywordArgumentsTest(final String extension) {
        this.extension = extension;
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordArguments_withoutKeywordName_andArgumentsDecOnly()
            throws Exception {
        test_emptyFile_argsDecOnly("", "EmptyKeywordArgumentsNoKeywordName");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordArguments_withKeywordName_andArgumentsDecOnly() throws Exception {
        test_emptyFile_argsDecOnly("User Keyword", "EmptyKeywordArguments");
    }

    private void test_emptyFile_argsDecOnly(final String userKeywordName, final String fileNameWithoutExt)
            throws Exception {
        // prepare
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileNameWithoutExt + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        uk.newArguments();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordArguments_withoutKeywordName_andArgumentsDec_andCommentsOnly()
            throws Exception {
        test_emptyFile_CommentsOnly("KeywordArgumentsWithCommentAndNoKeywordName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordArguments_withKeywordName_andArgumentsDec_andCommentsOnly()
            throws Exception {
        test_emptyFile_CommentsOnly("KeywordArgumentsWithCommentOnly", "User Keyword");
    }

    private void test_emptyFile_CommentsOnly(final String fileNameWithoutExt, final String userKeywordName)
            throws Exception {
        // prepare
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileNameWithoutExt + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordArguments newArguments = uk.newArguments();

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        newArguments.addCommentPart(cmTok1);
        newArguments.addCommentPart(cmTok2);
        newArguments.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordArguments_withoutKeywordName_andArgumentsDec_and3ArgsOnly()
            throws Exception {
        test_emptyFile_createKeyArgs_3ArgsOnly("KeywordArgumentsWith3ArgsAndNoKeywordName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordArguments_withKeywordName_andArgumentsDec_and3ArgsOnly()
            throws Exception {
        test_emptyFile_createKeyArgs_3ArgsOnly("KeywordArgumentsWith3ArgsOnly", "User Keyword");
    }

    private void test_emptyFile_createKeyArgs_3ArgsOnly(final String fileNameWithoutExt, final String userKeywordName)
            throws Exception {
        // prepare
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileNameWithoutExt + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordArguments newArguments = uk.newArguments();

        RobotToken argTok1 = new RobotToken();
        argTok1.setText("${arg1}");
        RobotToken argTok2 = new RobotToken();
        argTok2.setText("${arg2}");
        RobotToken argTok3 = new RobotToken();
        argTok3.setText("${arg3}");

        newArguments.addArgument(argTok1);
        newArguments.addArgument(argTok2);
        newArguments.addArgument(argTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordArguments_withoutKeywordName_andArgumentsDec_and3Args_andCommentOnly()
            throws Exception {
        test_emptyFile_keywordArgsCreation_plus3Args_andComment("KeywordArgumentsWith3ArgsCommentAndNoKeywordName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordArguments_withKeywordName_andArgumentsDec_and3Args_andCommentOnly()
            throws Exception {
        test_emptyFile_keywordArgsCreation_plus3Args_andComment("KeywordArgumentsWith3ArgsCommentOnly", "User Keyword");
    }

    private void test_emptyFile_keywordArgsCreation_plus3Args_andComment(final String fileNameWithoutExt,
            final String userKeywordName) throws Exception {
        // prepare
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileNameWithoutExt + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordArguments newArguments = uk.newArguments();

        RobotToken argTok1 = new RobotToken();
        argTok1.setText("${arg1}");
        RobotToken argTok2 = new RobotToken();
        argTok2.setText("${arg2}");
        RobotToken argTok3 = new RobotToken();
        argTok3.setText("${arg3}");

        newArguments.addArgument(argTok1);
        newArguments.addArgument(argTok2);
        newArguments.addArgument(argTok3);

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        newArguments.addCommentPart(cmTok1);
        newArguments.addCommentPart(cmTok2);
        newArguments.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    public String getExtension() {
        return extension;
    }
}
