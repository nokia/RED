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
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public abstract class ACreationOfKeywordReturnTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "keywords//setting//return//new//";

    private final String extension;

    public ACreationOfKeywordReturnTest(final String extension) {
        this.extension = extension;
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordReturn_withoutKeywordName_andReturnDecOnly() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "EmptyKeywordReturnNoKeywordName." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        uk.newReturn();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordReturn_withKeywordName_andReturnDecOnly() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "EmptyKeywordReturn." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("User Keyword");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        uk.newReturn();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordReturn_withoutKeywordName_andReturn_andComment() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "EmptyKeywordReturnNoKeywordNameComment." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordReturn keyReturn = uk.newReturn();

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        keyReturn.addCommentPart(cmTok1);
        keyReturn.addCommentPart(cmTok2);
        keyReturn.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordReturn_withKeywordName_andReturn_andComment() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "EmptyKeywordReturnComment." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("User Keyword");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordReturn keyReturn = uk.newReturn();

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        keyReturn.addCommentPart(cmTok1);
        keyReturn.addCommentPart(cmTok2);
        keyReturn.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordReturn_withoutKeywordName_andReturn_with3Values() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "KeywordReturn3ReturnsNoKeywordName." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordReturn keyReturn = uk.newReturn();

        RobotToken rTok1 = new RobotToken();
        rTok1.setText("${r1}");
        RobotToken rTok2 = new RobotToken();
        rTok2.setText("${r2}");
        RobotToken rTok3 = new RobotToken();
        rTok3.setText("${r3}");

        keyReturn.addReturnValue(rTok1);
        keyReturn.addReturnValue(rTok2);
        keyReturn.addReturnValue(rTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordReturn_withKeywordName_andReturn_with3Values() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "KeywordReturn3Returns." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("User Keyword");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordReturn keyReturn = uk.newReturn();

        RobotToken rTok1 = new RobotToken();
        rTok1.setText("${r1}");
        RobotToken rTok2 = new RobotToken();
        rTok2.setText("${r2}");
        RobotToken rTok3 = new RobotToken();
        rTok3.setText("${r3}");

        keyReturn.addReturnValue(rTok1);
        keyReturn.addReturnValue(rTok2);
        keyReturn.addReturnValue(rTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordReturn_withoutKeywordName_andReturn_with3Values_andComment()
            throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "KeywordReturn3ReturnsCommentNoKeywordName." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordReturn keyReturn = uk.newReturn();

        RobotToken rTok1 = new RobotToken();
        rTok1.setText("${r1}");
        RobotToken rTok2 = new RobotToken();
        rTok2.setText("${r2}");
        RobotToken rTok3 = new RobotToken();
        rTok3.setText("${r3}");

        keyReturn.addReturnValue(rTok1);
        keyReturn.addReturnValue(rTok2);
        keyReturn.addReturnValue(rTok3);

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        keyReturn.addCommentPart(cmTok1);
        keyReturn.addCommentPart(cmTok2);
        keyReturn.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordReturn_withKeywordName_andReturn_with3Values_andComment()
            throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "KeywordReturn3ReturnsComment." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("User Keyword");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordReturn keyReturn = uk.newReturn();

        RobotToken rTok1 = new RobotToken();
        rTok1.setText("${r1}");
        RobotToken rTok2 = new RobotToken();
        rTok2.setText("${r2}");
        RobotToken rTok3 = new RobotToken();
        rTok3.setText("${r3}");

        keyReturn.addReturnValue(rTok1);
        keyReturn.addReturnValue(rTok2);
        keyReturn.addReturnValue(rTok3);

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        keyReturn.addCommentPart(cmTok1);
        keyReturn.addCommentPart(cmTok2);
        keyReturn.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    public String getExtension() {
        return extension;
    }
}
