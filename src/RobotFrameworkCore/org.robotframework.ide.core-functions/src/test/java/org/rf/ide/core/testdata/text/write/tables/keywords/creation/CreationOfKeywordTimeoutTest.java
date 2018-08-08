/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.keywords.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;

public class CreationOfKeywordTimeoutTest extends RobotFormatParameterizedTest {

    public CreationOfKeywordTimeoutTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTimeout_withoutKeywordName_andTimeoutDecOnly() throws Exception {
        test_timeoutDecOnly("EmptyKeywordTimeoutNoKeywordName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTimeout_withKeywordName_andTimeoutDecOnly() throws Exception {
        test_timeoutDecOnly("EmptyKeywordTimeout", "User Keyword");
    }

    private void test_timeoutDecOnly(final String fileNameWithoutExt, final String userKeywordName) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        final KeywordTable keywordTable = modelFile.getKeywordTable();

        final RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        final UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        uk.newTimeout(0);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTimeout_withoutKeywordName_andTimeoutDec_withComment()
            throws Exception {
        test_timeoutDec_withCommentOnly("EmptyKeywordTimeoutCommentNoKeywordName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTimeout_withKeywordName_andTimeoutDec_withComment()
            throws Exception {
        test_timeoutDec_withCommentOnly("EmptyKeywordTimeoutComment", "User Keyword");
    }

    private void test_timeoutDec_withCommentOnly(final String fileNameWithoutExt, final String userKeywordName)
            throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        final KeywordTable keywordTable = modelFile.getKeywordTable();

        final RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        final UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);

        final LocalSetting<UserKeyword> keyTimeout = uk.newTimeout(0);
        keyTimeout.addCommentPart("cm1");
        keyTimeout.addCommentPart("cm2");
        keyTimeout.addCommentPart("cm3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTimeout_withoutKeywordName_andTimeoutDec_withValue()
            throws Exception {
        test_timeoutDec_withComment_andValue("EmptyKeywordTimeoutWithValueNoKeywordName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTimeout_withKeywordName_andTimeoutDec_withValue() throws Exception {
        test_timeoutDec_withComment_andValue("EmptyKeywordTimeoutWithValue", "User Keyword");
    }

    private void test_timeoutDec_withComment_andValue(final String fileNameWithoutExt, final String userKeywordName)
            throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        final KeywordTable keywordTable = modelFile.getKeywordTable();

        final RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        final UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);

        final LocalSetting<UserKeyword> keyTimeout = uk.newTimeout(0);
        keyTimeout.addToken("1 hours");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTimeout_withoutKeywordName_andTimeoutDec_withValue_andComment()
            throws Exception {
        test_timeoutDec_withComment_andValue_andComment("EmptyKeywordTimeoutWithValueAndCommentNoKeywordName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTimeout_withKeywordName_andTimeoutDec_withValue_andComment()
            throws Exception {
        test_timeoutDec_withComment_andValue_andComment("EmptyKeywordTimeoutWithValueAndComment", "User Keyword");
    }

    private void test_timeoutDec_withComment_andValue_andComment(final String fileNameWithoutExt,
            final String userKeywordName) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        final KeywordTable keywordTable = modelFile.getKeywordTable();

        final RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        final UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);

        final LocalSetting<UserKeyword> keyTimeout = uk.newTimeout(0);
        keyTimeout.addToken("1 hours");
        keyTimeout.addCommentPart("cm1");
        keyTimeout.addCommentPart("cm2");
        keyTimeout.addCommentPart("cm3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTimeout_withoutKeywordName_andTimeoutDec_withValue_and3MsgArgs()
            throws Exception {
        test_timeoutDec_withComment_andValue_and3MsgArgs("KeywordTimeoutWithValueAnd3MsgArgsNoKeywordName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTimeout_withKeywordName_andTimeoutDec_withValue_and3MsgArgs()
            throws Exception {
        test_timeoutDec_withComment_andValue_and3MsgArgs("KeywordTimeoutWithValueAnd3MsgArgs", "User Keyword");
    }

    private void test_timeoutDec_withComment_andValue_and3MsgArgs(final String fileNameWithoutExt,
            final String userKeywordName) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        final KeywordTable keywordTable = modelFile.getKeywordTable();

        final RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        final UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);

        final LocalSetting<UserKeyword> keyTimeout = uk.newTimeout(0);
        keyTimeout.addToken("1 hours");
        keyTimeout.addToken("msg1");
        keyTimeout.addToken("msg2");
        keyTimeout.addToken("msg3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTimeout_withoutKeywordName_andTimeoutDec_withValue_and3MsgArgs_andComment()
            throws Exception {
        test_timeoutDec_withComment_andValue_and3MsgArgs_andComment(
                "KeywordTimeoutWithValueAnd3MsgArgsAndCommentNoKeywordName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTimeout_withKeywordName_andTimeoutDec_withValue_and3MsgArgs_andComment()
            throws Exception {
        test_timeoutDec_withComment_andValue_and3MsgArgs_andComment("KeywordTimeoutWithValueAnd3MsgArgsAndComment",
                "User Keyword");
    }

    private void test_timeoutDec_withComment_andValue_and3MsgArgs_andComment(final String fileNameWithoutExt,
            final String userKeywordName) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        final KeywordTable keywordTable = modelFile.getKeywordTable();

        final RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        final UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);

        final LocalSetting<UserKeyword> keyTimeout = uk.newTimeout(0);
        keyTimeout.addToken("1 hours");
        keyTimeout.addToken("msg1");
        keyTimeout.addToken("msg2");
        keyTimeout.addToken("msg3");
        keyTimeout.addCommentPart("cm1");
        keyTimeout.addCommentPart("cm2");
        keyTimeout.addCommentPart("cm3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    private String convert(final String fileName) {
        return "keywords/setting/timeout/new/" + fileName + "." + getExtension();
    }
}
