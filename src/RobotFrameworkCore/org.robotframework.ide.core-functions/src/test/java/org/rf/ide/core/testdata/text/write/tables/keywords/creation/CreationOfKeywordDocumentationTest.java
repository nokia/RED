/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.keywords.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.IDocumentationHolder;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.presenter.DocumentationServiceHandler;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;

public class CreationOfKeywordDocumentationTest extends RobotFormatParameterizedTest {

    public CreationOfKeywordDocumentationTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordCaseDocumentation_withName_andThreeLinesOfDocumentation()
            throws Exception {
        // prepare
        final String filePath = convert("KeywordDocumentationWithThreeLinesCreation");
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

    @Test
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withoutKeywordName_andDocumentationDecOnly()
            throws Exception {
        test_onlyKeyDoc_decIncluded("EmptyKeywordDocumentationNoKeywordName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withKeywordName_andDocumentationDecOnly()
            throws Exception {
        test_onlyKeyDoc_decIncluded("EmptyKeywordDocumentation", "User Keyword");
    }

    private void test_onlyKeyDoc_decIncluded(final String fileNameWithoutExt, final String userKeywordName)
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
        uk.newDocumentation(0);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withoutKeywordName_andDocumentationAndCommentOnly()
            throws Exception {
        test_keyDoc_withCommentOnly("KeywordDocumentationNoKeywordNameAndComment", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withKeywordName_andDocumentationAndCommentOnly()
            throws Exception {
        test_keyDoc_withCommentOnly("KeywordDocumentationAndComment", "User Keyword");
    }

    private void test_keyDoc_withCommentOnly(final String fileNameWithoutExt, final String userKeywordName)
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

        final LocalSetting<UserKeyword> keyDoc = uk.newDocumentation(0);
        keyDoc.addCommentPart("cm1");
        keyDoc.addCommentPart("cm2");
        keyDoc.addCommentPart("cm3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withoutKeywordName_andDocumentationAnd3Words()
            throws Exception {
        test_docOnlyWith3Words("KeywordDocumentationNoKeywordNameAnd3WordsInText", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withKeywordName_andDocumentationAnd3Words()
            throws Exception {
        test_docOnlyWith3Words("KeywordDocumentationAnd3WordsInText", "User Keyword");
    }

    private void test_docOnlyWith3Words(final String fileNameWithoutExt, final String userKeywordName)
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

        final LocalSetting<UserKeyword> keyDoc = uk.newDocumentation(0);
        keyDoc.addToken("w1");
        keyDoc.addToken("w2");
        keyDoc.addToken("w3");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withoutKeywordName_andDocumentationAnd3WordsAndComment()
            throws Exception {
        test_keyDoc_withDoc3Words_andComment("KeywordDocumentationNoKeywordNameAnd3WordsInTextAndComment", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withKeywordName_andDocumentationAnd3WordsAndComment()
            throws Exception {
        test_keyDoc_withDoc3Words_andComment("KeywordDocumentationAnd3WordsInTextAndComment", "User Keyword");
    }

    private void test_keyDoc_withDoc3Words_andComment(final String fileNameWithoutExt, final String userKeywordName)
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

    private String convert(final String fileName) {
        return "keywords/setting/documentation/new/" + fileName + "." + getExtension();
    }
}
