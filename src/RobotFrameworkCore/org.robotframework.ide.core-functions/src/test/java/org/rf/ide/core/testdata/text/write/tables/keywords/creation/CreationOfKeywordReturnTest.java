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

public class CreationOfKeywordReturnTest extends RobotFormatParameterizedTest {

    public CreationOfKeywordReturnTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordReturn_withoutKeywordName_andReturnDecOnly() throws Exception {
        test_returnDecOnly("EmptyKeywordReturnNoKeywordName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordReturn_withKeywordName_andReturnDecOnly() throws Exception {
        test_returnDecOnly("EmptyKeywordReturn", "User Keyword");
    }

    private void test_returnDecOnly(final String fileNameWithoutExt, final String userKeywordName) throws Exception {
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
        uk.newReturn(0);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordReturn_withoutKeywordName_andReturn_andComment() throws Exception {
        test_returnWithCommentOnly("EmptyKeywordReturnNoKeywordNameComment", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordReturn_withKeywordName_andReturn_andComment() throws Exception {
        test_returnWithCommentOnly("EmptyKeywordReturnComment", "User Keyword");
    }

    private void test_returnWithCommentOnly(final String fileNameWithoutExt, final String userKeywordName)
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

        final LocalSetting<UserKeyword> keyReturn = uk.newReturn(0);
        keyReturn.addCommentPart("cm1");
        keyReturn.addCommentPart("cm2");
        keyReturn.addCommentPart("cm3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordReturn_withoutKeywordName_andReturn_with3Values() throws Exception {
        test_return_With3Values("KeywordReturn3ReturnsNoKeywordName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordReturn_withKeywordName_andReturn_with3Values() throws Exception {
        test_return_With3Values("KeywordReturn3Returns", "User Keyword");
    }

    private void test_return_With3Values(final String fileNameWithoutExt, final String userKeywordName)
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

        final LocalSetting<UserKeyword> keyReturn = uk.newReturn(0);
        keyReturn.addToken("${r1}");
        keyReturn.addToken("${r2}");
        keyReturn.addToken("${r3}");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordReturn_withoutKeywordName_andReturn_with3Values_andComment()
            throws Exception {
        test_returnWith_3ValuesAndComment("KeywordReturn3ReturnsCommentNoKeywordName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordReturn_withKeywordName_andReturn_with3Values_andComment()
            throws Exception {
        test_returnWith_3ValuesAndComment("KeywordReturn3ReturnsComment", "User Keyword");
    }

    private void test_returnWith_3ValuesAndComment(final String fileNameWithoutExt, final String userKeywordName)
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

        final LocalSetting<UserKeyword> keyReturn = uk.newReturn(0);
        keyReturn.addToken("${r1}");
        keyReturn.addToken("${r2}");
        keyReturn.addToken("${r3}");
        keyReturn.addCommentPart("cm1");
        keyReturn.addCommentPart("cm2");
        keyReturn.addCommentPart("cm3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    private String convert(final String fileName) {
        return "keywords/setting/return/new/" + fileName + "." + getExtension();
    }
}
