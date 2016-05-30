/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Test;
import org.rf.ide.core.testdata.model.ICommentHolder;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandlerTest.FakeCommentHolder.OperationEntry;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class CommentServiceHandlerTest {

    @Test
    public void test_read_threeCommentElement_oneToEscape_shouldReturn_textFromElements() {
        assertCommentConsolidation("text_me1 | \\| toEscape | text_me3", " | ", "text_me1", "| toEscape", "text_me3");
    }

    @Test
    public void test_read_threeCommentElement_oneIsEmptyString_shouldReturn_textFromElements() {
        assertCommentConsolidation("text_me1 |  | text_me3", " | ", "text_me1", "", "text_me3");
    }

    @Test
    public void test_read_threeCommentElement_shouldReturn_textFromElements() {
        assertCommentConsolidation("text_me1 | text_me2 | text_me3", " | ", "text_me1", "text_me2", "text_me3");
    }

    @Test
    public void test_read_oneCommentElement_shouldReturn_textFromElement() {
        assertCommentConsolidation("text_me1", " | ", "text_me1");
    }

    @Test
    public void test_read_emptyComment_shouldReturn_emptyString() {
        assertCommentConsolidation("", " | ");
    }

    private void assertCommentConsolidation(final String textExpected, final String commentTokenSeparator,
            final String... tokens) {
        // prepare
        List<RobotToken> comment = new ArrayList<>(0);
        for (String t : tokens) {
            comment.add(token(t));
        }
        FakeCommentHolder commentElement = new FakeCommentHolder(comment);

        // execute
        final String toShow = CommentServiceHandler.consolidate(commentElement, commentTokenSeparator);

        // verify
        assertThat(toShow).isEqualTo(textExpected);
        List<OperationEntry> operationsVsData = commentElement.operationsVsData;
        assertThat(operationsVsData).hasSize(1);
        OperationEntry operationEntry = operationsVsData.get(0);
        assertThat(operationEntry.getKey()).isEqualTo("getComment<VOID>");
        assertThat(operationEntry.getValue()).isEmpty();
    }

    @Test
    public void test_unescape_oneElementToUnEscapeWhitespaceAtTheBeginning_shouldReturn_unescapedText() {
        assertThat(CommentServiceHandler.unescape(" \\| to_unescape", " | ")).isEqualTo(" | to_unescape");
    }

    @Test
    public void test_unescape_oneElementToUnEscape_shouldReturn_unescapedText() {
        assertThat(CommentServiceHandler.unescape("\\| to_unescape", " | ")).isEqualTo("| to_unescape");
    }

    @Test
    public void test_unescape_nothingToUnEscape_shouldReturn_theSameText() {
        assertThat(CommentServiceHandler.unescape("nothing_to_unescape", " | ")).isEqualTo("nothing_to_unescape");
    }

    @Test
    public void test_escape_oneElementToEscapeWhitespaceAtTheBeginning_shouldReturn_escapedText() {
        assertThat(CommentServiceHandler.escape(" | to_escape", " | ")).isEqualTo(" \\| to_escape");
    }

    @Test
    public void test_escape_oneElementToEscape_shouldReturn_escapedText() {
        assertThat(CommentServiceHandler.escape("| to_escape", " | ")).isEqualTo("\\| to_escape");
    }

    @Test
    public void test_escape_nothingToEscape_shouldReturn_theSameText() {
        assertThat(CommentServiceHandler.escape("nothing_to_escape", " | ")).isEqualTo("nothing_to_escape");
    }

    public static class FakeCommentHolder implements ICommentHolder {

        private List<OperationEntry> operationsVsData = new ArrayList<>(0);

        private final List<RobotToken> comment;

        public FakeCommentHolder(final List<RobotToken> tokens) {
            this.comment = tokens;
        }

        public static class OperationEntry implements Entry<String, List<Object>> {

            private final String methodName;

            private List<Object> methodParameters = new ArrayList<>(0);

            public OperationEntry(final String methodName) {
                this.methodName = methodName;
            }

            @Override
            public String getKey() {
                return methodName;
            }

            @Override
            public List<Object> getValue() {
                return methodParameters;
            }

            @Override
            public List<Object> setValue(List<Object> value) {
                this.methodParameters = value;
                return value;
            }
        }

        @Override
        public List<RobotToken> getComment() {
            operationsVsData.add(new OperationEntry("getComment<VOID>"));
            return comment;
        }

        @Override
        public void setComment(String commentText) {
            OperationEntry currentOper = new OperationEntry("setComment<ONE_PARAM_STR>");
            currentOper.setValue(new ArrayList<Object>(Arrays.asList(commentText)));
            operationsVsData.add(currentOper);

        }

        @Override
        public void setComment(RobotToken commentText) {
            OperationEntry currentOper = new OperationEntry("setComment<ONE_PARAM_RobotToken>");
            currentOper.setValue(new ArrayList<Object>(Arrays.asList(commentText)));
            operationsVsData.add(currentOper);

            comment.clear();
            comment.add(commentText);
        }

        @Override
        public void addCommentPart(RobotToken cmPart) {
            OperationEntry currentOper = new OperationEntry("addCommentPart<ONE_PARAM_RobotToken>");
            currentOper.setValue(new ArrayList<Object>(Arrays.asList(cmPart)));
            operationsVsData.add(currentOper);

            comment.add(cmPart);
        }

        @Override
        public void removeCommentPart(int index) {
            OperationEntry currentOper = new OperationEntry("removeCommentPart<ONE_PARAM_INT>");
            currentOper.setValue(new ArrayList<Object>(Arrays.asList(index)));
            operationsVsData.add(currentOper);

            comment.remove(index);
        }
    }

    private RobotToken token(final String text) {
        RobotToken tok = new RobotToken();
        tok.setText(text);

        return tok;
    }
}
