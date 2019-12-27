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

public class CreationOfKeywordArgumentsTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordArguments_withoutKeywordName_andArgumentsDecOnly(
            final FileFormat format) throws Exception {
        test_emptyFile_argsDecOnly("EmptyKeywordArgumentsNoKeywordName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordArguments_withKeywordName_andArgumentsDecOnly(
            final FileFormat format) throws Exception {
        test_emptyFile_argsDecOnly("EmptyKeywordArguments", "User Keyword", format);
    }

    private void test_emptyFile_argsDecOnly(final String fileNameWithoutExt, final String userKeywordName,
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
        uk.newArguments(0);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordArguments_withoutKeywordName_andArgumentsDec_andCommentsOnly(
            final FileFormat format) throws Exception {
        test_emptyFile_CommentsOnly("KeywordArgumentsWithCommentAndNoKeywordName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordArguments_withKeywordName_andArgumentsDec_andCommentsOnly(
            final FileFormat format) throws Exception {
        test_emptyFile_CommentsOnly("KeywordArgumentsWithCommentOnly", "User Keyword", format);
    }

    private void test_emptyFile_CommentsOnly(final String fileNameWithoutExt, final String userKeywordName,
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

        final LocalSetting<UserKeyword> newArguments = uk.newArguments(0);
        newArguments.addCommentPart("cm1");
        newArguments.addCommentPart("cm2");
        newArguments.addCommentPart("cm3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordArguments_withoutKeywordName_andArgumentsDec_and3ArgsOnly(
            final FileFormat format) throws Exception {
        test_emptyFile_createKeyArgs_3ArgsOnly("KeywordArgumentsWith3ArgsAndNoKeywordName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordArguments_withKeywordName_andArgumentsDec_and3ArgsOnly(
            final FileFormat format) throws Exception {
        test_emptyFile_createKeyArgs_3ArgsOnly("KeywordArgumentsWith3ArgsOnly", "User Keyword", format);
    }

    private void test_emptyFile_createKeyArgs_3ArgsOnly(final String fileNameWithoutExt, final String userKeywordName,
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

        final LocalSetting<UserKeyword> newArguments = uk.newArguments(0);
        newArguments.addToken("${arg1}");
        newArguments.addToken("${arg2}");
        newArguments.addToken("${arg3}");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordArguments_withoutKeywordName_andArgumentsDec_and3Args_andCommentOnly(
            final FileFormat format) throws Exception {
        test_emptyFile_keywordArgsCreation_plus3Args_andComment("KeywordArgumentsWith3ArgsCommentAndNoKeywordName", "",
                format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordArguments_withKeywordName_andArgumentsDec_and3Args_andCommentOnly(
            final FileFormat format) throws Exception {
        test_emptyFile_keywordArgsCreation_plus3Args_andComment("KeywordArgumentsWith3ArgsCommentOnly", "User Keyword",
                format);
    }

    private void test_emptyFile_keywordArgsCreation_plus3Args_andComment(final String fileNameWithoutExt,
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

        final LocalSetting<UserKeyword> newArguments = uk.newArguments(0);
        newArguments.addToken("${arg1}");
        newArguments.addToken("${arg2}");
        newArguments.addToken("${arg3}");
        newArguments.addCommentPart("cm1");
        newArguments.addCommentPart("cm2");
        newArguments.addCommentPart("cm3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    private String convert(final String fileName, final FileFormat format) {
        return "keywords/setting/arguments/new/" + fileName + "." + format.getExtension();
    }
}
