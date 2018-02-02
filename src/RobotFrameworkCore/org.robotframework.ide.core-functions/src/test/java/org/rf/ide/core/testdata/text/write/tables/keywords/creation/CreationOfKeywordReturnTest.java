/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.keywords.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.keywords.KeywordReturn;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
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
        final KeywordReturn keyReturn = uk.newReturn(0);

        final RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        final RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        final RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        keyReturn.addCommentPart(cmTok1);
        keyReturn.addCommentPart(cmTok2);
        keyReturn.addCommentPart(cmTok3);

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
        final KeywordReturn keyReturn = uk.newReturn(0);

        final RobotToken rTok1 = new RobotToken();
        rTok1.setText("${r1}");
        final RobotToken rTok2 = new RobotToken();
        rTok2.setText("${r2}");
        final RobotToken rTok3 = new RobotToken();
        rTok3.setText("${r3}");

        keyReturn.addReturnValue(rTok1);
        keyReturn.addReturnValue(rTok2);
        keyReturn.addReturnValue(rTok3);

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
        final KeywordReturn keyReturn = uk.newReturn(0);

        final RobotToken rTok1 = new RobotToken();
        rTok1.setText("${r1}");
        final RobotToken rTok2 = new RobotToken();
        rTok2.setText("${r2}");
        final RobotToken rTok3 = new RobotToken();
        rTok3.setText("${r3}");

        keyReturn.addReturnValue(rTok1);
        keyReturn.addReturnValue(rTok2);
        keyReturn.addReturnValue(rTok3);

        final RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        final RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        final RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        keyReturn.addCommentPart(cmTok1);
        keyReturn.addCommentPart(cmTok2);
        keyReturn.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    private String convert(final String fileName) {
        return "keywords/setting/return/new/" + fileName + "." + getExtension();
    }
}
