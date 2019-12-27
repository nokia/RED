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

public class CreationOfKeywordTeardownTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTeardown_withoutKeywordName_andTeardownDecOnly(
            final FileFormat format) throws Exception {
        test_teardownDecOnly("EmptyKeywordTeardownNoKeywordName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTeardown_withKeywordName_andTeardownDecOnly(final FileFormat format)
            throws Exception {
        test_teardownDecOnly("EmptyKeywordTeardown", "User Keyword", format);
    }

    private void test_teardownDecOnly(final String fileNameWithoutExt, final String userKeywordName,
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
        uk.newTeardown(0);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTeardown_withoutKeywordName_andTeardown_andComment(
            final FileFormat format) throws Exception {
        test_teardownWithCommentOnly("EmptyKeywordTeardownCommentNoKeywordName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTeardown_withKeywordName_andTeardown_andComment(
            final FileFormat format) throws Exception {
        test_teardownWithCommentOnly("EmptyKeywordTeardownComment", "User Keyword", format);
    }

    private void test_teardownWithCommentOnly(final String fileNameWithoutExt, final String userKeywordName,
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

        final LocalSetting<UserKeyword> keyTear = uk.newTeardown(0);
        keyTear.addCommentPart("cm1");
        keyTear.addCommentPart("cm2");
        keyTear.addCommentPart("cm3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTeardown_withoutKeywordName_andTeardown_andExecKey(
            final FileFormat format) throws Exception {
        test_teardownWithExec("KeywordTeardownExecKeywordNoKeywordName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTeardown_withKeywordName_andTeardown_andExecKey(
            final FileFormat format) throws Exception {
        test_teardownWithExec("KeywordTeardownExecKeyword", "User Keyword", format);
    }

    private void test_teardownWithExec(final String fileNameWithoutExt, final String userKeywordName,
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

        final LocalSetting<UserKeyword> keyTear = uk.newTeardown(0);
        keyTear.addToken("execKey");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTeardown_withoutKeywordName_andTeardown_andExecKey_andComment(
            final FileFormat format) throws Exception {
        test_teardownWithExec_andComment("KeywordTeardownExecKeywordAndCommentNoKeywordName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTeardown_withKeywordName_andTeardown_andExecKey_andComment(
            final FileFormat format) throws Exception {
        test_teardownWithExec_andComment("KeywordTeardownExecKeywordAndComment", "User Keyword", format);
    }

    private void test_teardownWithExec_andComment(final String fileNameWithoutExt, final String userKeywordName,
            final FileFormat format)
            throws Exception {
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

        final LocalSetting<UserKeyword> keyTear = uk.newTeardown(0);
        keyTear.addToken("execKey");
        keyTear.addCommentPart("cm1");
        keyTear.addCommentPart("cm2");
        keyTear.addCommentPart("cm3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTeardown_withoutKeywordName_andTeardown_andExecKey_and3Args(
            final FileFormat format) throws Exception {
        test_teardownWithExec_and3Args("KeywordTeardownExecKeywordAnd3ArgsNoKeywordName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTeardown_withKeywordName_andTeardown_andExecKey_and3Args(
            final FileFormat format) throws Exception {
        test_teardownWithExec_and3Args("KeywordTeardownExecKeywordAnd3Args", "User Keyword", format);
    }

    private void test_teardownWithExec_and3Args(final String fileNameWithoutExt, final String userKeywordName,
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

        final LocalSetting<UserKeyword> keyTear = uk.newTeardown(0);
        keyTear.addToken("execKey");
        keyTear.addToken("arg1");
        keyTear.addToken("arg2");
        keyTear.addToken("arg3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTeardown_withoutKeywordName_andTeardown_andExecKey_and3Args_andComment(
            final FileFormat format) throws Exception {
        test_teardownWithExec_and3Args_andComment("KeywordTeardownExecKeywordAnd3ArgsAndCommentNoKeywordName", "",
                format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTeardown_withKeywordName_andTeardown_andExecKey_and3Args_andComment(
            final FileFormat format) throws Exception {
        test_teardownWithExec_and3Args_andComment("KeywordTeardownExecKeywordAnd3ArgsAndComment", "User Keyword",
                format);
    }

    private void test_teardownWithExec_and3Args_andComment(final String fileNameWithoutExt,
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

        final LocalSetting<UserKeyword> keyTear = uk.newTeardown(0);
        keyTear.addToken("execKey");
        keyTear.addToken("arg1");
        keyTear.addToken("arg2");
        keyTear.addToken("arg3");
        keyTear.addCommentPart("cm1");
        keyTear.addCommentPart("cm2");
        keyTear.addCommentPart("cm3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    private String convert(final String fileName, final FileFormat format) {
        return "keywords/setting/teardown/new/" + fileName + "." + format.getExtension();
    }
}
