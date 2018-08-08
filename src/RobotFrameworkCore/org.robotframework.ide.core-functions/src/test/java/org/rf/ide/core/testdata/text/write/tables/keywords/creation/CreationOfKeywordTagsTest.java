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

public class CreationOfKeywordTagsTest extends RobotFormatParameterizedTest {

    public CreationOfKeywordTagsTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTags_withoutKeywordName_andTagsDecOnly() throws Exception {
        test_tagsDecOnly("EmptyKeywordTagsNoKeywordName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTags_withKeywordName_andTagsDecOnly() throws Exception {
        test_tagsDecOnly("EmptyKeywordTags", "User Keyword");
    }

    private void test_tagsDecOnly(final String fileNameWithoutExt, final String userKeywordName) throws Exception {
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
        uk.newTags(0);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTags_withoutKeywordName_andTags_andComment() throws Exception {
        test_tagsDec_andComment("EmptyKeywordTagsCommentNoKeywordName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTags_withKeywordName_andTags_andComment() throws Exception {
        test_tagsDec_andComment("EmptyKeywordTagsComment", "User Keyword");
    }

    private void test_tagsDec_andComment(final String fileNameWithoutExt, final String userKeywordName)
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

        final LocalSetting<UserKeyword> keyTags = uk.newTags(0);
        keyTags.addCommentPart("cm1");
        keyTags.addCommentPart("cm2");
        keyTags.addCommentPart("cm3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTags_withoutKeywordName_andTags_and3Tags() throws Exception {
        test_tags_withTagsAnd3Tags("KeywordTagsAnd3TagsNoKeywordName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTags_withKeywordName_andTags_and3Tags() throws Exception {
        test_tags_withTagsAnd3Tags("KeywordTagsAnd3Tags", "User Keyword");
    }

    private void test_tags_withTagsAnd3Tags(final String fileNameWithoutExt, final String userKeywordName)
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

        final LocalSetting<UserKeyword> keyTags = uk.newTags(0);
        keyTags.addToken("tag1");
        keyTags.addToken("tag2");
        keyTags.addToken("tag3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTags_withoutKeywordName_andTags_and3Tags_andComment()
            throws Exception {
        test_tags_with3Tags_andComment("KeywordTagsAnd3TagsCommentNoKeywordName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTags_withKeywordName_andTags_and3Tags_andComment()
            throws Exception {
        test_tags_with3Tags_andComment("KeywordTagsAnd3TagsComment", "User Keyword");
    }

    private void test_tags_with3Tags_andComment(final String fileNameWithoutExt, final String userKeywordName)
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

        final LocalSetting<UserKeyword> keyTags = uk.newTags(0);
        keyTags.addToken("tag1");
        keyTags.addToken("tag2");
        keyTags.addToken("tag3");
        keyTags.addCommentPart("cm1");
        keyTags.addCommentPart("cm2");
        keyTags.addCommentPart("cm3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    private String convert(final String fileName) {
        return "keywords/setting/tags/new/" + fileName + "." + getExtension();
    }
}
