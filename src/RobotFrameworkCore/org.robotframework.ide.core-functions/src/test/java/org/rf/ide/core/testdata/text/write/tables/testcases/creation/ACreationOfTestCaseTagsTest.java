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
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public abstract class ACreationOfTestCaseTagsTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "testCases//setting//tags//new//";

    private final String extension;

    public ACreationOfTestCaseTagsTest(final String extension) {
        this.extension = extension;
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
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileNameWithoutExt + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);
        test.newTags();

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
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileNameWithoutExt + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);
        TestCaseTags testTags = test.newTags();

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
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
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileNameWithoutExt + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);
        TestCaseTags testTags = test.newTags();

        RobotToken tagOne = new RobotToken();
        tagOne.setText("tag1");
        RobotToken tagTwo = new RobotToken();
        tagTwo.setText("tag2");
        RobotToken tagThree = new RobotToken();
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
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileNameWithoutExt + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeTestCaseTableSection();
        TestCaseTable testCaseTable = modelFile.getTestCaseTable();

        RobotToken testName = new RobotToken();
        testName.setText(userTestName);
        TestCase test = new TestCase(testName);
        testCaseTable.addTest(test);
        TestCaseTags testTags = test.newTags();

        RobotToken tagOne = new RobotToken();
        tagOne.setText("tag1");
        RobotToken tagTwo = new RobotToken();
        tagTwo.setText("tag2");
        RobotToken tagThree = new RobotToken();
        tagThree.setText("tag3");
        testTags.addTag(tagOne);
        testTags.addTag(tagTwo);
        testTags.addTag(tagThree);

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        testTags.addCommentPart(cmTok1);
        testTags.addCommentPart(cmTok2);
        testTags.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    public String getExtension() {
        return extension;
    }
}
