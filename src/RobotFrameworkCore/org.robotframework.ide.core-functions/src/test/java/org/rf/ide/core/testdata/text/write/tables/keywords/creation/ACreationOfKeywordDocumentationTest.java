/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.keywords.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.keywords.KeywordDocumentation;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public abstract class ACreationOfKeywordDocumentationTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "keywords//setting//documentation//new//";

    private final String extension;

    public ACreationOfKeywordDocumentationTest(final String extension) {
        this.extension = extension;
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withoutKeywordName_andDocumentationDecOnly()
            throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "EmptyKeywordDocumentationNoKeywordName." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        uk.newDocumentation();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withKeywordName_andDocumentationDecOnly()
            throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "EmptyKeywordDocumentation." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("User Keyword");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        uk.newDocumentation();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withoutKeywordName_andDocumentationAndCommentOnly()
            throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "KeywordDocumentationNoKeywordNameAndComment."
                + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordDocumentation keyDoc = uk.newDocumentation();

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        keyDoc.addCommentPart(cmTok1);
        keyDoc.addCommentPart(cmTok2);
        keyDoc.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withKeywordName_andDocumentationAndCommentOnly()
            throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "KeywordDocumentationAndComment." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("User Keyword");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordDocumentation keyDoc = uk.newDocumentation();

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        keyDoc.addCommentPart(cmTok1);
        keyDoc.addCommentPart(cmTok2);
        keyDoc.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withoutKeywordName_andDocumentationAnd3Words()
            throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "KeywordDocumentationNoKeywordNameAnd3WordsInText."
                + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordDocumentation keyDoc = uk.newDocumentation();
        RobotToken wr1 = new RobotToken();
        wr1.setText("w1");
        RobotToken wr2 = new RobotToken();
        wr2.setText("w2");
        RobotToken wr3 = new RobotToken();
        wr3.setText("w3");

        keyDoc.addDocumentationText(wr1);
        keyDoc.addDocumentationText(wr2);
        keyDoc.addDocumentationText(wr3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withKeywordName_andDocumentationAnd3Words()
            throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "KeywordDocumentationAnd3WordsInText." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("User Keyword");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordDocumentation keyDoc = uk.newDocumentation();
        RobotToken wr1 = new RobotToken();
        wr1.setText("w1");
        RobotToken wr2 = new RobotToken();
        wr2.setText("w2");
        RobotToken wr3 = new RobotToken();
        wr3.setText("w3");

        keyDoc.addDocumentationText(wr1);
        keyDoc.addDocumentationText(wr2);
        keyDoc.addDocumentationText(wr3);
        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withoutKeywordName_andDocumentationAnd3WordsAndComment()
            throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "KeywordDocumentationNoKeywordNameAnd3WordsInTextAndComment."
                + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordDocumentation keyDoc = uk.newDocumentation();
        RobotToken wr1 = new RobotToken();
        wr1.setText("w1");
        RobotToken wr2 = new RobotToken();
        wr2.setText("w2");
        RobotToken wr3 = new RobotToken();
        wr3.setText("w3");

        keyDoc.addDocumentationText(wr1);
        keyDoc.addDocumentationText(wr2);
        keyDoc.addDocumentationText(wr3);

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        keyDoc.addCommentPart(cmTok1);
        keyDoc.addCommentPart(cmTok2);
        keyDoc.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withKeywordName_andDocumentationAnd3WordsAndComment()
            throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "KeywordDocumentationAnd3WordsInTextAndComment."
                + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("User Keyword");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordDocumentation keyDoc = uk.newDocumentation();
        RobotToken wr1 = new RobotToken();
        wr1.setText("w1");
        RobotToken wr2 = new RobotToken();
        wr2.setText("w2");
        RobotToken wr3 = new RobotToken();
        wr3.setText("w3");

        keyDoc.addDocumentationText(wr1);
        keyDoc.addDocumentationText(wr2);
        keyDoc.addDocumentationText(wr3);

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        keyDoc.addCommentPart(cmTok1);
        keyDoc.addCommentPart(cmTok2);
        keyDoc.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    public String getExtension() {
        return extension;
    }
}
