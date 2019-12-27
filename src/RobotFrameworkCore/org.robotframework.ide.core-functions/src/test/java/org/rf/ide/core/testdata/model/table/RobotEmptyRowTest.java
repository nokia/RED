/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.testdata.model.table;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class RobotEmptyRowTest {

    @Test
    public void emptyToken_isSetCorrectly() {
        final RobotEmptyRow<?> row = new RobotEmptyRow<>();
        final RobotToken empty = new RobotToken();
        final String emptyText = " ";
        empty.setText(emptyText);

        row.setEmpty(empty);

        assertThat(row.getElementTokens()).containsExactly(empty);
        assertThat(row.getElementTokens().get(0).getText()).isEqualTo(emptyText);
    }

    @Test
    public void nonEmptyToken_isSet() {
        final RobotEmptyRow<?> row = new RobotEmptyRow<>();
        final RobotToken nonEmpty = new RobotToken();
        final String nonEmptyText = "foo";
        nonEmpty.setText(nonEmptyText);

        row.setEmpty(nonEmpty);

        assertThat(row.getElementTokens()).containsExactly(nonEmpty);
        assertThat(row.getElementTokens().get(0).getText()).isEqualTo(nonEmptyText);
    }

    @Test
    public void nonEmptyText_isNotEmpty() {
        final boolean isSet = RobotEmptyRow.isEmpty("foo");
        assertThat(isSet).isFalse();
    }

    @Test
    public void emptyText_isEmpty() {
        final boolean isSet = RobotEmptyRow.isEmpty(" ");
        assertThat(isSet).isTrue();
    }

    @Test
    public void tokenCreationDoesNothing_whenInsertingOutOfTheRow() {
        final TestCase test = new TestCase(RobotToken.create("test"));
        final RobotEmptyRow<TestCase> row = createEmptyRow(test, "\\", comment("c1", "c2"));

        row.createToken(-1);
        row.createToken(6);
        row.createToken(7);
        row.createToken(10);

        assertThat(cellsOf(row)).containsExactly("\\", "# c1", "c2");
        assertThat(typesOf(row)).containsExactly(RobotTokenType.EMPTY_CELL, RobotTokenType.START_HASH_COMMENT,
                RobotTokenType.COMMENT_CONTINUE);
    }

    @Test
    public void tokenCreationInsertsEmptyToken_whenInsertingFirstCommentTokenInCommentOnlyRow() {
        final TestCase test = new TestCase(RobotToken.create("test"));
        final RobotEmptyRow<TestCase> row = createCommentRow(test, comment("c1", "c2"));

        row.createToken(0);

        assertThat(cellsOf(row)).containsExactly("", "# c1", "c2");
        assertThat(typesOf(row)).containsExactly(RobotTokenType.EMPTY_CELL, RobotTokenType.START_HASH_COMMENT,
                RobotTokenType.COMMENT_CONTINUE);
    }

    @Test
    public void tokenCreationIsNotPossible_whenInsertingOnEmptyTokenOrFirstCommentInEmptyRow() {
        final TestCase test = new TestCase(RobotToken.create("test"));
        final RobotEmptyRow<TestCase> row1 = createEmptyRow(test, "\\", comment("c1", "c2"));
        final RobotEmptyRow<TestCase> row2 = createEmptyRow(test, "\\", comment("c1", "c2"));

        assertThatIllegalArgumentException().isThrownBy(() -> row1.createToken(0));
        assertThatIllegalArgumentException().isThrownBy(() -> row2.createToken(1));
    }

    @Test
    public void tokenCreationInsertsEmptyComment_whenInsertingAtNonFirstComment() {
        final TestCase test = new TestCase(RobotToken.create("test"));
        final RobotEmptyRow<TestCase> row1 = createEmptyRow(test, "\\", comment("c1", "c2"));
        final RobotEmptyRow<TestCase> row2 = createCommentRow(test, comment("c1", "c2"));

        row1.createToken(2);
        row2.createToken(1);

        assertThat(cellsOf(row1)).containsExactly("\\", "# c1", "", "c2");
        assertThat(typesOf(row1)).containsExactly(RobotTokenType.EMPTY_CELL,
                RobotTokenType.START_HASH_COMMENT, RobotTokenType.COMMENT_CONTINUE, RobotTokenType.COMMENT_CONTINUE);

        assertThat(cellsOf(row2)).containsExactly("# c1", "", "c2");
        assertThat(typesOf(row2)).containsExactly(RobotTokenType.START_HASH_COMMENT, RobotTokenType.COMMENT_CONTINUE,
                RobotTokenType.COMMENT_CONTINUE);
    }

    @Test
    public void tokenUpdateDoesNothing_whenNegativeIndexIsGiven() {
        final TestCase test = new TestCase(RobotToken.create("test"));
        final RobotEmptyRow<TestCase> row = createEmptyRow(test, "\\", comment("c1", "c2"));

        row.updateToken(-1, "a");
        row.updateToken(-5, "b");
        row.updateToken(-10, "c");

        assertThat(cellsOf(row)).containsExactly("\\", "# c1", "c2");
        assertThat(typesOf(row)).containsExactly(RobotTokenType.EMPTY_CELL, RobotTokenType.START_HASH_COMMENT,
                RobotTokenType.COMMENT_CONTINUE);
    }

    @Test
    public void tokenUpdateIsNotPossibleOnCommentRow_whenUpdatingFirstElementToEmptyAndThereIsUncommentedContinuation() {
        final TestCase test = new TestCase(RobotToken.create("test"));
        final RobotEmptyRow<TestCase> row = createCommentRow(test, comment("c1", "c2"));

        assertThatIllegalArgumentException().isThrownBy(() -> row.updateToken(0, ""));
        assertThatIllegalArgumentException().isThrownBy(() -> row.updateToken(0, "\\"));
    }

    @Test
    public void tokenUpdateIsNotPossibleOnCommentRow_whenUpdatingFirstElementToNonEmptyButThereIsCommentedContinuation() {
        final TestCase test = new TestCase(RobotToken.create("test"));
        final RobotEmptyRow<TestCase> row = createCommentRow(test, comment("c1", "# c2"));

        assertThatIllegalArgumentException().isThrownBy(() -> row.updateToken(0, "non-empty"));
    }

    @Test
    public void tokenUpdateChangesCommentTokenIntoEmptyOnCommentRow_whenThereIsCommentedContinuation_1() {
        final TestCase test = new TestCase(RobotToken.create("test"));
        final RobotEmptyRow<TestCase> row = createCommentRow(test, comment("c1", "# c2"));

        row.updateToken(0, "");

        assertThat(cellsOf(row)).containsExactly("", "# c2");
        assertThat(typesOf(row)).containsExactly(RobotTokenType.EMPTY_CELL, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void tokenUpdateChangesCommentTokenIntoEmptyOnCommentRow_whenThereIsCommentedContinuation_2() {
        final TestCase test = new TestCase(RobotToken.create("test"));
        final RobotEmptyRow<TestCase> row = createCommentRow(test, comment("c1", "# c2"));

        row.updateToken(0, "\\");

        assertThat(cellsOf(row)).containsExactly("\\", "# c2");
        assertThat(typesOf(row)).containsExactly(RobotTokenType.EMPTY_CELL, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void tokenUpdateChangesCommentInCommentRow_whenGivenValueIsCommentToo() {
        final TestCase test = new TestCase(RobotToken.create("test"));
        final RobotEmptyRow<TestCase> row = createCommentRow(test, comment("c1", "c2"));

        row.updateToken(0, "# other");

        assertThat(cellsOf(row)).containsExactly("# other", "c2");
        assertThat(typesOf(row)).containsExactly(RobotTokenType.START_HASH_COMMENT, RobotTokenType.COMMENT_CONTINUE);
    }

    @Test
    public void tokenUpdateIsNotPossibleInEmptyRow_whenTryingToChangeEmptyTokenToNonEmpty() {
        final TestCase test = new TestCase(RobotToken.create("test"));
        final RobotEmptyRow<TestCase> row = createEmptyRow(test, "", comment("c1", "c2"));

        assertThatIllegalArgumentException().isThrownBy(() -> row.updateToken(0, "non empty"));
    }

    @Test
    public void tokenUpdateChangesEmptyTokenInEmptyRow_whenUpdatingFirstElement_1() {
        final TestCase test = new TestCase(RobotToken.create("test"));
        final RobotEmptyRow<TestCase> row = createEmptyRow(test, "", comment("c1", "c2"));

        row.updateToken(0, "\\");

        assertThat(cellsOf(row)).containsExactly("\\", "# c1", "c2");
        assertThat(typesOf(row)).containsExactly(RobotTokenType.EMPTY_CELL, RobotTokenType.START_HASH_COMMENT,
                RobotTokenType.COMMENT_CONTINUE);
    }

    @Test
    public void tokenUpdateChangesEmptyTokenInEmptyRow_whenThereIsOneAlready_2() {
        final TestCase test = new TestCase(RobotToken.create("test"));
        final RobotEmptyRow<TestCase> row = createEmptyRow(test, "\\", comment("c1", "c2"));

        row.updateToken(0, "");

        assertThat(cellsOf(row)).containsExactly("", "# c1", "c2");
        assertThat(typesOf(row)).containsExactly(RobotTokenType.EMPTY_CELL, RobotTokenType.START_HASH_COMMENT,
                RobotTokenType.COMMENT_CONTINUE);
    }

    @Test
    public void tokenUpdateChangesEmptyTokenInEmptyRow_whenUpdatingToComment() {
        final TestCase test = new TestCase(RobotToken.create("test"));
        final RobotEmptyRow<TestCase> row = createEmptyRow(test, "\\", comment("c1", "c2"));

        row.updateToken(0, "# comment");

        assertThat(cellsOf(row)).containsExactly("# comment", "# c1", "c2");
        assertThat(typesOf(row)).containsExactly(RobotTokenType.START_HASH_COMMENT, RobotTokenType.COMMENT_CONTINUE,
                RobotTokenType.COMMENT_CONTINUE);
    }

    @Test
    public void tokenUpdateChangesCommentTokenInCommentRow_whenUpdatingAnyElement() {
        final TestCase test = new TestCase(RobotToken.create("test"));
        final RobotEmptyRow<TestCase> row1 = createCommentRow(test, comment("c1", "c2"));
        final RobotEmptyRow<TestCase> row2 = createCommentRow(test, comment("c1", "c2"));
        final RobotEmptyRow<TestCase> row3 = createCommentRow(test, comment("c1", "c2"));
        final RobotEmptyRow<TestCase> row4 = createCommentRow(test, comment("c1", "c2"));

        row1.updateToken(0, "# d1");
        row2.updateToken(1, "d2");
        row3.updateToken(2, "d3");
        row4.updateToken(3, "d4");

        assertThat(cellsOf(row1)).containsExactly("# d1", "c2");
        assertThat(typesOf(row1)).containsExactly(RobotTokenType.START_HASH_COMMENT, RobotTokenType.COMMENT_CONTINUE);

        assertThat(cellsOf(row2)).containsExactly("# c1", "d2");
        assertThat(typesOf(row2)).containsExactly(RobotTokenType.START_HASH_COMMENT, RobotTokenType.COMMENT_CONTINUE);

        assertThat(cellsOf(row3)).containsExactly("# c1", "c2", "d3");
        assertThat(typesOf(row3)).containsExactly(RobotTokenType.START_HASH_COMMENT, RobotTokenType.COMMENT_CONTINUE,
                RobotTokenType.COMMENT_CONTINUE);

        assertThat(cellsOf(row4)).containsExactly("# c1", "c2", "\\", "d4");
        assertThat(typesOf(row4)).containsExactly(RobotTokenType.START_HASH_COMMENT, RobotTokenType.COMMENT_CONTINUE,
                RobotTokenType.COMMENT_CONTINUE, RobotTokenType.COMMENT_CONTINUE);
    }

    @Test
    public void tokenUpdateChangesCommentTokenInEmptyRow_whenUpdatingAnyElement() {
        final TestCase test = new TestCase(RobotToken.create("test"));
        final RobotEmptyRow<TestCase> row1 = createEmptyRow(test, "\\", comment("c1", "c2"));
        final RobotEmptyRow<TestCase> row2 = createEmptyRow(test, "\\", comment("c1", "c2"));
        final RobotEmptyRow<TestCase> row3 = createEmptyRow(test, "\\", comment("c1", "c2"));
        final RobotEmptyRow<TestCase> row4 = createEmptyRow(test, "\\", comment("c1", "c2"));

        row1.updateToken(1, "# d1");
        row2.updateToken(2, "d2");
        row3.updateToken(3, "d3");
        row4.updateToken(4, "d4");

        assertThat(cellsOf(row1)).containsExactly("\\", "# d1", "c2");
        assertThat(typesOf(row1)).containsExactly(RobotTokenType.EMPTY_CELL, RobotTokenType.START_HASH_COMMENT,
                RobotTokenType.COMMENT_CONTINUE);

        assertThat(cellsOf(row2)).containsExactly("\\", "# c1", "d2");
        assertThat(typesOf(row2)).containsExactly(RobotTokenType.EMPTY_CELL, RobotTokenType.START_HASH_COMMENT,
                RobotTokenType.COMMENT_CONTINUE);

        assertThat(cellsOf(row3)).containsExactly("\\", "# c1", "c2", "d3");
        assertThat(typesOf(row3)).containsExactly(RobotTokenType.EMPTY_CELL, RobotTokenType.START_HASH_COMMENT,
                RobotTokenType.COMMENT_CONTINUE, RobotTokenType.COMMENT_CONTINUE);

        assertThat(cellsOf(row4)).containsExactly("\\", "# c1", "c2", "\\", "d4");
        assertThat(typesOf(row4)).containsExactly(RobotTokenType.EMPTY_CELL, RobotTokenType.START_HASH_COMMENT,
                RobotTokenType.COMMENT_CONTINUE, RobotTokenType.COMMENT_CONTINUE, RobotTokenType.COMMENT_CONTINUE);
    }

    @Test
    public void tokenUpdateIsNotPossibleInEmptyRow_whenUpdatingFirstElementToNonComment() {
        final TestCase test = new TestCase(RobotToken.create("test"));
        final RobotEmptyRow<TestCase> row = createEmptyRow(test, "\\", comment("c1", "c2"));

        assertThatIllegalArgumentException().isThrownBy(() -> row.updateToken(1, "non-comment"));
    }

    @Test
    public void tokenDeletionDoesNothing_whenIndexIsOutOfTheRow() {
        final TestCase test = new TestCase(RobotToken.create("test"));
        final RobotEmptyRow<TestCase> row = createEmptyRow(test, "\\", comment("c1", "c2"));

        row.deleteToken(-1);
        row.deleteToken(5);
        row.deleteToken(6);
        row.deleteToken(10);

        assertThat(cellsOf(row)).containsExactly("\\", "# c1", "c2");
        assertThat(typesOf(row)).containsExactly(RobotTokenType.EMPTY_CELL,
                RobotTokenType.START_HASH_COMMENT, RobotTokenType.COMMENT_CONTINUE);
    }

    @Test
    public void tokenDeletionIsNotPossible_whenRemovingFirstCommentAndThereIsNoAnother() {
        final TestCase test = new TestCase(RobotToken.create("test"));
        final RobotEmptyRow<TestCase> row1 = createCommentRow(test, comment("c1", "c2"));
        final RobotEmptyRow<TestCase> row2 = createEmptyRow(test, "\\", comment("c1", "c2"));

        assertThatIllegalArgumentException().isThrownBy(() -> row1.deleteToken(0));
        assertThatIllegalArgumentException().isThrownBy(() -> row2.deleteToken(1));
    }

    @Test
    public void tokenDeletionChangesCommentRowIntoEmptyRow_whenRemovingFirstCommentAndThereIsEmpty() {
        final TestCase test = new TestCase(RobotToken.create("test"));
        final RobotEmptyRow<TestCase> row1 = createCommentRow(test, comment("c1", "\\"));
        final RobotEmptyRow<TestCase> row2 = createCommentRow(test, comment("c1", "\\", "# c3"));

        row1.deleteToken(0);
        row2.deleteToken(0);

        assertThat(cellsOf(row1)).containsExactly("\\");
        assertThat(typesOf(row1)).containsExactly(RobotTokenType.EMPTY_CELL);

        assertThat(cellsOf(row2)).containsExactly("\\", "# c3");
        assertThat(typesOf(row2)).containsExactly(RobotTokenType.EMPTY_CELL, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void tokenDeletionChangesEmptyRowToCommentOnlyRow_whenRemovingOnEmptyToken() {
        final TestCase test = new TestCase(RobotToken.create("test"));
        final RobotEmptyRow<TestCase> row = createEmptyRow(test, "\\", comment("c1", "c2"));

        row.deleteToken(0);

        assertThat(cellsOf(row)).containsExactly("# c1", "c2");
        assertThat(typesOf(row)).containsExactly(RobotTokenType.START_HASH_COMMENT, RobotTokenType.COMMENT_CONTINUE);
    }

    @Test
    public void tokenDeletionRemovesComment_whenRemovingFirstCommentAndThereIsAnother() {
        final TestCase test = new TestCase(RobotToken.create("test"));
        final RobotEmptyRow<TestCase> row1 = createCommentRow(test, comment("c1"));
        final RobotEmptyRow<TestCase> row2 = createCommentRow(test, comment("c1", "# c2"));
        final RobotEmptyRow<TestCase> row3 = createEmptyRow(test, "\\", comment("c1"));
        final RobotEmptyRow<TestCase> row4 = createEmptyRow(test, "\\", comment("c1", "# c2"));

        row1.deleteToken(0);
        row2.deleteToken(0);
        row3.deleteToken(1);
        row4.deleteToken(1);

        assertThat(cellsOf(row1)).containsExactly("");
        assertThat(typesOf(row1)).containsExactly(RobotTokenType.UNKNOWN);

        assertThat(cellsOf(row2)).containsExactly("# c2");
        assertThat(typesOf(row2)).containsExactly(RobotTokenType.START_HASH_COMMENT);

        assertThat(cellsOf(row3)).containsExactly("\\");
        assertThat(typesOf(row3)).containsExactly(RobotTokenType.EMPTY_CELL);

        assertThat(cellsOf(row4)).containsExactly("\\", "# c2");
        assertThat(typesOf(row4)).containsExactly(RobotTokenType.EMPTY_CELL, RobotTokenType.START_HASH_COMMENT);
    }

    @Test
    public void tokenDeletionRemovesComment_whenRemovingNonFirstCommentTokens() {
        final TestCase test = new TestCase(RobotToken.create("test"));
        final RobotEmptyRow<TestCase> row1 = createCommentRow(test, comment("c1", "c2"));
        final RobotEmptyRow<TestCase> row2 = createCommentRow(test, comment("c1", "c2", "c3"));
        final RobotEmptyRow<TestCase> row3 = createEmptyRow(test, "\\", comment("c1", "c2"));
        final RobotEmptyRow<TestCase> row4 = createEmptyRow(test, "\\", comment("c1", "c2", "c3"));

        row1.deleteToken(1);
        row2.deleteToken(1);
        row3.deleteToken(2);
        row4.deleteToken(2);

        assertThat(cellsOf(row1)).containsExactly("# c1");
        assertThat(typesOf(row1)).containsExactly(RobotTokenType.START_HASH_COMMENT);

        assertThat(cellsOf(row2)).containsExactly("# c1", "c3");
        assertThat(typesOf(row2)).containsExactly(RobotTokenType.START_HASH_COMMENT, RobotTokenType.COMMENT_CONTINUE);

        assertThat(cellsOf(row3)).containsExactly("\\", "# c1");
        assertThat(typesOf(row3)).containsExactly(RobotTokenType.EMPTY_CELL, RobotTokenType.START_HASH_COMMENT);

        assertThat(cellsOf(row4)).containsExactly("\\", "# c1", "c3");
        assertThat(typesOf(row4)).containsExactly(RobotTokenType.EMPTY_CELL, RobotTokenType.START_HASH_COMMENT,
                RobotTokenType.COMMENT_CONTINUE);
    }

    @Test
    public void elementTokensAreCorrectlyReturned() throws Exception {
        final TestCase test = new TestCase(RobotToken.create("test"));
        final RobotEmptyRow<TestCase> empty = createEmptyRow(test, "", comment());
        final RobotEmptyRow<TestCase> backslash = createEmptyRow(test, "\\", comment());
        final RobotEmptyRow<TestCase> comment = createCommentRow(test, comment("c1", "c2"));
        final RobotEmptyRow<TestCase> unknown = createCommentRow(test, comment());
        final RobotEmptyRow<TestCase> backslashWithComment = createEmptyRow(test, "\\", comment("c1", "c2"));

        assertThat(cellsOf(empty)).containsExactly("");
        assertThat(typesOf(empty)).containsExactly(RobotTokenType.EMPTY_CELL);

        assertThat(cellsOf(backslash)).containsExactly("\\");
        assertThat(typesOf(backslash)).containsExactly(RobotTokenType.EMPTY_CELL);

        assertThat(cellsOf(comment)).containsExactly("# c1", "c2");
        assertThat(typesOf(comment)).containsExactly(RobotTokenType.START_HASH_COMMENT,
                RobotTokenType.COMMENT_CONTINUE);

        assertThat(cellsOf(unknown)).containsExactly("");
        assertThat(typesOf(unknown)).containsExactly(RobotTokenType.UNKNOWN);

        assertThat(cellsOf(backslashWithComment)).containsExactly("\\", "# c1", "c2");
        assertThat(typesOf(backslashWithComment)).containsExactly(RobotTokenType.EMPTY_CELL,
                RobotTokenType.START_HASH_COMMENT, RobotTokenType.COMMENT_CONTINUE);
    }

    private static List<String> comment(final String... comments) {
        final List<String> cmts = newArrayList(comments);
        if (!cmts.isEmpty()) {
            cmts.set(0, "# " + cmts.get(0));
        }
        return cmts;
    }

    private static RobotEmptyRow<TestCase> createCommentRow(final TestCase test, final List<String> cmts) {
        final RobotEmptyRow<TestCase> row = new RobotEmptyRow<>();
        row.setParent(test);
        cmts.stream().map(RobotToken::create).forEach(row::addCommentPart);
        return row;
    }

    private static RobotEmptyRow<TestCase> createEmptyRow(final TestCase test, final String empty,
            final List<String> cmts) {
        final RobotEmptyRow<TestCase> row = new RobotEmptyRow<>();
        row.setParent(test);
        final RobotToken emptyToken = RobotToken.create(empty);
        emptyToken.markAsDirty();
        row.setEmpty(emptyToken);
        cmts.stream().map(RobotToken::create).forEach(row::addCommentPart);
        return row;
    }

    private static List<String> cellsOf(final RobotEmptyRow<?> row) {
        return row.getElementTokens().stream().map(RobotToken::getText).collect(toList());
    }

    private static List<IRobotTokenType> typesOf(final RobotEmptyRow<?> row) {
        return row.getElementTokens().stream().map(token -> token.getTypes().get(0)).collect(toList());
    }
}
