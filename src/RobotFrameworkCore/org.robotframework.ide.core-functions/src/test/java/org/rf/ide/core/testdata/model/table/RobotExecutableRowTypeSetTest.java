/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

/**
 * @author wypych
 */
public class RobotExecutableRowTypeSetTest {

    @Test
    public void test_getAllElementsWhen_parentIsTestCase_andWithoutSetActionAndOnlyComment_shouldReturnAllTokensWithType_TestCase() {
        // prepare
        final RobotExecutableRow<Object> rowExec = new RobotExecutableRow<>();
        final RobotToken comment = new RobotToken();
        comment.setText("comment");

        // execute
        rowExec.setParent(new TestCase(new RobotToken()));
        rowExec.addCommentPart(comment);
        final List<RobotToken> toks = rowExec.getElementTokens();

        // verify
        assertThat(rowExec.getAction().getTypes().get(0)).isEqualTo(RobotTokenType.TEST_CASE_ACTION_NAME);
        assertThat(rowExec.getArguments()).isEmpty();
        assertThat(rowExec.getComment()).hasSize(1);
        assertThat(rowExec.getComment().get(0).getTypes().get(0)).isEqualTo(RobotTokenType.START_HASH_COMMENT);

        assertThat(toks).hasSize(2);
        assertThat(toks).containsExactlyElementsOf(Arrays.asList(rowExec.getAction(), rowExec.getComment().get(0)));
    }

    @Test
    public void test_getAllElementsWhen_parentIsTestCase_shouldReturnAllTokensWithType_TestCase() {
        // prepare
        final RobotExecutableRow<Object> rowExec = new RobotExecutableRow<>();
        final RobotToken action = new RobotToken();
        final RobotToken arg = new RobotToken();
        arg.setText("text");

        // execute
        rowExec.setAction(action);
        rowExec.addArgument(arg);
        rowExec.setParent(new TestCase(new RobotToken()));
        final List<RobotToken> toks = rowExec.getElementTokens();

        // verify
        assertThat(toks).containsExactly(action, arg);
        assertThat(action.getTypes().get(0)).isEqualTo(RobotTokenType.TEST_CASE_ACTION_NAME);
        assertThat(arg.getTypes().get(0)).isEqualTo(RobotTokenType.TEST_CASE_ACTION_ARGUMENT);
    }

    @Test
    public void test_getAllElementsWhen_parentIsUserKeyword_shouldReturnAllTokensWithType_UserKeyword() {
        // prepare
        final RobotExecutableRow<Object> rowExec = new RobotExecutableRow<>();
        final RobotToken action = new RobotToken();
        final RobotToken arg = new RobotToken();
        arg.setText("text");

        // execute
        rowExec.setAction(action);
        rowExec.addArgument(arg);
        rowExec.setParent(new UserKeyword(new RobotToken()));
        final List<RobotToken> toks = rowExec.getElementTokens();

        // verify
        assertThat(toks).containsExactly(action, arg);
        assertThat(action.getTypes().get(0)).isEqualTo(RobotTokenType.KEYWORD_ACTION_NAME);
        assertThat(arg.getTypes().get(0)).isEqualTo(RobotTokenType.KEYWORD_ACTION_ARGUMENT);
    }

    @Test
    public void test_getAllElementsWhen_parentIsNotSet_shouldReturnAllTokensWithType_UNKNOWN() {
        // prepare
        final RobotExecutableRow<Object> rowExec = new RobotExecutableRow<>();
        final RobotToken action = new RobotToken();
        final RobotToken arg = new RobotToken();
        arg.setText("arg");

        // execute
        rowExec.setAction(action);
        rowExec.addArgument(arg);
        final List<RobotToken> toks = rowExec.getElementTokens();

        // verify
        assertThat(toks).containsExactly(action, arg);
        assertThat(action.getTypes().get(0)).isEqualTo(RobotTokenType.UNKNOWN);
        assertThat(arg.getTypes().get(0)).isEqualTo(RobotTokenType.UNKNOWN);
    }

    @Test
    public void test_ifArgumentIncaseParentClassIs_TestCase_argumentShouldGetType_testCase_addArgumentMethod() {
        // prepare
        final RobotExecutableRow<Object> rowExec = new RobotExecutableRow<>();

        // execute
        rowExec.setParent(new TestCase(new RobotToken()));
        final RobotToken arg = new RobotToken();
        rowExec.setArgument(0, arg);
        final List<RobotToken> arguments = rowExec.getArguments();

        // verify
        assertThat(arguments).containsExactly(arg);
        assertThat(arguments.get(0).getTypes()).containsExactly(RobotTokenType.TEST_CASE_ACTION_ARGUMENT);
    }

