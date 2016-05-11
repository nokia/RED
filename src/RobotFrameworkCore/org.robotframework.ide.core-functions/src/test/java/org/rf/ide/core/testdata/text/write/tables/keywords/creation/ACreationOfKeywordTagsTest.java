/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.keywords.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTags;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public abstract class ACreationOfKeywordTagsTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "keywords//setting//tags//new//";

    private final String extension;

    public ACreationOfKeywordTagsTest(final String extension) {
        this.extension = extension;
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTags_withoutKeywordName_andTagsDecOnly() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "EmptyKeywordTagsNoKeywordName." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        uk.newTags();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTags_withKeywordName_andTagsDecOnly() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "EmptyKeywordTags." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("User Keyword");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        uk.newTags();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTags_withoutKeywordName_andTags_andComment() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "EmptyKeywordTagsCommentNoKeywordName." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordTags keyTags = uk.newTags();

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        keyTags.addCommentPart(cmTok1);
        keyTags.addCommentPart(cmTok2);
        keyTags.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTags_withKeywordName_andTags_andComment() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "EmptyKeywordTagsComment." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("User Keyword");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordTags keyTags = uk.newTags();

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        keyTags.addCommentPart(cmTok1);
        keyTags.addCommentPart(cmTok2);
        keyTags.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTags_withoutKeywordName_andTags_and3Tags() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "KeywordTagsAnd3TagsNoKeywordName." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordTags keyTags = uk.newTags();

        RobotToken tagOne = new RobotToken();
        tagOne.setText("tag1");
        RobotToken tagTwo = new RobotToken();
        tagTwo.setText("tag2");
        RobotToken tagThree = new RobotToken();
        tagThree.setText("tag3");
        keyTags.addTag(tagOne);
        keyTags.addTag(tagTwo);
        keyTags.addTag(tagThree);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTags_withKeywordName_andTags_and3Tags() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "KeywordTagsAnd3Tags." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("User Keyword");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordTags keyTags = uk.newTags();

        RobotToken tagOne = new RobotToken();
        tagOne.setText("tag1");
        RobotToken tagTwo = new RobotToken();
        tagTwo.setText("tag2");
        RobotToken tagThree = new RobotToken();
        tagThree.setText("tag3");
        keyTags.addTag(tagOne);
        keyTags.addTag(tagTwo);
        keyTags.addTag(tagThree);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTags_withoutKeywordName_andTags_and3Tags_andComment()
            throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "KeywordTagsAnd3TagsCommentNoKeywordName." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordTags keyTags = uk.newTags();

        RobotToken tagOne = new RobotToken();
        tagOne.setText("tag1");
        RobotToken tagTwo = new RobotToken();
        tagTwo.setText("tag2");
        RobotToken tagThree = new RobotToken();
        tagThree.setText("tag3");
        keyTags.addTag(tagOne);
        keyTags.addTag(tagTwo);
        keyTags.addTag(tagThree);

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        keyTags.addCommentPart(cmTok1);
        keyTags.addCommentPart(cmTok2);
        keyTags.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTags_withKeywordName_andTags_and3Tags_andComment()
            throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "KeywordTagsAnd3TagsComment." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("User Keyword");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordTags keyTags = uk.newTags();

        RobotToken tagOne = new RobotToken();
        tagOne.setText("tag1");
        RobotToken tagTwo = new RobotToken();
        tagTwo.setText("tag2");
        RobotToken tagThree = new RobotToken();
        tagThree.setText("tag3");
        keyTags.addTag(tagOne);
        keyTags.addTag(tagTwo);
        keyTags.addTag(tagThree);

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        keyTags.addCommentPart(cmTok1);
        keyTags.addCommentPart(cmTok2);
        keyTags.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    public String getExtension() {
        return extension;
    }
}
