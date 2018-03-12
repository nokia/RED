/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.testdata.model.table;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class RobotEmptyRowTest {

    @Test
    public void emptyToken_isSetCorrectly() {
        // prepare
        final RobotEmptyRow<UserKeyword> row = new RobotEmptyRow<>();
        final RobotToken empty = new RobotToken();
        final String emptyText = " ";
        empty.setText(emptyText);

        // execute
        final boolean isSet = row.setEmptyToken(empty);

        // verify
        assertThat(isSet).isTrue();
        assertThat(row.getElementTokens()).containsExactly(empty);
        assertThat(row.getElementTokens().get(0).getText()).isEqualTo(emptyText);
    }

    @Test
    public void nonEmptyToken_isNotSet() {
        // prepare
        final RobotEmptyRow<UserKeyword> row = new RobotEmptyRow<>();
        final RobotToken nonEmpty = new RobotToken();
        final String nonEmptyText = "foo";
        nonEmpty.setText(nonEmptyText);

        // execute
        final boolean isSet = row.setEmptyToken(nonEmpty);

        // verify
        assertThat(isSet).isFalse();
        assertThat(row.getElementTokens()).doesNotContain(nonEmpty);
        assertThat(row.getElementTokens().get(0).getText()).isNotEqualTo(nonEmptyText);
    }

    @Test
    public void nonEmptyText_isNotEmpty() {
        // prepare
        final String nonEmptyText = "foo";

        // execute
        final boolean isSet = RobotEmptyRow.isEmpty(nonEmptyText);

        // verify
        assertThat(isSet).isFalse();
    }

    @Test
    public void emptyText_isEmpty() {
        // prepare
        final String emptyText = " ";

        // execute
        final boolean isSet = RobotEmptyRow.isEmpty(emptyText);

        // verify
        assertThat(isSet).isTrue();
    }

    @Test
    public void commentsCanNotBeChanged() {
        // prepare
        final RobotEmptyRow<UserKeyword> row = new RobotEmptyRow<>();
        final RobotToken comment = new RobotToken();
        final String text = "# comment";
        comment.setText(text);

        // execute
        row.clearComment();
        row.removeCommentPart(0);
        row.setComment("#foo");
        row.setComment(comment);
        row.addCommentPart(comment);

        // verify
        assertThat(row.getComment()).isEmpty();
    }

    @Test
    public void insertCell_doesNothing() {
        // prepare
        final RobotEmptyRow<UserKeyword> row = new RobotEmptyRow<>();

        // execute
        row.insertValueAt("foo", 0);
        row.insertValueAt("bar", 1);
        row.insertValueAt("foobar", 2);

        // verify
        assertThat(row.getElementTokens()).hasSize(1);
        assertThat(RobotEmptyRow.isEmpty(row.getElementTokens().get(0).getText())).isTrue();
    }
}
