/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.rf.ide.core.testdata.model.ICommentHolder;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler.ETokenSeparator;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler.ITokenSeparatorPresenter;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class CommentServiceHandlerTest {

    @Test
    public void test_update_withOneCommentElement_with2ValuesToUnescape() {
        // prepare
        final FakeCommentHolder fComment = new FakeCommentHolder();
        final List<RobotToken> commentParts = newArrayList(token("tok1"), token("tok2\\ | "), token("tok3"));
        fComment.commentToks = commentParts;

        // execute
        CommentServiceHandler.update(fComment, ETokenSeparator.PIPE_WRAPPED_WITH_SPACE, "new comment2\\ | \\ | ");

        // verify
        assertThat(text(fComment.getComment())).containsExactly("new comment2 |  | ");
    }

    @Test
    public void test_update_withThreeCommentPart_withoutAnyEscape() {
        // prepare
        final FakeCommentHolder fComment = new FakeCommentHolder();
        final List<RobotToken> commentParts = newArrayList(token("tok1"), token("tok2\\ | "), token("tok3"));
        fComment.commentToks = commentParts;

        // execute
        CommentServiceHandler.update(fComment, ETokenSeparator.PIPE_WRAPPED_WITH_SPACE,
                "new comment | new comment 2 | new comment 3");

        // verify
        assertThat(text(fComment.getComment())).containsExactly("new comment", "new comment 2", "new comment 3");
    }

    @Test
    public void test_update_withOneCommentPart_withoutAnyEscape() {
        // prepare
        final FakeCommentHolder fComment = new FakeCommentHolder();
        final List<RobotToken> commentParts = newArrayList(token("tok1"), token("tok2\\ | "), token("tok3"));
        fComment.commentToks = commentParts;

        // execute
        CommentServiceHandler.update(fComment, ETokenSeparator.PIPE_WRAPPED_WITH_SPACE, "new comment");

        // verify
        assertThat(text(fComment.getComment())).containsExactly("new comment");
    }

    @Test
    public void test_consolidate_withThreeCommentTexts_withMiddleValueToEscape() {
        // prepare
        final FakeCommentHolder fComment = new FakeCommentHolder();
        final List<RobotToken> commentParts = newArrayList(token("tok1"), token("tok2\\ | "), token("tok3"));
        fComment.commentToks = commentParts;

        // execute
        final String toShow = CommentServiceHandler.consolidate(fComment, ETokenSeparator.PIPE_WRAPPED_WITH_SPACE);

        // verify
        assertThat(toShow).isEqualTo("tok1 | tok2\\ |  | tok3");
    }

    @Test
    public void test_consolidate_withOneCommentText_with2ValuesToEscape() {
        // prepare
        final FakeCommentHolder fComment = new FakeCommentHolder();
        final List<RobotToken> commentParts = newArrayList(token("tok1 |  | "));
        fComment.commentToks = commentParts;

        // execute
        final String toShow = CommentServiceHandler.consolidate(fComment, ETokenSeparator.PIPE_WRAPPED_WITH_SPACE);

        // verify
        assertThat(toShow).isEqualTo("tok1\\ | \\ | ");
    }

    @Test
    public void test_consolidate_withOneCommentText_withValueToEscape() {
        // prepare
        final FakeCommentHolder fComment = new FakeCommentHolder();
        final List<RobotToken> commentParts = newArrayList(token("tok1 | "));
        fComment.commentToks = commentParts;

        // execute
        final String toShow = CommentServiceHandler.consolidate(fComment, ETokenSeparator.PIPE_WRAPPED_WITH_SPACE);

        // verify
        assertThat(toShow).isEqualTo("tok1\\ | ");
    }

    @Test
    public void test_consolidate_withThreeCommentTexts_withoutValueToEscape() {
        // prepare
        final FakeCommentHolder fComment = new FakeCommentHolder();
        final List<RobotToken> commentParts = newArrayList(token("tok1"), token("tok2"), token("tok3"));
        fComment.commentToks = commentParts;

        // execute
        final String toShow = CommentServiceHandler.consolidate(fComment, ETokenSeparator.PIPE_WRAPPED_WITH_SPACE);

        // verify
        assertThat(toShow).isEqualTo("tok1 | tok2 | tok3");
    }

    @Test
    public void test_consolidate_withOneCommentText_withoutValueToEscape() {
        // prepare
        final FakeCommentHolder fComment = new FakeCommentHolder();
        final List<RobotToken> commentParts = newArrayList(token("tok1"));
        fComment.commentToks = commentParts;

        // execute
        final String toShow = CommentServiceHandler.consolidate(fComment, ETokenSeparator.PIPE_WRAPPED_WITH_SPACE);

        // verify
        assertThat(toShow).isEqualTo("tok1");
    }

    @Test
    public void test_consolidate_withEmptyCommentText_shouldReturn_emptyText() {
        // prepare
        final FakeCommentHolder fComment = new FakeCommentHolder();

        // execute
        final String toShow = CommentServiceHandler.consolidate(fComment, ETokenSeparator.PIPE_WRAPPED_WITH_SPACE);

        // verify
        assertThat(toShow).isEmpty();
    }

    @Test
    public void test_ETokenSeparator_PIPE_WRAPPED_WITH_SPACE_pattern_toSplit_textWithUnEscapedPipeAtTheEnd_shouldReturn_twoElementList() {
        // prepare
        ITokenSeparatorPresenter separatorInView = CommentServiceHandler.ETokenSeparator.PIPE_WRAPPED_WITH_SPACE;

        // execute
        final String text = "nowy | ";
        List<String> splitted = separatorInView.splitTextFromViewBySeparator(text);

        // verify
        assertThat(splitted).containsExactly("nowy", "");
    }

    @Test
    public void test_ETokenSeparator_PIPE_WRAPPED_WITH_SPACE_pattern_toSplit_textWithUnEscapedPipeAndEscapedPipe_shouldReturn_twoElementList() {
        // prepare
        ITokenSeparatorPresenter separatorInView = CommentServiceHandler.ETokenSeparator.PIPE_WRAPPED_WITH_SPACE;

        // execute
        final String text = "nowy | textowy\\ | text";
        List<String> splitted = separatorInView.splitTextFromViewBySeparator(text);

        // verify
        assertThat(splitted).containsExactly("nowy", "textowy\\ | text");
    }

    @Test
    public void test_ETokenSeparator_PIPE_WRAPPED_WITH_SPACE_pattern_toSplit_textWithEscapedPipe_shouldReturn_oneElementList() {
        // prepare
        ITokenSeparatorPresenter separatorInView = CommentServiceHandler.ETokenSeparator.PIPE_WRAPPED_WITH_SPACE;

        // execute
        final String text = "nowy\\ | text";
        List<String> splitted = separatorInView.splitTextFromViewBySeparator(text);

        // verify
        assertThat(splitted).containsExactly("nowy\\ | text");
    }

    @Test
    public void test_ETokenSeparator_PIPE_WRAPPED_WITH_SPACE_pattern_toSplit_textWithNotEscapedPipe_shouldReturn_twoElementsList() {
        // prepare
        ITokenSeparatorPresenter separatorInView = CommentServiceHandler.ETokenSeparator.PIPE_WRAPPED_WITH_SPACE;

        // execute
        final String text = "nowy | text";
        List<String> splitted = separatorInView.splitTextFromViewBySeparator(text);

        // verify
        assertThat(splitted).containsExactly("nowy", "text");
    }

    @Test
    public void test_ETokenSeparator_PIPE_WRAPPED_WITH_SPACE_pattern_toSplit_emptyText_shouldReturn_emptyArray() {
        // prepare
        ITokenSeparatorPresenter separatorInView = CommentServiceHandler.ETokenSeparator.PIPE_WRAPPED_WITH_SPACE;

        // execute
        final String text = "";
        List<String> splitted = separatorInView.splitTextFromViewBySeparator(text);

        // verify
        assertThat(splitted).containsExactly("");
    }

    @Test
    public void test_ETokenSeparator_PIPE_WRAPPED_WITH_SPACE_getSeparatorAsText_shouldReturn_SPACE_PIPE_SPACE() {
        // prepare
        ITokenSeparatorPresenter separatorInView = CommentServiceHandler.ETokenSeparator.PIPE_WRAPPED_WITH_SPACE;

        // execute & verify
        assertThat(separatorInView.getSeparatorAsText()).isEqualTo(" | ");
    }

    private List<String> text(final List<RobotToken> toks) {
        List<String> texts = new ArrayList<>(0);
        for (final RobotToken t : toks) {
            texts.add(t.getText());
        }

        return texts;
    }

    private RobotToken token(final String text) {
        RobotToken tok = new RobotToken();
        tok.setText(text);

        return tok;
    }

    private class FakeCommentHolder implements ICommentHolder {

        List<RobotToken> commentToks = new ArrayList<>();

        @Override
        public List<RobotToken> getComment() {
            return commentToks;
        }

        @Override
        public void setComment(final String comment) {

        }

        @Override
        public void setComment(final RobotToken comment) {

        }

        @Override
        public void addCommentPart(final RobotToken cmPart) {
            commentToks.add(cmPart);
        }

        @Override
        public void removeCommentPart(final int index) {

        }

        @Override
        public void clearComment() {
            commentToks.clear();
        }
    }
}
