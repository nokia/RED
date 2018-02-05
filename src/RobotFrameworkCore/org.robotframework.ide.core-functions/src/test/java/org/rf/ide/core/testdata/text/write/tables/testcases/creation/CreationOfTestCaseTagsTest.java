/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.testcases.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTags;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;

public class CreationOfTestCaseTagsTest extends RobotFormatParameterizedTest {

    public CreationOfTestCaseTagsTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTags_withoutTestName_andTagsDecOnly() throws Exception {
        test_tagsDecOnly("EmptyTestCaseTagsNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTags_withTestName_andTagsDecOnly() throws Exception {
        test_tagsDecOnly("EmptyTestCaseTags", "TestCase");
    }

    private void test_tagsDecOnly(final String fileNameWithoutExt, final String userTestName) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        final TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);
        test.newTags(0);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTags_withoutTestName_andTags_andComment() throws Exception {
        test_tagsDec_andComment("EmptyTestCaseTagsCommentNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTags_withTestName_andTags_andComment() throws Exception {
        test_tagsDec_andComment("EmptyTestCaseTagsComment", "TestCase");
    }

    private void test_tagsDec_andComment(final String fileNameWithoutExt, final String userTestName) throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        final TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);
        final TestCaseTags testTags = test.newTags(0);

        final RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        final RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        final RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        testTags.addCommentPart(cmTok1);
        testTags.addCommentPart(cmTok2);
        testTags.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTags_withoutTestName_andTags_and3Tags() throws Exception {
        test_tags_withTagsAnd3Tags("TestCaseTagsAnd3TagsNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTags_withTestName_andTags_and3Tags() throws Exception {
        test_tags_withTagsAnd3Tags("TestCaseTagsAnd3Tags", "TestCase");
    }

    private void test_tags_withTagsAnd3Tags(final String fileNameWithoutExt, final String userTestName)
            throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        final TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);
        final TestCaseTags testTags = test.newTags(0);

        final RobotToken tagOne = new RobotToken();
        tagOne.setText("tag1");
        final RobotToken tagTwo = new RobotToken();
        tagTwo.setText("tag2");
        final RobotToken tagThree = new RobotToken();
        tagThree.setText("tag3");
        testTags.addTag(tagOne);
        testTags.addTag(tagTwo);
        testTags.addTag(tagThree);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTags_withoutTestName_andTags_and3Tags_andComment()
            throws Exception {
        test_tags_with3Tags_andComment("TestCaseTagsAnd3TagsCommentNoTestName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateTestCaseTags_withTestName_andTags_and3Tags_andComment() throws Exception {
        test_tags_with3Tags_andComment("TestCaseTagsAnd3TagsComment", "TestCase");
    }

    private void test_tags_with3Tags_andComment(final String fileNameWithoutExt, final String userTestName)
            throws Exception {
        // prepare
        final String filePath = convert(fileNameWithoutExt);
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        final TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        final RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        final TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);
        final TestCaseTags testTags = test.newTags(0);

        final RobotToken tagOne = new RobotToken();
        tagOne.setText("tag1");
        final RobotToken tagTwo = new RobotToken();
        tagTwo.setText("tag2");
        final RobotToken tagThree = new RobotToken();
        tagThree.setText("tag3");
        testTags.addTag(tagOne);
        testTags.addTag(tagTwo);
        testTags.addTag(tagThree);

        final RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        final RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        final RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        testTags.addCommentPart(cmTok1);
        testTags.addCommentPart(cmTok2);
        testTags.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    private String convert(final String fileName) {
        return "testCases/setting/tags/new/" + fileName + "." + getExtension();
    }
}
