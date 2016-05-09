/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.keywords.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.keywords.KeywordArguments;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public abstract class ACreationOfKeywordArgumentsTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "keywords//setting//arguments//new//";

    private final String extension;

    public ACreationOfKeywordArgumentsTest(final String extension) {
        this.extension = extension;
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordHeader_withoutKeywordName_andArgumentsDecOnly() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "EmptyKeywordArgumentsNoKeywordName." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        uk.newArguments();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordHeader_withKeywordName_andArgumentsDecOnly() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "EmptyKeywordArguments." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("User Keyword");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        uk.newArguments();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordHeader_withoutKeywordName_andArgumentsDec_andCommentsOnly()
            throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "KeywordArgumentsWithCommentAndNoKeywordName."
                + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordArguments newArguments = uk.newArguments();

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        newArguments.addCommentPart(cmTok1);
        newArguments.addCommentPart(cmTok2);
        newArguments.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordHeader_withKeywordName_andArgumentsDec_andCommentsOnly()
            throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "KeywordArgumentsWithCommentOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("User Keyword");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordArguments newArguments = uk.newArguments();

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        newArguments.addCommentPart(cmTok1);
        newArguments.addCommentPart(cmTok2);
        newArguments.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordHeader_withoutKeywordName_andArgumentsDec_and3ArgsOnly()
            throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "KeywordArgumentsWith3ArgsAndNoKeywordName." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordArguments newArguments = uk.newArguments();

        RobotToken argTok1 = new RobotToken();
        argTok1.setText("${arg1}");
        RobotToken argTok2 = new RobotToken();
        argTok2.setText("${arg2}");
        RobotToken argTok3 = new RobotToken();
        argTok3.setText("${arg3}");

        newArguments.addArgument(argTok1);
        newArguments.addArgument(argTok2);
        newArguments.addArgument(argTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordHeader_withKeywordName_andArgumentsDec_and3ArgsOnly()
            throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "KeywordArgumentsWith3ArgsOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("User Keyword");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordArguments newArguments = uk.newArguments();

        RobotToken argTok1 = new RobotToken();
        argTok1.setText("${arg1}");
        RobotToken argTok2 = new RobotToken();
        argTok2.setText("${arg2}");
        RobotToken argTok3 = new RobotToken();
        argTok3.setText("${arg3}");

        newArguments.addArgument(argTok1);
        newArguments.addArgument(argTok2);
        newArguments.addArgument(argTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordHeader_withoutKeywordName_andArgumentsDec_and3Args_andCommentOnly()
            throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "KeywordArgumentsWith3ArgsCommentAndNoKeywordName."
                + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordArguments newArguments = uk.newArguments();

        RobotToken argTok1 = new RobotToken();
        argTok1.setText("${arg1}");
        RobotToken argTok2 = new RobotToken();
        argTok2.setText("${arg2}");
        RobotToken argTok3 = new RobotToken();
        argTok3.setText("${arg3}");

        newArguments.addArgument(argTok1);
        newArguments.addArgument(argTok2);
        newArguments.addArgument(argTok3);

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        newArguments.addCommentPart(cmTok1);
        newArguments.addCommentPart(cmTok2);
        newArguments.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordHeader_withKeywordName_andArgumentsDec_and3Args_andCommentOnly()
            throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "KeywordArgumentsWith3ArgsCommentOnly." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("User Keyword");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordArguments newArguments = uk.newArguments();

        RobotToken argTok1 = new RobotToken();
        argTok1.setText("${arg1}");
        RobotToken argTok2 = new RobotToken();
        argTok2.setText("${arg2}");
        RobotToken argTok3 = new RobotToken();
        argTok3.setText("${arg3}");

        newArguments.addArgument(argTok1);
        newArguments.addArgument(argTok2);
        newArguments.addArgument(argTok3);

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        newArguments.addCommentPart(cmTok1);
        newArguments.addCommentPart(cmTok2);
        newArguments.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    public String getExtension() {
        return extension;
    }
}
