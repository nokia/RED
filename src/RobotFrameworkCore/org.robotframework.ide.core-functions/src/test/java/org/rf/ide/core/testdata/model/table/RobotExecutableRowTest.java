/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;

import org.junit.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
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
        row.createToken(0);
        row.updateToken(0, "action");

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
        row.createToken(2);
        row.updateToken(2, "argument");

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
        row.createToken(5);
        row.updateToken(5, "argument");

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

    @Test
    public void tokenCreationDoesNothing_whenInsertingOutOfTheRow() {
        final TestCase test = createTest();
        final RobotExecutableRow<TestCase> row = createRow(test, "action", args("1", "2"), comment("c1", "c2"));

        row.createToken(-1);
        row.createToken(6);
        row.createToken(7);
        row.createToken(10);

        assertThat(cellsOf(row)).containsExactly("action", "1", "2", "# c1", "c2");
        assertThat(typesOf(row)).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.START_HASH_COMMENT, RobotTokenType.COMMENT_CONTINUE);
    }

    @Test
    public void tokenCreationInsertsEmptyToken_whenInsertingAtActionName() {
        final TestCase test = createTest();
        final RobotExecutableRow<TestCase> row = createRow(test, "action", args("1", "2"), comment("c1", "c2"));

        row.createToken(0);

        assertThat(cellsOf(row)).containsExactly("", "action", "1", "2", "# c1", "c2");
        assertThat(typesOf(row)).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.START_HASH_COMMENT,
                RobotTokenType.COMMENT_CONTINUE);
    }

    @Test
    public void tokenCreationInsertsEmptyToken_whenInsertingAtArgumentOrFirstComment() {
        final TestCase test = createTest();
        final RobotExecutableRow<TestCase> row1 = createRow(test, "action", args("1", "2"), comment("c1", "c2"));
        final RobotExecutableRow<TestCase> row2 = createRow(test, "action", args("1", "2"), comment("c1", "c2"));
        final RobotExecutableRow<TestCase> row3 = createRow(test, "action", args("1", "2"), comment("c1", "c2"));

        row1.createToken(1);
        row2.createToken(2);
        row3.createToken(3);

        assertThat(cellsOf(row1)).containsExactly("action", "", "1", "2", "# c1", "c2");
        assertThat(typesOf(row1)).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.START_HASH_COMMENT,
                RobotTokenType.COMMENT_CONTINUE);
        assertThat(cellsOf(row2)).containsExactly("action", "1", "", "2", "# c1", "c2");
        assertThat(typesOf(row2)).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.START_HASH_COMMENT,
                RobotTokenType.COMMENT_CONTINUE);
        assertThat(cellsOf(row3)).containsExactly("action", "1", "2", "", "# c1", "c2");
        assertThat(typesOf(row3)).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.START_HASH_COMMENT,
                RobotTokenType.COMMENT_CONTINUE);
    }

    @Test
    public void tokenCreationInsertsEmptyComment_whenInsertingAtNonFirstComment() {
        final TestCase test = createTest();
        final RobotExecutableRow<TestCase> row = createRow(test, "action", args("1", "2"), comment("c1", "c2"));

        row.createToken(4);

        assertThat(cellsOf(row)).containsExactly("action", "1", "2", "# c1", "", "c2");
        assertThat(typesOf(row)).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.START_HASH_COMMENT, RobotTokenType.COMMENT_CONTINUE, RobotTokenType.COMMENT_CONTINUE);
    }

    @Test
    public void tokenDeletionIsNotPossible_whenTryingToRemoveActionButThereAreNoArguments() {
        final TestCase test = createTest();
        final RobotExecutableRow<TestCase> row1 = createRow(test, "action", args(), comment());
        final RobotExecutableRow<TestCase> row2 = createRow(test, "action", args(), comment("c1", "c2"));

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> row1.deleteToken(0));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> row2.deleteToken(0));
    }

    @Test
    public void tokenDeletionDoesNothing_whenIndexIsOutOfTheRow() {
        final TestCase test = createTest();
        final RobotExecutableRow<TestCase> row = createRow(test, "action", args("1", "2"), comment("c1", "c2"));

        row.deleteToken(-1);
        row.deleteToken(5);
        row.deleteToken(6);
        row.deleteToken(10);

        assertThat(cellsOf(row)).containsExactly("action", "1", "2", "# c1", "c2");
        assertThat(typesOf(row)).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.START_HASH_COMMENT, RobotTokenType.COMMENT_CONTINUE);
    }

    @Test
    public void tokenDeletionMovesArgumentAsAction_whenActionIsRemoved() {
        final TestCase test = createTest();
        final RobotExecutableRow<TestCase> row = createRow(test, "action", args("1", "2"), comment("c1", "c2"));

        row.deleteToken(0);

        assertThat(cellsOf(row)).containsExactly("1", "2", "# c1", "c2");
        assertThat(typesOf(row)).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.START_HASH_COMMENT,
                RobotTokenType.COMMENT_CONTINUE);
    }

    @Test
    public void tokenDeletionRemovesArgument_whenArgumentIsRemoved() {
        final TestCase test = createTest();
        final RobotExecutableRow<TestCase> row1 = createRow(test, "action", args("1", "2"), comment("c1", "c2"));
        final RobotExecutableRow<TestCase> row2 = createRow(test, "action", args("1", "2"), comment("c1", "c2"));

        row1.deleteToken(1);
        row2.deleteToken(2);

        assertThat(cellsOf(row1)).containsExactly("action", "2", "# c1", "c2");
        assertThat(typesOf(row1)).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.START_HASH_COMMENT,
                RobotTokenType.COMMENT_CONTINUE);

        assertThat(cellsOf(row2)).containsExactly("action", "1", "# c1", "c2");
        assertThat(typesOf(row2)).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.START_HASH_COMMENT,
                RobotTokenType.COMMENT_CONTINUE);
    }

    @Test
    public void tokenDeletionRemovesComment_andMovesOtherToArgumentsProperly() {
        final TestCase test = createTest();
        final RobotExecutableRow<TestCase> row1 = createRow(test, "action", args("1", "2"), comment("c1", "c2"));
        final RobotExecutableRow<TestCase> row2 = createRow(test, "action", args("1", "2"),
                comment("c1", "c2", "# c3", "c4"));

        row1.deleteToken(3);
        row2.deleteToken(3);

        assertThat(cellsOf(row1)).containsExactly("action", "1", "2", "c2");
        assertThat(typesOf(row1)).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT);

        assertThat(cellsOf(row2)).containsExactly("action", "1", "2", "c2", "# c3", "c4");
        assertThat(typesOf(row2)).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.START_HASH_COMMENT,
                RobotTokenType.COMMENT_CONTINUE);
    }

    @Test
    public void tokenUpdateIsNotPossible_whenTryingToChangeActionIntoComment() {
        final TestCase test = createTest();
        final RobotExecutableRow<TestCase> row = createRow(test, "action", args("1", "2"), comment("c1", "c2"));

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> row.updateToken(0, "# action"));
    }

    @Test
    public void tokenUpdateDoesNothing_whenNegativeIndexIsGiven() {
        final TestCase test = createTest();
        final RobotExecutableRow<TestCase> row = createRow(test, "action", args("1", "2"), comment("c1", "c2"));

        row.updateToken(-1, "a");
        row.updateToken(-5, "b");
        row.updateToken(-10, "c");

        assertThat(cellsOf(row)).containsExactly("action", "1", "2", "# c1", "c2");
        assertThat(typesOf(row)).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.START_HASH_COMMENT, RobotTokenType.COMMENT_CONTINUE);
    }

    @Test
    public void tokenUpdateChangesAction_whenUpdatingAtFirstElement() {
        final TestCase test = createTest();
        final RobotExecutableRow<TestCase> row = createRow(test, "action", args("1", "2"), comment("c1", "c2"));

        row.updateToken(0, "other action");

        assertThat(cellsOf(row)).containsExactly("other action", "1", "2", "# c1", "c2");
        assertThat(typesOf(row)).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.START_HASH_COMMENT, RobotTokenType.COMMENT_CONTINUE);
    }

    @Test
    public void tokenUpdateChangesArgument_whenUpdatingAfterAction() {
        final TestCase test = createTest();
        final RobotExecutableRow<TestCase> row1 = createRow(test, "action", args("1", "2"), comment("c1", "c2"));
        final RobotExecutableRow<TestCase> row2 = createRow(test, "action", args("1", "2"), comment("c1", "c2"));

        row1.updateToken(1, "11");
        row2.updateToken(2, "22");

        assertThat(cellsOf(row1)).containsExactly("action", "11", "2", "# c1", "c2");
        assertThat(typesOf(row1)).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.START_HASH_COMMENT, RobotTokenType.COMMENT_CONTINUE);

        assertThat(cellsOf(row2)).containsExactly("action", "1", "22", "# c1", "c2");
        assertThat(typesOf(row2)).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.START_HASH_COMMENT, RobotTokenType.COMMENT_CONTINUE);
    }

    @Test
    public void tokenUpdateChangesArgumentIntoComment_whenUpdatingAfterActionWithCommentedValue() {
        final TestCase test = createTest();
        final RobotExecutableRow<TestCase> row1 = createRow(test, "action", args("1", "2"), comment("c1", "c2"));
        final RobotExecutableRow<TestCase> row2 = createRow(test, "action", args("1", "2"), comment("c1", "c2"));

        row1.updateToken(1, "# commented");
        row2.updateToken(2, "# commented");

        assertThat(cellsOf(row1)).containsExactly("action", "# commented", "2", "# c1", "c2");
        assertThat(typesOf(row1)).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.START_HASH_COMMENT, RobotTokenType.COMMENT_CONTINUE, RobotTokenType.COMMENT_CONTINUE,
                RobotTokenType.COMMENT_CONTINUE);

        assertThat(cellsOf(row2)).containsExactly("action", "1", "# commented", "# c1", "c2");
        assertThat(typesOf(row2)).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.START_HASH_COMMENT,
                RobotTokenType.COMMENT_CONTINUE, RobotTokenType.COMMENT_CONTINUE);
    }

    @Test
    public void tokenUpdateChangesComment_whenUpdatingAfterArguments() {
        final TestCase test = createTest();
        final RobotExecutableRow<TestCase> row1 = createRow(test, "action", args("1", "2"), comment("c1", "c2"));
        final RobotExecutableRow<TestCase> row2 = createRow(test, "action", args("1", "2"), comment("c1", "c2"));

        row1.updateToken(3, "# d1");
        row2.updateToken(4, "d2");

        assertThat(cellsOf(row1)).containsExactly("action", "1", "2", "# d1", "c2");
        assertThat(typesOf(row1)).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.START_HASH_COMMENT, RobotTokenType.COMMENT_CONTINUE);

        assertThat(cellsOf(row2)).containsExactly("action", "1", "2", "# c1", "d2");
        assertThat(typesOf(row2)).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.START_HASH_COMMENT, RobotTokenType.COMMENT_CONTINUE);
    }

    @Test
    public void tokenUpdateChangesCommentIntoArgument_whenUpdatingAfterArgumentsWithUncommentedValue() {
        final TestCase test = createTest();
        final RobotExecutableRow<TestCase> row1 = createRow(test, "action", args("1", "2"), comment("c1", "c2"));
        final RobotExecutableRow<TestCase> row2 = createRow(test, "action", args("1", "2"), comment("c1", "# c2", "c3"));

        row1.updateToken(3, "d1");
        row2.updateToken(3, "d1");

        assertThat(cellsOf(row1)).containsExactly("action", "1", "2", "d1", "c2");
        assertThat(typesOf(row1)).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.TEST_CASE_ACTION_ARGUMENT);

        assertThat(cellsOf(row2)).containsExactly("action", "1", "2", "d1", "# c2", "c3");
        assertThat(typesOf(row2)).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.START_HASH_COMMENT,
                RobotTokenType.COMMENT_CONTINUE);
    }

    @Test
    public void tokenUpdateFillsMissingArguments_whenUpdatingRowWithoutCommentsAfterEndOfTokens() {
        final TestCase test = createTest();
        final RobotExecutableRow<TestCase> row1 = createRow(test, "action", args("1", "2"), comment());
        final RobotExecutableRow<TestCase> row2 = createRow(test, "action", args("1", "2"), comment());
        final RobotExecutableRow<TestCase> row3 = createRow(test, "action", args("1", "2"), comment());

        row1.updateToken(3, "3");
        row2.updateToken(4, "4");
        row3.updateToken(5, "# 5");

        assertThat(cellsOf(row1)).containsExactly("action", "1", "2", "3");
        assertThat(typesOf(row1)).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT);

        assertThat(cellsOf(row2)).containsExactly("action", "1", "2", "\\", "4");
        assertThat(typesOf(row2)).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.TEST_CASE_ACTION_ARGUMENT);

        assertThat(cellsOf(row3)).containsExactly("action", "1", "2", "\\", "\\", "# 5");
        assertThat(typesOf(row3)).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void tokenUpdateFillsMissingComments_whenUpdatingRowAfterEndOfComments() {
        final TestCase test = createTest();
        final RobotExecutableRow<TestCase> row1 = createRow(test, "action", args("1"), comment("c1"));
        final RobotExecutableRow<TestCase> row2 = createRow(test, "action", args("1"), comment("c1"));
        final RobotExecutableRow<TestCase> row3 = createRow(test, "action", args("1"), comment("c1"));

        row1.updateToken(3, "3");
        row2.updateToken(4, "4");
        row3.updateToken(5, "# 5");

        assertThat(cellsOf(row1)).containsExactly("action", "1", "# c1", "3");
        assertThat(typesOf(row1)).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.START_HASH_COMMENT,
                RobotTokenType.COMMENT_CONTINUE);

        assertThat(cellsOf(row2)).containsExactly("action", "1", "# c1", "\\", "4");
        assertThat(typesOf(row2)).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.START_HASH_COMMENT,
                RobotTokenType.COMMENT_CONTINUE, RobotTokenType.COMMENT_CONTINUE);

        assertThat(cellsOf(row3)).containsExactly("action", "1", "# c1", "\\", "\\", "# 5");
        assertThat(typesOf(row3)).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_ACTION_ARGUMENT, RobotTokenType.START_HASH_COMMENT,
                RobotTokenType.COMMENT_CONTINUE, RobotTokenType.COMMENT_CONTINUE, RobotTokenType.COMMENT_CONTINUE);
    }

    @Test
    public void test_ifCellWillBeAdded_atActionPosition_whenTemplateIsDefined() {
        // prepare
        final RobotToken action = RobotToken.create("act");
        final RobotToken arg1 = RobotToken.create("a1");
        final RobotToken arg2 = RobotToken.create("a2");
        final RobotToken comment = RobotToken.create("cmnt");

        final TestCase test = createTest();
        final LocalSetting<TestCase> template = test.newTemplate(0);
        template.addToken("Some Kw");
        final RobotExecutableRow<TestCase> row = new RobotExecutableRow<>();
        row.setParent(test);
        row.setAction(action);
        row.addArgument(arg1);
        row.addArgument(arg2);
        row.addCommentPart(comment);

        // execute
        row.createToken(0);
        row.updateToken(0, "action");

        // verify
        final List<RobotToken> elementTokens = row.getElementTokens();
        assertThat(elementTokens).hasSize(5);
        assertThat(elementTokens.get(0).getText()).isEqualTo("action");
        assertThat(elementTokens.get(0).getTypes()).containsExactly(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT);
        assertThat(elementTokens.get(1).getText()).isEqualTo("act");
        assertThat(elementTokens.get(1).getTypes()).containsExactly(RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT);
        assertThat(elementTokens.get(2)).isSameAs(arg1);
        assertThat(elementTokens.get(2).getTypes()).containsExactly(RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT);
        assertThat(elementTokens.get(3)).isSameAs(arg2);
        assertThat(elementTokens.get(3).getTypes()).containsExactly(RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT);
        assertThat(elementTokens.get(4)).isSameAs(comment);
        assertThat(elementTokens.get(4).getTypes()).containsExactly(RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void test_ifCellWillBeAdded_atArgumentPosition_whenTemplateIsDefined() {
        // prepare
        final RobotToken action = RobotToken.create("act");
        final RobotToken arg1 = RobotToken.create("a1");
        final RobotToken arg2 = RobotToken.create("a2");
        final RobotToken comment = RobotToken.create("cmnt");

        final TestCase test = createTest();
        final LocalSetting<TestCase> template = test.newTemplate(0);
        template.addToken("Some Kw");
        final RobotExecutableRow<TestCase> row = new RobotExecutableRow<>();
        row.setParent(test);
        row.setAction(action);
        row.addArgument(arg1);
        row.addArgument(arg2);
        row.addCommentPart(comment);

        // execute
        row.createToken(2);
        row.updateToken(2, "argument");

        // verify
        final List<RobotToken> elementTokens = row.getElementTokens();
        assertThat(elementTokens).hasSize(5);
        assertThat(elementTokens.get(0)).isSameAs(action);
        assertThat(elementTokens.get(0).getTypes()).contains(RobotTokenType.TEST_CASE_ACTION_NAME,
                RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT);
        assertThat(elementTokens.get(1)).isSameAs(arg1);
        assertThat(elementTokens.get(1).getTypes()).contains(RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT);
        assertThat(elementTokens.get(2).getText()).isEqualTo("argument");
        assertThat(elementTokens.get(2).getTypes()).contains(RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT);
        assertThat(elementTokens.get(3)).isSameAs(arg2);
        assertThat(elementTokens.get(3).getTypes()).contains(RobotTokenType.TEST_CASE_ACTION_ARGUMENT,
                RobotTokenType.TEST_CASE_TEMPLATE_ARGUMENT);
        assertThat(elementTokens.get(4)).isSameAs(comment);
        assertThat(elementTokens.get(4).getTypes()).containsExactly(RobotTokenType.START_HASH_COMMENT);
    }

    private static List<String> args(final String... arguments) {
        return newArrayList(arguments);
    }

    private static List<String> comment(final String... comments) {
        final List<String> cmts = newArrayList(comments);
        if (!cmts.isEmpty()) {
            cmts.set(0, "# " + cmts.get(0));
        }
        return cmts;
    }

    private static TestCase createTest() {
        final RobotFileOutput parentFileOutput = new RobotFileOutput(RobotVersion.from("3.1.0"));
        final RobotFile parent = new RobotFile(parentFileOutput);
        final TestCaseTable table = new TestCaseTable(parent);
        final TestCase test = new TestCase(RobotToken.create("test"));
        test.setParent(table);
        return test;
    }

    private static RobotExecutableRow<TestCase> createRow(final TestCase test, final String action,
            final List<String> args,
            final List<String> cmts) {
        final RobotExecutableRow<TestCase> row = new RobotExecutableRow<>();
        row.setParent(test);
        row.setAction(RobotToken.create(action));
        args.stream().map(RobotToken::create).forEach(row::addArgument);
        cmts.stream().map(RobotToken::create).forEach(row::addCommentPart);
        return row;
    }

    private static List<String> cellsOf(final RobotExecutableRow<?> row) {
        return row.getElementTokens().stream().map(RobotToken::getText).collect(toList());
    }

    private static List<IRobotTokenType> typesOf(final RobotExecutableRow<?> row) {
        return row.getElementTokens().stream().map(token -> token.getTypes().get(0)).collect(toList());
    }
}
