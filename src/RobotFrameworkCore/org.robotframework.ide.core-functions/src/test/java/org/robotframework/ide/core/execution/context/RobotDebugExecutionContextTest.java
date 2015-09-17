/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.execution.context;

import java.net.URISyntaxException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Ignore;
import org.robotframework.ide.core.testData.model.RobotFile;


public class RobotDebugExecutionContextTest {
    
    private RobotDebugExecutionContext debugExecutionContext;
    private int linesCounter = 0;
    
    private int[] lines1 = new int[] { 7, 8, 10, 11, 28, 29, 30, 32, 33, 3, 4, 5, 8, 9, 12, 35, 36, 39, 40, 37, 13, 14, 6, 7, 8,
            3, 4, 5, 8, 9, 9, 12, 13, 14, 3, 4, 5, 8, 9, 15, 18, 19, 15, 12, 13, 14, 3, 4, 5, 8, 9, 15, 18, 19, 16, 19,
            20, 42, 28, 29, 30, 32, 33, 3, 4, 5, 8, 9, 43, 35, 36, 39, 40, 37, 21 };

    private int[] lines2 = new int[] { 6, 16, 9, 18, 22, 12, 12, 13, 14, 17, 13, 23, 19 };
    
    private int[] lines3 = new int[] { 6, 7, 8, 6, 7, 8, 6, 7, 8, 9 };

    private int[] lines4 = new int[] { 4, 21, 23, 26, 28, 7, 12, 26, 28 };
    
