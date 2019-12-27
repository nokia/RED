/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.keywords.creation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public class CreationOfKeywordTimeoutTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTimeout_withoutKeywordName_andTimeoutDecOnly(
            final FileFormat format) throws Exception {
        test_timeoutDecOnly("EmptyKeywordTimeoutNoKeywordName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTimeout_withKeywordName_andTimeoutDecOnly(final FileFormat format)
            throws Exception {
        test_timeoutDecOnly("EmptyKeywordTimeout", "User Keyword", format);
    }

    private void test_timeoutDecOnly(final String fileNameWithoutExt, final String userKeywordName,
            final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt, format);
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

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTimeout_withoutKeywordName_andTimeoutDec_withComment(
            final FileFormat format) throws Exception {
        test_timeoutDec_withCommentOnly("EmptyKeywordTimeoutCommentNoKeywordName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTimeout_withKeywordName_andTimeoutDec_withComment(
            final FileFormat format) throws Exception {
        test_timeoutDec_withCommentOnly("EmptyKeywordTimeoutComment", "User Keyword", format);
    }

    private void test_timeoutDec_withCommentOnly(final String fileNameWithoutExt, final String userKeywordName,
            final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt, format);
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

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTimeout_withoutKeywordName_andTimeoutDec_withValue(
            final FileFormat format) throws Exception {
        test_timeoutDec_withComment_andValue("EmptyKeywordTimeoutWithValueNoKeywordName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTimeout_withKeywordName_andTimeoutDec_withValue(
            final FileFormat format) throws Exception {
        test_timeoutDec_withComment_andValue("EmptyKeywordTimeoutWithValue", "User Keyword", format);
    }

    private void test_timeoutDec_withComment_andValue(final String fileNameWithoutExt, final String userKeywordName,
            final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt, format);
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

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTimeout_withoutKeywordName_andTimeoutDec_withValue_andComment(
            final FileFormat format) throws Exception {
        test_timeoutDec_withComment_andValue_andComment("EmptyKeywordTimeoutWithValueAndCommentNoKeywordName", "",
                format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTimeout_withKeywordName_andTimeoutDec_withValue_andComment(
            final FileFormat format) throws Exception {
        test_timeoutDec_withComment_andValue_andComment("EmptyKeywordTimeoutWithValueAndComment", "User Keyword",
                format);
    }

    private void test_timeoutDec_withComment_andValue_andComment(final String fileNameWithoutExt,
            final String userKeywordName, final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt, format);
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

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTimeout_withoutKeywordName_andTimeoutDec_withValue_and3MsgArgs(
            final FileFormat format) throws Exception {
        test_timeoutDec_withComment_andValue_and3MsgArgs("KeywordTimeoutWithValueAnd3MsgArgsNoKeywordName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTimeout_withKeywordName_andTimeoutDec_withValue_and3MsgArgs(
            final FileFormat format) throws Exception {
        test_timeoutDec_withComment_andValue_and3MsgArgs("KeywordTimeoutWithValueAnd3MsgArgs", "User Keyword", format);
    }

    private void test_timeoutDec_withComment_andValue_and3MsgArgs(final String fileNameWithoutExt,
            final String userKeywordName, final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt, format);
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

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTimeout_withoutKeywordName_andTimeoutDec_withValue_and3MsgArgs_andComment(
            final FileFormat format) throws Exception {
        test_timeoutDec_withComment_andValue_and3MsgArgs_andComment(
                "KeywordTimeoutWithValueAnd3MsgArgsAndCommentNoKeywordName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTimeout_withKeywordName_andTimeoutDec_withValue_and3MsgArgs_andComment(
            final FileFormat format) throws Exception {
        test_timeoutDec_withComment_andValue_and3MsgArgs_andComment("KeywordTimeoutWithValueAnd3MsgArgsAndComment",
                "User Keyword", format);
    }

    private void test_timeoutDec_withComment_andValue_and3MsgArgs_andComment(final String fileNameWithoutExt,
            final String userKeywordName, final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt, format);
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

    private String convert(final String fileName, final FileFormat format) {
        return "keywords/setting/timeout/new/" + fileName + "." + format.getExtension();
    }
}