    @Test
    public void test_ifArgumentIncaseParentClassIs_TestCase_argumentShouldGetType_testCase_setArgumentMethod() {
        // prepare
        final RobotExecutableRow<Object> rowExec = new RobotExecutableRow<>();

        // execute
        rowExec.setParent(new TestCase(new RobotToken()));
        final RobotToken arg = new RobotToken();
        rowExec.setArgument(0, arg);
        final List<RobotToken> arguments = rowExec.getArguments();

        // verify
        assertThat(arguments).containsExactly(arg);
        assertThat(arguments.get(0).getTypes()).containsExactly(RobotTokenType.TEST_CASE_ACTION_ARGUMENT);
    }

    @Test
    public void test_ifArgumentIncaseParentClassIs_UserKeyword_argumentShouldGetType_keyword_addArgumentMethod() {
        // prepare
        final RobotExecutableRow<Object> rowExec = new RobotExecutableRow<>();

        // execute
        rowExec.setParent(new UserKeyword(new RobotToken()));
        final RobotToken arg = new RobotToken();
        rowExec.setArgument(0, arg);
        final List<RobotToken> arguments = rowExec.getArguments();

        // verify
        assertThat(arguments).containsExactly(arg);
        assertThat(arguments.get(0).getTypes()).containsExactly(RobotTokenType.KEYWORD_ACTION_ARGUMENT);
    }

    @Test
    public void test_ifArgumentIncaseParentClassIs_UserKeyword_argumentShouldGetType_keyword_setArgumentMethod() {
        // prepare
        final RobotExecutableRow<Object> rowExec = new RobotExecutableRow<>();

        // execute
        rowExec.setParent(new UserKeyword(new RobotToken()));
        final RobotToken arg = new RobotToken();
        rowExec.setArgument(0, arg);
        final List<RobotToken> arguments = rowExec.getArguments();

        // verify
        assertThat(arguments).containsExactly(arg);
        assertThat(arguments.get(0).getTypes()).containsExactly(RobotTokenType.KEYWORD_ACTION_ARGUMENT);
    }

    @Test
    public void test_ifArgumentIncaseParentClassIs_notSet_argumentShouldGetType_UNKNOWN_addArgumentMethod() {
        // prepare
        final RobotExecutableRow<Object> rowExec = new RobotExecutableRow<>();

        // execute
        final RobotToken arg = new RobotToken();
        rowExec.addArgument(arg);
        final List<RobotToken> arguments = rowExec.getArguments();

        // verify
        assertThat(arguments).containsExactly(arg);
        assertThat(arguments.get(0).getTypes()).containsExactly(RobotTokenType.UNKNOWN);
    }

    @Test
    public void test_ifArgumentIncaseParentClassIs_notSet_argumentShouldGetType_UNKNOWN_setArgumentMethod() {
        // prepare
        final RobotExecutableRow<Object> rowExec = new RobotExecutableRow<>();

        // execute
        final RobotToken arg = new RobotToken();
        rowExec.setArgument(0, arg);
        final List<RobotToken> arguments = rowExec.getArguments();

        // verify
        assertThat(arguments).containsExactly(arg);
        assertThat(arguments.get(0).getTypes()).containsExactly(RobotTokenType.UNKNOWN);
    }

    @Test
    public void test_ifActionIncaseParentClassIs_TestCase_WillSetTypeTo_TESTCASEtype() {
        // prepare
        final RobotExecutableRow<Object> rowExec = new RobotExecutableRow<>();
        final RobotToken actionPut = new RobotToken();

        // execute
        rowExec.setParent(new TestCase(new RobotToken()));
        rowExec.setAction(actionPut);
        final RobotToken actionGet = rowExec.getAction();

        // verify
        assertThat(actionGet).isSameAs(actionPut);
        assertThat(actionGet.getTypes()).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME, RobotTokenType.UNKNOWN);

    }

    @Test
    public void test_ifActionIncaseParentClassIs_UserKeyword_WillSetTypeTo_KEYWORDtype() {
        // prepare
        final RobotExecutableRow<Object> rowExec = new RobotExecutableRow<>();
        final RobotToken actionPut = new RobotToken();

        // execute
        rowExec.setParent(new UserKeyword(new RobotToken()));
        rowExec.setAction(actionPut);
        final RobotToken actionGet = rowExec.getAction();

        // verify
        assertThat(actionGet).isSameAs(actionPut);
        assertThat(actionGet.getTypes()).containsExactly(RobotTokenType.KEYWORD_ACTION_NAME, RobotTokenType.UNKNOWN);
    }

    @Test
    public void test_ifActionIncaseNoParentClassWillSetTypeTo_UNKNOWN() {
        // prepare
        final RobotExecutableRow<Object> rowExec = new RobotExecutableRow<>();
        final RobotToken actionPut = new RobotToken();

        // execute
        rowExec.setAction(actionPut);
        final RobotToken actionGet = rowExec.getAction();

        // verify
        assertThat(actionGet).isSameAs(actionPut);
        assertThat(actionGet.getTypes()).containsExactly(RobotTokenType.UNKNOWN);
    }
}
