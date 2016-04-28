/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.context;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rf.ide.core.testdata.RobotParser;
import org.rf.ide.core.testdata.model.RobotFile;


@SuppressWarnings("PMD")
public class RobotDebugExecutionContextTest {
    
    private static RobotParser parser;

    private RobotDebugExecutionContext debugExecutionContext;

    private int linesCounter = 0;

    private int[] test1_lines = new int[] { 7, 8, 10, 11, 28, 29, 30, 32, 33, 3, 4, 5, 8, 9, 12, 35, 36, 39, 40, 37,
            13, 14, 6, 7, 8, 3, 4, 5, 8, 9, 9, 12, 13, 14, 3, 4, 5, 8, 9, 15, 18, 19, 15, 12, 13, 14, 3, 4, 5, 8, 9,
            15, 18, 19, 16, 19, 20, 42, 28, 29, 30, 32, 33, 3, 4, 5, 8, 9, 43, 35, 36, 39, 40, 37, 21 };

    private int[] test2_lines = new int[] { 6, 16, 9, 18, 22, 12, 12, 13, 14, 17, 13, 23, 19 };

    private int[] test3_lines = new int[] { 6, 6, 7, 8, 6, 7, 8, 6, 7, 8, 9, 10, 17, 18, 18, 20, 18, 20, 18, 20, 21,
            11, 12, 20, 20, 21, 22, 23, 20, 21, 22, 23, 13 };

    private int[] test4_lines = new int[] { 4, 21, 23, 26, 28, 7, 12, 26, 28 };

    private int[] test5_lines = new int[] { 3, 20, 21, 26, 27, 8, 9, 10, 4, 23, 24, 13, 27, 15, 16, 14, 29 };

    private int[] test6_lines = new int[] { 2, 12, 13, 7, 8, 3, 15, 16 };

    private int[] test7_lines = new int[] { 6, 30, 7, 8, 13, 9 };

    private int[] test8_lines = new int[] { 7, 8, 3, 9, 26, 27, 10, 8, 9 };

    private int[] test9_lines = new int[] { 6, 6, 7, 8, 15, 16, 16, 18, 19, 9, 11 };
    
    private int[] test11_lines = new int[] { 2, 10, 11, 11, 12, 13, 6 };

    @BeforeClass
    public static void init() {
        parser = RobotModelTestProvider.getParser();
    }
    
    @Before
    public void setUp() {
        linesCounter = 0;
        debugExecutionContext = new RobotDebugExecutionContext();
    }

