/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class RobotExecutableRowTest {

    @Test
    public void compactGetElementsToken_lastArgumentTokenIsEmpty() {
        // prepare
        final RobotToken action = new RobotToken();
        final RobotToken arg1 = RobotToken.create("foo");
        final RobotToken arg2 = new RobotToken();
        final RobotToken arg3 = RobotToken.create("bar");
        final RobotToken arg4 = new RobotToken();

        final RobotExecutableRow<UserKeyword> row = new RobotExecutableRow<>();
        row.setAction(action);
        row.addArgument(arg1);
        row.addArgument(arg2);
        row.addArgument(arg3);
        row.addArgument(arg4);

        // execute
        final List<RobotToken> allElements = row.getElementTokens();

        // verify
        assertThat(allElements).containsExactly(action, arg1, arg2, arg3);
    }

    @Test
    public void compactGetElementsToken_lastArgumentTokenIsNotEmpty() {
        // prepare
        final RobotToken action = new RobotToken();
        final RobotToken arg1 = RobotToken.create("foo");
        final RobotToken arg2 = new RobotToken();
        final RobotToken arg3 = RobotToken.create("bar");
        final RobotToken arg4 = RobotToken.create("foobar");

        final RobotExecutableRow<UserKeyword> row = new RobotExecutableRow<>();
        row.setAction(action);
        row.addArgument(arg1);
        row.addArgument(arg2);
        row.addArgument(arg3);
        row.addArgument(arg4);

        // execute
        final List<RobotToken> allElements = row.getElementTokens();

        // verify
        assertThat(allElements).containsExactly(action, arg1, arg2, arg3, arg4);
    }

    @Test
    public void test_ifCellWillBeAdded_atActionPosition() {
        // prepare
        final RobotToken action = RobotToken.create("act");
        final RobotToken arg1 = RobotToken.create("t");
        final RobotToken arg2 = RobotToken.create("e");
        final RobotToken arg3 = RobotToken.create("s");
        final RobotToken arg4 = RobotToken.create("t");

        final RobotExecutableRow<UserKeyword> row = new RobotExecutableRow<>();
        row.setParent(new UserKeyword(new RobotToken()));
        row.setAction(action);
        row.addArgument(arg1);
        row.addArgument(arg2);
        row.addArgument(arg3);
        row.addArgument(arg4);

        // execute
        row.insertValueAt("action", 0);

        // verify
        final List<RobotToken> elementTokens = row.getElementTokens();
        assertThat(elementTokens).hasSize(6);
        assertThat(elementTokens.get(0).getText()).isEqualTo("action");
        assertThat(elementTokens.get(0).getTypes()).contains(RobotTokenType.KEYWORD_ACTION_NAME);
        assertThat(elementTokens.get(1).getText()).isEqualTo("act");
        assertThat(elementTokens.get(1).getTypes()).contains(RobotTokenType.KEYWORD_ACTION_ARGUMENT);
        assertThat(elementTokens.get(2)).isSameAs(arg1);
        assertThat(elementTokens.get(2).getTypes()).contains(RobotTokenType.KEYWORD_ACTION_ARGUMENT);
        assertThat(elementTokens.get(3)).isSameAs(arg2);
        assertThat(elementTokens.get(3).getTypes()).contains(RobotTokenType.KEYWORD_ACTION_ARGUMENT);
        assertThat(elementTokens.get(4)).isSameAs(arg3);
        assertThat(elementTokens.get(4).getTypes()).contains(RobotTokenType.KEYWORD_ACTION_ARGUMENT);
        assertThat(elementTokens.get(5)).isSameAs(arg4);
        assertThat(elementTokens.get(5).getTypes()).contains(RobotTokenType.KEYWORD_ACTION_ARGUMENT);
    }

    @Test
    public void test_ifCellWillBeAdded_atArgumentPosition() {
        // prepare
        final RobotToken action = RobotToken.create("act");
        final RobotToken arg1 = RobotToken.create("t");
        final RobotToken arg2 = RobotToken.create("e");
        final RobotToken arg3 = RobotToken.create("s");
        final RobotToken arg4 = RobotToken.create("t");

        final RobotExecutableRow<UserKeyword> row = new RobotExecutableRow<>();
        row.setParent(new UserKeyword(new RobotToken()));
        row.setAction(action);
        row.addArgument(arg1);
        row.addArgument(arg2);
        row.addArgument(arg3);
        row.addArgument(arg4);

        // execute
        row.insertValueAt("argument", 2);

        // verify
        final List<RobotToken> elementTokens = row.getElementTokens();
        assertThat(elementTokens).hasSize(6);
        assertThat(elementTokens.get(0)).isSameAs(action);
        assertThat(elementTokens.get(0).getTypes()).contains(RobotTokenType.KEYWORD_ACTION_NAME);
        assertThat(elementTokens.get(1)).isSameAs(arg1);
        assertThat(elementTokens.get(1).getTypes()).contains(RobotTokenType.KEYWORD_ACTION_ARGUMENT);
        assertThat(elementTokens.get(2).getText()).isEqualTo("argument");
        assertThat(elementTokens.get(2).getTypes()).contains(RobotTokenType.KEYWORD_ACTION_ARGUMENT);
        assertThat(elementTokens.get(3)).isSameAs(arg2);
        assertThat(elementTokens.get(3).getTypes()).contains(RobotTokenType.KEYWORD_ACTION_ARGUMENT);
        assertThat(elementTokens.get(4)).isSameAs(arg3);
        assertThat(elementTokens.get(4).getTypes()).contains(RobotTokenType.KEYWORD_ACTION_ARGUMENT);
        assertThat(elementTokens.get(5)).isSameAs(arg4);
        assertThat(elementTokens.get(5).getTypes()).contains(RobotTokenType.KEYWORD_ACTION_ARGUMENT);
    }

    @Test
    public void test_ifCellWillBeAdded_atCommentPosition() {
        // prepare
        final RobotToken action = RobotToken.create("act");
        final RobotToken arg1 = RobotToken.create("t");
        final RobotToken arg2 = RobotToken.create("e");
        final RobotToken arg3 = RobotToken.create("s");
        final RobotToken arg4 = RobotToken.create("t");
        final RobotToken cmt = RobotToken.create("#cmt");

        final RobotExecutableRow<UserKeyword> row = new RobotExecutableRow<>();
        row.setParent(new UserKeyword(new RobotToken()));
        row.setAction(action);
        row.addArgument(arg1);
        row.addArgument(arg2);
        row.addArgument(arg3);
        row.addArgument(arg4);
        row.addCommentPart(cmt);

        // execute
        row.insertValueAt("argument", 5);

        // verify
        final List<RobotToken> elementTokens = row.getElementTokens();
        assertThat(elementTokens).hasSize(7);
        assertThat(elementTokens.get(0)).isSameAs(action);
        assertThat(elementTokens.get(0).getTypes()).contains(RobotTokenType.KEYWORD_ACTION_NAME);
        assertThat(elementTokens.get(1)).isSameAs(arg1);
        assertThat(elementTokens.get(1).getTypes()).contains(RobotTokenType.KEYWORD_ACTION_ARGUMENT);
        assertThat(elementTokens.get(2)).isSameAs(arg2);
        assertThat(elementTokens.get(2).getTypes()).contains(RobotTokenType.KEYWORD_ACTION_ARGUMENT);
        assertThat(elementTokens.get(3)).isSameAs(arg3);
        assertThat(elementTokens.get(3).getTypes()).contains(RobotTokenType.KEYWORD_ACTION_ARGUMENT);
        assertThat(elementTokens.get(4)).isSameAs(arg4);
        assertThat(elementTokens.get(4).getTypes()).contains(RobotTokenType.KEYWORD_ACTION_ARGUMENT);
        assertThat(elementTokens.get(5).getText()).isEqualTo("argument");
        assertThat(elementTokens.get(5).getTypes()).contains(RobotTokenType.KEYWORD_ACTION_ARGUMENT);
        assertThat(elementTokens.get(6)).isSameAs(cmt);
        assertThat(elementTokens.get(6).getTypes()).contains(RobotTokenType.START_HASH_COMMENT);
    }
}