    @Test
    public void test_MultipleUserKeywordsAndResources() throws URISyntaxException {
        linesCounter = 0;
        RobotFile modelFile = RobotModelTestProvider.getModelFile("test1.robot");
        
        debugExecutionContext = new RobotDebugExecutionContext();
        debugExecutionContext.startSuite(modelFile.getParent());
        
        debugExecutionContext.startTest("test a");
        
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("${scalar}"));checkKeywordLine1();debugExecutionContext.endKeyword();
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("${a}"));checkKeywordLine1();debugExecutionContext.endKeyword();
            startBuiltInLogKeyword1();
            startKey1Keyword();
            startKey3Keyword();
            startBuiltInLogKeyword1();
            
            debugExecutionContext.startKeyword("resource1.MyLog2", "Keyword", Arrays.asList(""));
            checkKeywordLine1();
                startBuiltinLogKeyword2();
                startBuiltInLogKeyword1();
                startMyLogKeyword();
                startTestKKeyword();
            debugExecutionContext.endKeyword();
            
            startTestKKeyword();
            startBuiltInLogKeyword1();
        
        debugExecutionContext.endTest();
        
        debugExecutionContext.startTest("test b");
        
            startBuiltInLogKeyword1();
            debugExecutionContext.startKeyword("key5", "Keyword", Arrays.asList(""));
            checkKeywordLine1();
                startKey1Keyword();
                startKey3Keyword();
            debugExecutionContext.endKeyword();
            startBuiltInLogKeyword1();
            
        debugExecutionContext.endTest();
    }
    
    @Test
    public void test_MultipleResources() throws URISyntaxException {
        linesCounter = 0;
        
        RobotFile modelFile = RobotModelTestProvider.getModelFile("test2.robot");
        
        debugExecutionContext = new RobotDebugExecutionContext();
        debugExecutionContext.startSuite(modelFile.getParent());
        
        debugExecutionContext.startTest("test a");
        
            debugExecutionContext.startKeyword("key1", "Keyword", Arrays.asList(""));
            checkKeywordLine2();
                debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("1234"));checkKeywordLine2();debugExecutionContext.endKeyword();
            debugExecutionContext.endKeyword();
            
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
                            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("1234"));checkKeywordLine2();debugExecutionContext.endKeyword();
                            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("1234"));checkKeywordLine2();debugExecutionContext.endKeyword();
                            debugExecutionContext.startKeyword("Keyword4", "Keyword", Arrays.asList(""));
                            checkKeywordLine2();
                                debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("1234"));checkKeywordLine2();debugExecutionContext.endKeyword();
                            debugExecutionContext.endKeyword();
                        debugExecutionContext.endKeyword();
                        debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("1234"));checkKeywordLine2();debugExecutionContext.endKeyword();
                    debugExecutionContext.endKeyword();
                    debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("1234"));checkKeywordLine2();debugExecutionContext.endKeyword();
                debugExecutionContext.endKeyword();
                debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("1234"));checkKeywordLine2();debugExecutionContext.endKeyword();
            debugExecutionContext.endKeyword();
            
        debugExecutionContext.endTest();
        
    }
    
    @Ignore
    @Test
    public void test_ForLoop() throws URISyntaxException {
        linesCounter = 0;
        RobotFile modelFile = RobotModelTestProvider.getModelFile("test3.robot");
        
        debugExecutionContext = new RobotDebugExecutionContext();
        debugExecutionContext.startSuite(modelFile.getParent());
        debugExecutionContext.startTest("test a");
        
            debugExecutionContext.startKeyword("${i} IN [ @{t} ]", "Test For", Arrays.asList(""));
            checkKeywordLine3();
                debugExecutionContext.startKeyword("${i} = 1", "Test Foritem", Arrays.asList(""));checkKeywordLine3();
                    debugExecutionContext.startKeyword("BuiltIn.Log", "Test Foritem", Arrays.asList("1234"));checkKeywordLine3();debugExecutionContext.endKeyword();
                    debugExecutionContext.startKeyword("BuiltIn.Log", "Test Foritem", Arrays.asList("1234"));checkKeywordLine3();debugExecutionContext.endKeyword();
                debugExecutionContext.endKeyword();
                debugExecutionContext.startKeyword("${i} = 2", "Test Foritem", Arrays.asList(""));checkKeywordLine3();
                    debugExecutionContext.startKeyword("BuiltIn.Log", "Test Foritem", Arrays.asList("1234"));checkKeywordLine3();debugExecutionContext.endKeyword();
                    debugExecutionContext.startKeyword("BuiltIn.Log", "Test Foritem", Arrays.asList("1234"));checkKeywordLine3();debugExecutionContext.endKeyword();
                debugExecutionContext.endKeyword();
                debugExecutionContext.startKeyword("${i} = 3", "Test Foritem", Arrays.asList(""));checkKeywordLine3();
                    debugExecutionContext.startKeyword("BuiltIn.Log", "Test Foritem", Arrays.asList("1234"));checkKeywordLine3();debugExecutionContext.endKeyword();
                    debugExecutionContext.startKeyword("BuiltIn.Log", "Test Foritem", Arrays.asList("1234"));checkKeywordLine3();debugExecutionContext.endKeyword();
                debugExecutionContext.endKeyword();
            debugExecutionContext.endKeyword();
            
            debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("1234"));checkKeywordLine3();debugExecutionContext.endKeyword();
        
        debugExecutionContext.endTest();
    }
    
    @Test
    public void test_Comments() throws URISyntaxException {
        linesCounter = 0;
        RobotFile modelFile = RobotModelTestProvider.getModelFile("test4.robot");
        
        debugExecutionContext = new RobotDebugExecutionContext();
        debugExecutionContext.startSuite(modelFile.getParent());
        
        debugExecutionContext.startTest("test a");
            debugExecutionContext.startKeyword("key1", "Keyword", Arrays.asList(""));
            checkKeywordLine4();
                startBuiltInLogKeyword3();
                debugExecutionContext.startKeyword("key2", "Keyword", Arrays.asList(""));
                checkKeywordLine4();
                    startBuiltInLogKeyword3();
                    startBuiltInLogKeyword3();
                debugExecutionContext.endKeyword();
            debugExecutionContext.endKeyword();
            startBuiltInLogKeyword3();
        debugExecutionContext.endTest();
        
        debugExecutionContext.startTest("test b");
            debugExecutionContext.startKeyword("key2", "Keyword", Arrays.asList(""));
            checkKeywordLine4();
                startBuiltInLogKeyword3();
                startBuiltInLogKeyword3();
            debugExecutionContext.endKeyword();
        debugExecutionContext.endTest();
    }
    
    private void startBuiltInLogKeyword1() {
        debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("1234"));checkKeywordLine1();debugExecutionContext.endKeyword();
    }
    
    private void startBuiltinLogKeyword2() {
        debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("some info"));checkKeywordLine1();debugExecutionContext.endKeyword();
    }
    
    private void startBuiltInLogKeyword3() {
        debugExecutionContext.startKeyword("BuiltIn.Log", "Keyword", Arrays.asList("1234"));checkKeywordLine4();debugExecutionContext.endKeyword();
    }
    
    private void startKey1Keyword() {
        debugExecutionContext.startKeyword("key1", "Keyword", Arrays.asList(""));checkKeywordLine1();
            startBuiltInLogKeyword1();
            startBuiltInLogKeyword1();
            debugExecutionContext.startKeyword("key2", "Keyword", Arrays.asList(""));checkKeywordLine1();
                startBuiltInLogKeyword1();
                debugExecutionContext.startKeyword("resource3.MyLog3", "Keyword", Arrays.asList(""));checkKeywordLine1();
                    startBuiltInLogKeyword1();
                    startBuiltInLogKeyword1();
                    debugExecutionContext.startKeyword("testP", "Keyword", Arrays.asList(""));checkKeywordLine1();
                        startBuiltInLogKeyword1();
                        startBuiltInLogKeyword1();
                    debugExecutionContext.endKeyword();
                debugExecutionContext.endKeyword();
            debugExecutionContext.endKeyword();
        debugExecutionContext.endKeyword();
    }
    
    private void startKey3Keyword() {
        debugExecutionContext.startKeyword("key3", "Keyword", Arrays.asList(""));checkKeywordLine1();
            startBuiltInLogKeyword1();
            debugExecutionContext.startKeyword("key4", "Keyword", Arrays.asList(""));checkKeywordLine1();
                startBuiltInLogKeyword1();
                startBuiltInLogKeyword1();
            debugExecutionContext.endKeyword();
            startBuiltInLogKeyword1();
        debugExecutionContext.endKeyword();
    }
    
    private void startMyLogKeyword() {
        debugExecutionContext.startKeyword("resource2.MyLog", "Keyword", Arrays.asList(""));checkKeywordLine1();
            startBuiltinLogKeyword2();
            startBuiltInLogKeyword1();
            debugExecutionContext.startKeyword("testN", "Keyword", Arrays.asList(""));checkKeywordLine1();
                startBuiltInLogKeyword1();
                startBuiltInLogKeyword1();
            debugExecutionContext.endKeyword();
        debugExecutionContext.endKeyword();
    }
    
    private void startTestKKeyword() {
        debugExecutionContext.startKeyword("resource1.testK", "Keyword", Arrays.asList(""));checkKeywordLine1();
            startBuiltInLogKeyword1();
            startBuiltInLogKeyword1();
            startMyLogKeyword();
            debugExecutionContext.startKeyword("testM", "Keyword", Arrays.asList(""));checkKeywordLine1();
                startBuiltInLogKeyword1();
                startBuiltInLogKeyword1();
            debugExecutionContext.endKeyword();
        debugExecutionContext.endKeyword();
    }
    
    private void checkKeywordLine1() {
        Assert.assertEquals(lines1[linesCounter], debugExecutionContext.findKeywordPosition().getLineNumber());
        linesCounter++;
    }
    
    private void checkKeywordLine2() {
        Assert.assertEquals(lines2[linesCounter], debugExecutionContext.findKeywordPosition().getLineNumber());
        linesCounter++;
    }
    
    private void checkKeywordLine3() {
        Assert.assertEquals(lines3[linesCounter], debugExecutionContext.findKeywordPosition().getLineNumber());
        linesCounter++;
    }
    
    private void checkKeywordLine4() {
        Assert.assertEquals(lines4[linesCounter], debugExecutionContext.findKeywordPosition().getLineNumber());
        linesCounter++;
    }
}