    @Test
    public void test_MultipleUserKeywordsAndResources()
            throws URISyntaxException {
        RobotFile modelFile = RobotModelTestProvider.getModelFile("test_ExeContext_1.robot", parser);

        debugExecutionContext.startSuite(modelFile.getParent(), parser);

        debugExecutionContext.startTest("test a");

            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("${scalar}"));
            checkKeywordLine1();
            debugExecutionContext.endKeyword("Keyword");
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("${a}"));
            checkKeywordLine1();
            debugExecutionContext.endKeyword("Keyword");
            startBuiltInLogKeyword1();
            startKey1Keyword();
            startKey3Keyword();
            startBuiltInLogKeyword1();
    
            debugExecutionContext.startKeyword("resource1.MyLog2", "Keyword", Arrays.asList(""));
            checkKeywordLine1();
            startBuiltInLogKeyword1();
            startBuiltInLogKeyword1();
            startMyLogKeyword();
            startTestKKeyword();
            debugExecutionContext.endKeyword("Keyword");
    
            startTestKKeyword();
            startBuiltInLogKeyword1();

        debugExecutionContext.endTest();

        debugExecutionContext.startTest("test b");

            startBuiltInLogKeyword1();
            debugExecutionContext.startKeyword("key5", "Keyword", Arrays.asList(""));
            checkKeywordLine1();
            startKey1Keyword();
            startKey3Keyword();
            debugExecutionContext.endKeyword("Keyword");
            startBuiltInLogKeyword1();

        debugExecutionContext.endTest();
    }


    @Test
    public void test_MultipleResources() throws URISyntaxException {
        RobotFile modelFile = RobotModelTestProvider.getModelFile("test_ExeContext_2.robot", parser);

        debugExecutionContext.startSuite(modelFile.getParent(), parser);

        debugExecutionContext.startTest("test a");
            debugExecutionContext.startKeyword("key1", "Keyword", Arrays.asList(""));
            checkKeywordLine2();
                startBuiltInLogKeyword2();
            debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endTest();

        debugExecutionContext.startTest("test b");

        debugExecutionContext.startKeyword("key2", "Keyword", Arrays.asList(""));
        checkKeywordLine2();
            debugExecutionContext.startKeyword("resource1.Keyword1", "Keyword", Arrays.asList(""));
            checkKeywordLine2();
            debugExecutionContext.startKeyword("resource2.Keyword2", "Keyword", Arrays.asList(""));
            checkKeywordLine2();
            debugExecutionContext.startKeyword("resource3.Keyword3", "Keyword", Arrays.asList(""));
            checkKeywordLine2();
                startBuiltInLogKeyword2();
                startBuiltInLogKeyword2();
                debugExecutionContext.startKeyword("Keyword4", "Keyword", Arrays.asList(""));
                checkKeywordLine2();
                    startBuiltInLogKeyword2();
                debugExecutionContext.endKeyword("Keyword");
            debugExecutionContext.endKeyword("Keyword");
            startBuiltInLogKeyword2();
            debugExecutionContext.endKeyword("Keyword");
            startBuiltInLogKeyword2();
            debugExecutionContext.endKeyword("Keyword");
            startBuiltInLogKeyword2();
        debugExecutionContext.endKeyword("Keyword");

        debugExecutionContext.endTest();

    }

    @Test
    public void test_ForLoop() throws URISyntaxException {
        RobotFile modelFile = RobotModelTestProvider.getModelFile("test_ExeContext_3.robot", parser);

        debugExecutionContext.startSuite(modelFile.getParent(), parser);
        debugExecutionContext.startTest("test a");

        debugExecutionContext.startKeyword("${i} IN [ @{t} ]", "Test For", Arrays.asList(""));
        checkKeywordLine3();
        debugExecutionContext.startKeyword("${i} = 1", "Test Foritem", Arrays.asList(""));
        checkKeywordLine3();
        startBuiltInLogKeyword3();
        startBuiltInLogKeyword3();
        debugExecutionContext.endKeyword("Test Foritem");
        debugExecutionContext.startKeyword("${i} = 2", "Test Foritem", Arrays.asList(""));
        checkKeywordLine3();
        startBuiltInLogKeyword3();
        startBuiltInLogKeyword3();
        debugExecutionContext.endKeyword("Test Foritem");
        debugExecutionContext.startKeyword("${i} = 3", "Test Foritem", Arrays.asList(""));
        checkKeywordLine3();
        startBuiltInLogKeyword3();
        startBuiltInLogKeyword3();
        debugExecutionContext.endKeyword("Test Foritem");
        debugExecutionContext.endKeyword("Test For");

        debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("1234"));
        checkKeywordLine3();
        debugExecutionContext.endKeyword("Keyword");

        debugExecutionContext.startKeyword("testFor", "Keyword", Arrays.asList(""));
        checkKeywordLine3();
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("1234"));
            checkKeywordLine3();
            debugExecutionContext.endKeyword("Keyword");
            debugExecutionContext.startKeyword("${i} IN [ @{t} ]", "Test For", Arrays.asList(""));
            checkKeywordLine3();
            debugExecutionContext.startKeyword("${i} = 1", "Test Foritem", Arrays.asList(""));
            checkKeywordLine3();
            startBuiltInLogKeyword3();
            debugExecutionContext.endKeyword("Test Foritem");
            debugExecutionContext.startKeyword("${i} = 2", "Test Foritem", Arrays.asList(""));
            checkKeywordLine3();
            startBuiltInLogKeyword3();
            debugExecutionContext.endKeyword("Test Foritem");
            debugExecutionContext.startKeyword("${i} = 3", "Test Foritem", Arrays.asList(""));
            checkKeywordLine3();
            startBuiltInLogKeyword3();
            debugExecutionContext.endKeyword("Test Foritem");
            debugExecutionContext.endKeyword("Test For");
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("1234"));
            checkKeywordLine3();
            debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endKeyword("Keyword");

        debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("1234"));
        checkKeywordLine3();
        debugExecutionContext.endKeyword("Keyword");

        debugExecutionContext.startKeyword("resource3.LoopKeyword", "Keyword", Arrays.asList(""));
        checkKeywordLine3();
            debugExecutionContext.startKeyword("${i} IN [ @{t} ]", "Test For", Arrays.asList(""));
            checkKeywordLine3();
            debugExecutionContext.startKeyword("${i} = 1", "Test Foritem", Arrays.asList(""));
            checkKeywordLine3();
            startBuiltInLogKeyword3();
            startBuiltInLogKeyword3();
            startBuiltInLogKeyword3();
            debugExecutionContext.endKeyword("Test Foritem");
            debugExecutionContext.startKeyword("${i} = 2", "Test Foritem", Arrays.asList(""));
            checkKeywordLine3();
            startBuiltInLogKeyword3();
            startBuiltInLogKeyword3();
            startBuiltInLogKeyword3();
            debugExecutionContext.endKeyword("Test Foritem");
            debugExecutionContext.endKeyword("Test For");
        debugExecutionContext.endKeyword("Keyword");

        debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("1234"));
        checkKeywordLine3();
        debugExecutionContext.endKeyword("Keyword");

        debugExecutionContext.endTest();
    }


    @Test
    public void test_Comments() throws URISyntaxException {
        RobotFile modelFile = RobotModelTestProvider.getModelFile("test_ExeContext_4.robot", parser);

        debugExecutionContext.startSuite(modelFile.getParent(), parser);

        debugExecutionContext.startTest("test a");
        debugExecutionContext.startKeyword("key1", "Keyword", Arrays.asList(""));
        checkKeywordLine4();
        startBuiltInLogKeyword4();
        debugExecutionContext.startKeyword("key2", "Keyword", Arrays.asList(""));
        checkKeywordLine4();
        startBuiltInLogKeyword4();
        startBuiltInLogKeyword4();
        debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endKeyword("Keyword");
        startBuiltInLogKeyword4();
        debugExecutionContext.endTest();

        debugExecutionContext.startTest("test b");
        debugExecutionContext.startKeyword("key2", "Keyword", Arrays.asList(""));
        checkKeywordLine4();
        startBuiltInLogKeyword4();
        startBuiltInLogKeyword4();
        debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endTest();
    }


    @Test
    public void test_SetupAndTeardownKeywords() throws URISyntaxException {
        RobotFile modelFile = RobotModelTestProvider.getModelFile("test_ExeContext_5.robot", parser);

        debugExecutionContext.startSuite(modelFile.getParent(), parser);

        debugExecutionContext.startTest("test5");
        debugExecutionContext.startKeyword("my_setup", "Test Setup", Arrays.asList(""));
        checkKeywordLine5();
            debugExecutionContext.startKeyword("BuiltIn.Log", "Test Setup", Arrays.asList("setup"));
            checkKeywordLine5();
            debugExecutionContext.endKeyword("Keyword");
            debugExecutionContext.startKeyword("resource1.SetupKeyword", "Test Setup", Arrays.asList(""));
            checkKeywordLine5();
                debugExecutionContext.startKeyword("BuiltIn.Log", "Test Setup", Arrays.asList("12345"));
                checkKeywordLine5();
                debugExecutionContext.endKeyword("Keyword");
                debugExecutionContext.startKeyword("BuiltIn.Log", "Test Setup", Arrays.asList("123"));
                checkKeywordLine5();
                debugExecutionContext.endKeyword("Keyword");
            debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endKeyword("Test Setup");

        debugExecutionContext.startKeyword("BuiltIn.Should Be True", "Keyword", Arrays.asList("True"));
        checkKeywordLine5();
        debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("123"));
        checkKeywordLine5();
        debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("2"));
        checkKeywordLine5();
        debugExecutionContext.endKeyword("Keyword");

        debugExecutionContext.startKeyword("my_teardown", "Test Teardown", Arrays.asList(""));
        checkKeywordLine5();
            debugExecutionContext.startKeyword("BuiltIn.Log", "Test Teardown", Arrays.asList("close"));
            checkKeywordLine5();
            debugExecutionContext.endKeyword("Keyword");
            debugExecutionContext.startKeyword("BuiltIn.Log", "Test Teardown", Arrays.asList("close2"));
            checkKeywordLine5();
            debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endKeyword("Test Teardown");
        debugExecutionContext.endTest();

        debugExecutionContext.startTest("test5_2");
        debugExecutionContext.startKeyword("testCaseSetup", "Test Setup", Arrays.asList(""));
        checkKeywordLine5();
            debugExecutionContext.startKeyword("BuiltIn.Log", "Test Setup", Arrays.asList("setup"));
            checkKeywordLine5();
            debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endKeyword("Test Setup");

        debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("1234"));
        checkKeywordLine5();
        debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("123"));
        checkKeywordLine5();
        debugExecutionContext.endKeyword("Keyword");

        debugExecutionContext.startKeyword("testCaseTeardown", "Test Teardown", Arrays.asList(""));
        checkKeywordLine5();
            debugExecutionContext.startKeyword("BuiltIn.Log", "Test Teardown", Arrays.asList("teardown"));
            checkKeywordLine5();
            debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endKeyword("Test Teardown");
        debugExecutionContext.endTest();
    }
    
    @Test
    public void test_SetupAndTeardownKeywordsWithNewTypes() throws URISyntaxException {
        RobotFile modelFile = RobotModelTestProvider.getModelFile("test_ExeContext_5.robot", parser);

        debugExecutionContext.startSuite(modelFile.getParent(), parser);

        debugExecutionContext.startTest("test5");
        debugExecutionContext.startKeyword("my_setup", "Setup", Arrays.asList(""));
        checkKeywordLine5();
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("setup"));
            checkKeywordLine5();
            debugExecutionContext.endKeyword("Keyword");
            debugExecutionContext.startKeyword("resource1.SetupKeyword", "Keyword", Arrays.asList(""));
            checkKeywordLine5();
                debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("12345"));
                checkKeywordLine5();
                debugExecutionContext.endKeyword("Keyword");
                debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("123"));
                checkKeywordLine5();
                debugExecutionContext.endKeyword("Keyword");
            debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endKeyword("Setup");

        debugExecutionContext.startKeyword("BuiltIn.Should Be True", "Keyword", Arrays.asList("True"));
        checkKeywordLine5();
        debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("123"));
        checkKeywordLine5();
        debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("2"));
        checkKeywordLine5();
        debugExecutionContext.endKeyword("Keyword");

        debugExecutionContext.endTest();
    }


    @Test
    public void test_SuiteSetupAndTeardownKeywords() throws URISyntaxException {
        RobotFile modelFile = RobotModelTestProvider.getModelFile("test_ExeContext_6.robot", parser);

        debugExecutionContext.startSuite(modelFile.getParent(), parser);

        debugExecutionContext.startKeyword("my_setup", "Suite Setup", Arrays.asList(""));
        checkKeywordLine6();
            debugExecutionContext.startKeyword("BuiltIn.Log", "Suite Setup", Arrays.asList("setup"));
            checkKeywordLine6();
            debugExecutionContext.endKeyword("Suite Setup");
            debugExecutionContext.startKeyword("BuiltIn.Log", "Suite Setup", Arrays.asList("setup2"));
            checkKeywordLine6();
            debugExecutionContext.endKeyword("Suite Setup");
        debugExecutionContext.endKeyword("Suite Setup");

        debugExecutionContext.startTest("test6");
        debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("123"));
        checkKeywordLine6();
        debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("2"));
        checkKeywordLine6();
        debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endTest();

        debugExecutionContext.startKeyword("my_teardown", "Suite Teardown", Arrays.asList(""));
        checkKeywordLine6();
            debugExecutionContext.startKeyword("BuiltIn.Log", "Suite Teardown", Arrays.asList("close"));
            checkKeywordLine6();
            debugExecutionContext.endKeyword("Suite Teardown");
            debugExecutionContext.startKeyword("BuiltIn.Log", "Suite Teardown", Arrays.asList("close2"));
            checkKeywordLine6();
            debugExecutionContext.endKeyword("Suite Teardown");
        debugExecutionContext.endKeyword("Suite Teardown");

    }
    
    @Test
    public void test_SuiteSetupAndTeardownKeywordsWithNewTypes() throws URISyntaxException {
        RobotFile modelFile = RobotModelTestProvider.getModelFile("test_ExeContext_6.robot", parser);

        debugExecutionContext.startSuite(modelFile.getParent(), parser);

        debugExecutionContext.startKeyword("my_setup", "Setup", Arrays.asList(""));
        checkKeywordLine6();
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("setup"));
            checkKeywordLine6();
            debugExecutionContext.endKeyword("Keyword");
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("setup2"));
            checkKeywordLine6();
            debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endKeyword("Setup");

        debugExecutionContext.startTest("test6");
        debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("123"));
        checkKeywordLine6();
        debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("2"));
        checkKeywordLine6();
        debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endTest();

        debugExecutionContext.startKeyword("my_teardown", "Teardown", Arrays.asList(""));
        checkKeywordLine6();
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("close"));
            checkKeywordLine6();
            debugExecutionContext.endKeyword("Keyword");
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("close2"));
            checkKeywordLine6();
            debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endKeyword("Teardown");

    }

    @Test
    public void test_VariableDeclarationAsKeyword() throws URISyntaxException {
        RobotFile modelFile = RobotModelTestProvider.getModelFile("test_ExeContext_7.robot", parser);

        debugExecutionContext.startSuite(modelFile.getParent(), parser);

        debugExecutionContext.startTest("test7");
        debugExecutionContext.startKeyword("${var} = resource1.KeywordReturnValue", "Keyword", Arrays.asList(""));
        checkKeywordLine7();
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("Return value"));
            checkKeywordLine7();
            debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("${var}"));
        checkKeywordLine7();
        debugExecutionContext.endKeyword("Keyword");

        debugExecutionContext.startKeyword("${var2} = SecondKeywordReturnValue", "Keyword", Arrays.asList(""));
        checkKeywordLine7();
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("Return value"));
            checkKeywordLine7();
            debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("${var2}"));
        checkKeywordLine7();
        debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endTest();

    }


    @Test
    public void test_ResourcesWithTheSameNames() throws URISyntaxException {
        RobotFile modelFile = RobotModelTestProvider.getModelFile("test_ExeContext_8.robot", parser);

        debugExecutionContext.startSuite(modelFile.getParent(), parser);

        debugExecutionContext.startTest("test8");

        debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("start"));
        checkKeywordLine8();
        debugExecutionContext.endKeyword("Keyword");

        debugExecutionContext.startKeyword("resource1.MyKeyword1", "Keyword", Arrays.asList(""));
        checkKeywordLine8();
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("some log"));
            checkKeywordLine8();
            debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endKeyword("Keyword");

        debugExecutionContext.startKeyword("resource1.SetupKeyword", "Keyword", Arrays.asList(""));
        checkKeywordLine8();
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("12345"));
            checkKeywordLine8();
            debugExecutionContext.endKeyword("Keyword");
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("123"));
            checkKeywordLine8();
            debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endKeyword("Keyword");

        debugExecutionContext.startKeyword("resource2.testN", "Keyword", Arrays.asList(""));
        checkKeywordLine8();
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("1234"));
            checkKeywordLine8();
            debugExecutionContext.endKeyword("Keyword");
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("1234"));
            checkKeywordLine8();
            debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endKeyword("Keyword");

        debugExecutionContext.endTest();
    }

    @Test
    public void test_ResourcesWithParametrizedPath() throws URISyntaxException {
        RobotFile modelFile = RobotModelTestProvider.getModelFile("test_ExeContext_10.robot", parser);

        debugExecutionContext.resourceImport(Paths.get(this.getClass().getResource("resources/resource1.robot").toURI()).toString());

        debugExecutionContext.startSuite(modelFile.getParent(), parser);
        
        debugExecutionContext.startTest("test8");

        debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("start"));
        checkKeywordLine8();
        debugExecutionContext.endKeyword("Keyword");

        debugExecutionContext.startKeyword("resource1.MyKeyword1", "Keyword", Arrays.asList(""));
        checkKeywordLine8();
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("some log"));
            checkKeywordLine8();
            debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endKeyword("Keyword");

        debugExecutionContext.startKeyword("resource1.SetupKeyword", "Keyword", Arrays.asList(""));
        checkKeywordLine8();
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("12345"));
            checkKeywordLine8();
            debugExecutionContext.endKeyword("Keyword");
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("123"));
            checkKeywordLine8();
            debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endKeyword("Keyword");

        debugExecutionContext.startKeyword("resource2.testN", "Keyword", Arrays.asList(""));
        checkKeywordLine8();
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("1234"));
            checkKeywordLine8();
            debugExecutionContext.endKeyword("Keyword");
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("1234"));
            checkKeywordLine8();
            debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endKeyword("Keyword");

        debugExecutionContext.endTest();
    }
    
    @Test
    public void test_ResourcesWithParametrizedPathAndImportAfterSuiteStart() throws URISyntaxException {
        RobotFile modelFile = RobotModelTestProvider.getModelFile("test_ExeContext_10.robot", parser);

        debugExecutionContext.startSuite(modelFile.getParent(), parser);
        
        debugExecutionContext.startTest("test8");

        debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("start"));
        checkKeywordLine8();
        debugExecutionContext.endKeyword("Keyword");
        
        debugExecutionContext.resourceImport(Paths.get(this.getClass().getResource("resources/resource1.robot").toURI()).toString());

        debugExecutionContext.startKeyword("resource1.MyKeyword1", "Keyword", Arrays.asList(""));
        checkKeywordLine8();
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("some log"));
            checkKeywordLine8();
            debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endKeyword("Keyword");

        debugExecutionContext.startKeyword("resource1.SetupKeyword", "Keyword", Arrays.asList(""));
        checkKeywordLine8();
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("12345"));
            checkKeywordLine8();
            debugExecutionContext.endKeyword("Keyword");
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("123"));
            checkKeywordLine8();
            debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endKeyword("Keyword");

        debugExecutionContext.startKeyword("resource2.testN", "Keyword", Arrays.asList(""));
        checkKeywordLine8();
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("1234"));
            checkKeywordLine8();
            debugExecutionContext.endKeyword("Keyword");
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("1234"));
            checkKeywordLine8();
            debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endKeyword("Keyword");

        debugExecutionContext.endTest();
    }
    
    @Test
    public void test_ForLoopWithUserKeyword() throws URISyntaxException {
        RobotFile modelFile = RobotModelTestProvider.getModelFile("test_ExeContext_9.robot", parser);

        debugExecutionContext.startSuite(modelFile.getParent(), parser);
        debugExecutionContext.startTest("test a");

        debugExecutionContext.startKeyword("${i} IN [ @{t} ]", "Test For", Arrays.asList(""));
        checkKeywordLine9();
            debugExecutionContext.startKeyword("${i} = 1", "Test Foritem", Arrays.asList(""));
            checkKeywordLine9();
            startBuiltInLogKeyword9();
            debugExecutionContext.startKeyword("testFor", "Test Foritem", Arrays.asList(""));
            checkKeywordLine9();
                startBuiltInLogKeyword9();
                debugExecutionContext.startKeyword("${i} IN [ @{t} ]", "Test For", Arrays.asList(""));
                checkKeywordLine9();
                debugExecutionContext.startKeyword("${i} = 1", "Test Foritem", Arrays.asList(""));
                checkKeywordLine9();
                    startBuiltInLogKeyword9();
                debugExecutionContext.endKeyword("Test Foritem");
                debugExecutionContext.endKeyword("Test For");
                startBuiltInLogKeyword9();
            debugExecutionContext.endKeyword("Test Foritem");
        startBuiltInLogKeyword9();
        debugExecutionContext.endKeyword("Test Foritem");
        debugExecutionContext.endKeyword("Test For");

        debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("end"));
        checkKeywordLine9();
        debugExecutionContext.endKeyword("Keyword");

        debugExecutionContext.endTest();
    }
    
    @Test
    public void test_ForLoopWithUserKeywordWithNewTypes() throws URISyntaxException {
        RobotFile modelFile = RobotModelTestProvider.getModelFile("test_ExeContext_9.robot", parser);

        debugExecutionContext.startSuite(modelFile.getParent(), parser);
        debugExecutionContext.startTest("test a");

        debugExecutionContext.startKeyword("${i} IN [ @{t} ]", "For", Arrays.asList(""));
        checkKeywordLine9();
            debugExecutionContext.startKeyword("${i} = 1", "For Item", Arrays.asList(""));
            checkKeywordLine9();
            startBuiltInLogKeyword9_newType();
            debugExecutionContext.startKeyword("testFor", "Keyword", Arrays.asList(""));
            checkKeywordLine9();
                startBuiltInLogKeyword9_newType();
                debugExecutionContext.startKeyword("${i} IN [ @{t} ]", "For", Arrays.asList(""));
                checkKeywordLine9();
                debugExecutionContext.startKeyword("${i} = 1", "For Item", Arrays.asList(""));
                checkKeywordLine9();
                    startBuiltInLogKeyword9_newType();
                debugExecutionContext.endKeyword("For Item");
                debugExecutionContext.endKeyword("For");
                startBuiltInLogKeyword9_newType();
            debugExecutionContext.endKeyword("Keyword");
        startBuiltInLogKeyword9_newType();
        debugExecutionContext.endKeyword("For Item");
        debugExecutionContext.endKeyword("For");

        debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("end"));
        checkKeywordLine9();
        debugExecutionContext.endKeyword("Keyword");

        debugExecutionContext.endTest();
    }
    
    @Test
    public void test_SuiteSetupWithForLoop() throws URISyntaxException {
        RobotFile modelFile = RobotModelTestProvider.getModelFile("test_ExeContext_11.robot", parser);

        debugExecutionContext.startSuite(modelFile.getParent(), parser);

        debugExecutionContext.startKeyword("my_setup", "Setup", Arrays.asList(""));
        checkKeywordLine11();
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("setup"));
            checkKeywordLine11();
            debugExecutionContext.endKeyword("Keyword");
            debugExecutionContext.startKeyword("${i} IN [ @{t} ]", "Suite For", Arrays.asList(""));
            checkKeywordLine11();
                debugExecutionContext.startKeyword("${i} = 1", "Suite Foritem", Arrays.asList(""));
                checkKeywordLine11();
                    debugExecutionContext.startKeyword("BuiltIn.Log", "Suite Foritem", Arrays.asList("${i}"));
                    checkKeywordLine11();
                    debugExecutionContext.endKeyword("Suite Foritem");
                    debugExecutionContext.startKeyword("BuiltIn.Log", "Suite Foritem", Arrays.asList("${i}"));
                    checkKeywordLine11();
                    debugExecutionContext.endKeyword("Suite Foritem");
                debugExecutionContext.endKeyword("Suite Foritem");
            debugExecutionContext.endKeyword("Suite For");
        debugExecutionContext.endKeyword("Setup");

        debugExecutionContext.startTest("test11");
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("123"));
            checkKeywordLine11();
            debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endTest();
    }
    
    @Test
    public void test_isSuiteSetupTeardownKeyword() throws URISyntaxException {
        RobotFile modelFile = RobotModelTestProvider.getModelFile("test_ExeContext_11.robot", parser);
        debugExecutionContext.startSuite(modelFile.getParent(), parser);
        
        assertFalse(debugExecutionContext.isSuiteSetupTeardownKeyword("Test Setup"));
        assertFalse(debugExecutionContext.isSuiteSetupTeardownKeyword("Test Teardown"));
        assertTrue(debugExecutionContext.isSuiteSetupTeardownKeyword("Suite Setup"));
        assertTrue(debugExecutionContext.isSuiteSetupTeardownKeyword("Setup"));
        assertTrue(debugExecutionContext.isSuiteSetupTeardownKeyword("Suite Teardown"));
        assertTrue(debugExecutionContext.isSuiteSetupTeardownKeyword("Teardown"));
        
        debugExecutionContext.startTest("test11");
        
        assertFalse(debugExecutionContext.isSuiteSetupTeardownKeyword("Setup"));
        assertFalse(debugExecutionContext.isSuiteSetupTeardownKeyword("Teardown"));
    }
    
    @Test
    public void test_isTestCaseTeardownKeyword() throws URISyntaxException {
        RobotFile modelFile = RobotModelTestProvider.getModelFile("test_ExeContext_11.robot", parser);
        debugExecutionContext.startSuite(modelFile.getParent(), parser);
        
        assertFalse(debugExecutionContext.isTestCaseTeardownKeyword("Suite Teardown"));
        assertFalse(debugExecutionContext.isTestCaseTeardownKeyword("Teardown"));
        
        debugExecutionContext.startTest("test11");
        
        assertTrue(debugExecutionContext.isTestCaseTeardownKeyword("Test Teardown"));
        assertTrue(debugExecutionContext.isTestCaseTeardownKeyword("Teardown"));
    }


    private void startBuiltInLogKeyword1() {
        debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("1234"));
        checkKeywordLine1();
        debugExecutionContext.endKeyword("Keyword");
    }

    private void startBuiltInLogKeyword2() {
        debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("1234"));
        checkKeywordLine2();
        debugExecutionContext.endKeyword("Keyword");
    }

    private void startBuiltInLogKeyword3() {
        debugExecutionContext.startKeyword("BuiltIn.Log", "Test Foritem", Arrays.asList("1234"));
        checkKeywordLine3();
        debugExecutionContext.endKeyword("Test Foritem");
    }

    private void startBuiltInLogKeyword4() {
        debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("1234"));
        checkKeywordLine4();
        debugExecutionContext.endKeyword("Keyword");
    }

    private void startBuiltInLogKeyword9() {
        debugExecutionContext.startKeyword("BuiltIn.Log", "Test Foritem", Arrays.asList("${i}"));
        checkKeywordLine9();
        debugExecutionContext.endKeyword("Test Foritem");
    }
    
    private void startBuiltInLogKeyword9_newType() {
        debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("${i}"));
        checkKeywordLine9();
        debugExecutionContext.endKeyword("Keyword");
    }

    private void startKey1Keyword() {
        debugExecutionContext.startKeyword("key1", "Keyword", Arrays.asList(""));
        checkKeywordLine1();
        startBuiltInLogKeyword1();
        startBuiltInLogKeyword1();
        debugExecutionContext.startKeyword("key2", "Keyword", Arrays.asList(""));
        checkKeywordLine1();
        startBuiltInLogKeyword1();
        debugExecutionContext.startKeyword("resource3.MyLog3", "Keyword", Arrays.asList(""));
        checkKeywordLine1();
        startBuiltInLogKeyword1();
        startBuiltInLogKeyword1();
        debugExecutionContext.startKeyword("testP", "Keyword", Arrays.asList(""));
        checkKeywordLine1();
        startBuiltInLogKeyword1();
        startBuiltInLogKeyword1();
        debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endKeyword("Keyword");
    }

    private void startKey3Keyword() {
        debugExecutionContext.startKeyword("key3", "Keyword", Arrays.asList(""));
        checkKeywordLine1();
        startBuiltInLogKeyword1();
        debugExecutionContext.startKeyword("key4", "Keyword", Arrays.asList(""));
        checkKeywordLine1();
        startBuiltInLogKeyword1();
        startBuiltInLogKeyword1();
        debugExecutionContext.endKeyword("Keyword");
        startBuiltInLogKeyword1();
        debugExecutionContext.endKeyword("Keyword");
    }

    private void startMyLogKeyword() {
        debugExecutionContext.startKeyword("resource2.MyLog", "Keyword", Arrays.asList(""));
        checkKeywordLine1();
        startBuiltInLogKeyword1();
        startBuiltInLogKeyword1();
        debugExecutionContext.startKeyword("testN", "Keyword", Arrays.asList(""));
        checkKeywordLine1();
        startBuiltInLogKeyword1();
        startBuiltInLogKeyword1();
        debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endKeyword("Keyword");
    }

    private void startTestKKeyword() {
        debugExecutionContext.startKeyword("resource1.testK", "Keyword", Arrays.asList(""));
        checkKeywordLine1();
        startBuiltInLogKeyword1();
        startBuiltInLogKeyword1();
        startMyLogKeyword();
        debugExecutionContext.startKeyword("testM", "Keyword", Arrays.asList(""));
        checkKeywordLine1();
        startBuiltInLogKeyword1();
        startBuiltInLogKeyword1();
        debugExecutionContext.endKeyword("Keyword");
        debugExecutionContext.endKeyword("Keyword");
    }
    
    private void checkKeywordLine(int lineNumber) {
        Assert.assertEquals(lineNumber, debugExecutionContext.findKeywordPosition().getLineNumber());
        linesCounter++;
    }

    private void checkKeywordLine1() {
        checkKeywordLine(test1_lines[linesCounter]);
    }

    private void checkKeywordLine2() {
        checkKeywordLine(test2_lines[linesCounter]);
    }

    private void checkKeywordLine3() {
        checkKeywordLine(test3_lines[linesCounter]);
    }

    private void checkKeywordLine4() {
        checkKeywordLine(test4_lines[linesCounter]);
    }

    private void checkKeywordLine5() {
        checkKeywordLine(test5_lines[linesCounter]);
    }

    private void checkKeywordLine6() {
        checkKeywordLine(test6_lines[linesCounter]);
    }

    private void checkKeywordLine7() {
        checkKeywordLine(test7_lines[linesCounter]);
    }

    private void checkKeywordLine8() {
        checkKeywordLine(test8_lines[linesCounter]);
    }

    private void checkKeywordLine9() {
        checkKeywordLine(test9_lines[linesCounter]);
    }
    
    private void checkKeywordLine11() {
        checkKeywordLine(test11_lines[linesCounter]);
    }
}
