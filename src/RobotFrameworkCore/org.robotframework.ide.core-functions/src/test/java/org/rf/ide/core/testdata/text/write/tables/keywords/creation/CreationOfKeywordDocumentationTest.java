/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.keywords.creation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.IDocumentationHolder;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.presenter.DocumentationServiceHandler;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public class CreationOfKeywordDocumentationTest {

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordCaseDocumentation_withName_andThreeLinesOfDocumentation(
            final FileFormat format) throws Exception {
        // prepare
        final String filePath = convert("KeywordDocumentationWithThreeLinesCreation", format);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        final KeywordTable keywordTable = modelFile.getKeywordTable();

        final RobotToken keyName = new RobotToken();
        keyName.setText("User Keyword");
        final UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        final LocalSetting<UserKeyword> keyDoc = uk.newDocumentation(0);

        DocumentationServiceHandler.update(keyDoc.adaptTo(IDocumentationHolder.class),
                "doc me" + "\n" + "textZero" + "\n" + "textTwo");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withoutKeywordName_andDocumentationDecOnly(
            final FileFormat format) throws Exception {
        test_onlyKeyDoc_decIncluded("EmptyKeywordDocumentationNoKeywordName", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withKeywordName_andDocumentationDecOnly(
            final FileFormat format) throws Exception {
        test_onlyKeyDoc_decIncluded("EmptyKeywordDocumentation", "User Keyword", format);
    }

    private void test_onlyKeyDoc_decIncluded(final String fileNameWithoutExt, final String userKeywordName,
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
        uk.newDocumentation(0);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withoutKeywordName_andDocumentationAndCommentOnly(
            final FileFormat format) throws Exception {
        test_keyDoc_withCommentOnly("KeywordDocumentationNoKeywordNameAndComment", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withKeywordName_andDocumentationAndCommentOnly(
            final FileFormat format) throws Exception {
        test_keyDoc_withCommentOnly("KeywordDocumentationAndComment", "User Keyword", format);
    }

    private void test_keyDoc_withCommentOnly(final String fileNameWithoutExt, final String userKeywordName,
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

        final LocalSetting<UserKeyword> keyDoc = uk.newDocumentation(0);
        keyDoc.addCommentPart("cm1");
        keyDoc.addCommentPart("cm2");
        keyDoc.addCommentPart("cm3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withoutKeywordName_andDocumentationAnd3Words(
            final FileFormat format) throws Exception {
        test_docOnlyWith3Words("KeywordDocumentationNoKeywordNameAnd3WordsInText", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withKeywordName_andDocumentationAnd3Words(
            final FileFormat format) throws Exception {
        test_docOnlyWith3Words("KeywordDocumentationAnd3WordsInText", "User Keyword", format);
    }

    private void test_docOnlyWith3Words(final String fileNameWithoutExt, final String userKeywordName,
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

        final LocalSetting<UserKeyword> keyDoc = uk.newDocumentation(0);
        keyDoc.addToken("w1");
        keyDoc.addToken("w2");
        keyDoc.addToken("w3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withoutKeywordName_andDocumentationAnd3WordsAndComment(
            final FileFormat format) throws Exception {
        test_keyDoc_withDoc3Words_andComment("KeywordDocumentationNoKeywordNameAnd3WordsInTextAndComment", "", format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withKeywordName_andDocumentationAnd3WordsAndComment(
            final FileFormat format) throws Exception {
        test_keyDoc_withDoc3Words_andComment("KeywordDocumentationAnd3WordsInTextAndComment", "User Keyword", format);
    }

    private void test_keyDoc_withDoc3Words_andComment(final String fileNameWithoutExt, final String userKeywordName,
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

        final LocalSetting<UserKeyword> keyDoc = uk.newDocumentation(0);
        keyDoc.addToken("w1");
        keyDoc.addToken("w2");
        keyDoc.addToken("w3");
        keyDoc.addCommentPart("cm1");
        keyDoc.addCommentPart("cm2");
        keyDoc.addCommentPart("cm3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    private String convert(final String fileName, final FileFormat format) {
        return "keywords/setting/documentation/new/" + fileName + "." + format.getExtension();
    }
}
