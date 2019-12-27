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

public class CreationOfKeywordTagsTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTags_withoutKeywordName_andTagsDecOnly(final FileFormat format)
            throws Exception {
        test_tagsDecOnly("EmptyKeywordTagsNoKeywordName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTags_withKeywordName_andTagsDecOnly(final FileFormat format)
            throws Exception {
        test_tagsDecOnly("EmptyKeywordTags", "User Keyword", format);
    }

    private void test_tagsDecOnly(final String fileNameWithoutExt, final String userKeywordName,
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
        uk.newTags(0);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTags_withoutKeywordName_andTags_andComment(final FileFormat format)
            throws Exception {
        test_tagsDec_andComment("EmptyKeywordTagsCommentNoKeywordName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTags_withKeywordName_andTags_andComment(final FileFormat format)
            throws Exception {
        test_tagsDec_andComment("EmptyKeywordTagsComment", "User Keyword", format);
    }

    private void test_tagsDec_andComment(final String fileNameWithoutExt, final String userKeywordName,
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

        final LocalSetting<UserKeyword> keyTags = uk.newTags(0);
        keyTags.addCommentPart("cm1");
        keyTags.addCommentPart("cm2");
        keyTags.addCommentPart("cm3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTags_withoutKeywordName_andTags_and3Tags(final FileFormat format)
            throws Exception {
        test_tags_withTagsAnd3Tags("KeywordTagsAnd3TagsNoKeywordName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTags_withKeywordName_andTags_and3Tags(final FileFormat format)
            throws Exception {
        test_tags_withTagsAnd3Tags("KeywordTagsAnd3Tags", "User Keyword", format);
    }

    private void test_tags_withTagsAnd3Tags(final String fileNameWithoutExt, final String userKeywordName,
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

        final LocalSetting<UserKeyword> keyTags = uk.newTags(0);
        keyTags.addToken("tag1");
        keyTags.addToken("tag2");
        keyTags.addToken("tag3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTags_withoutKeywordName_andTags_and3Tags_andComment(
            final FileFormat format) throws Exception {
        test_tags_with3Tags_andComment("KeywordTagsAnd3TagsCommentNoKeywordName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordTags_withKeywordName_andTags_and3Tags_andComment(
            final FileFormat format) throws Exception {
        test_tags_with3Tags_andComment("KeywordTagsAnd3TagsComment", "User Keyword", format);
    }

    private void test_tags_with3Tags_andComment(final String fileNameWithoutExt, final String userKeywordName,
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

    private String convert(final String fileName, final FileFormat format) {
        return "keywords/setting/tags/new/" + fileName + "." + format.getExtension();
    }
}
