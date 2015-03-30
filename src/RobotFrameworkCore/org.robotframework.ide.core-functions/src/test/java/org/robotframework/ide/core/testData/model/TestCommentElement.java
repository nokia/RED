package org.robotframework.ide.core.testData.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Test;


/**
 * 
 * @author wypych
 * @see Comment
 */
public class TestCommentElement {

    private Comment comment;


    @Test
    public void test_unsetPreviouslySetComment() {
        // prepare
        comment = new Comment("comment");

        // execute
        comment.clearText();

        // verify
        assertThat(comment.getText()).isNull();
        assertThat(comment.isPresent()).isFalse();
    }


    @Test
    public void test_setPreviouslyUnsetComment() {
        // prepare
        comment = new Comment();
        String commentText = "comment";

        // execute
        comment.setText(commentText);

        // verify
        assertThat(comment.getText()).isNotNull();
        assertThat(comment.getText()).isEqualTo(commentText);
        assertThat(comment.isPresent()).isTrue();
    }


    @Test
    public void test_construction_with_comment_set() {
        // prepare & execute
        String commentText = "comment";
        comment = new Comment(commentText);

        // verify
        assertThat(comment.getText()).isNotNull();
        assertThat(comment.getText()).isEqualTo(commentText);
        assertThat(comment.isPresent()).isTrue();
    }


    @Test
    public void test_construction_without_comment_set() {
        // prepare & execute
        comment = new Comment();

        // verify
        assertThat(comment.getText()).isNull();
        assertThat(comment.isPresent()).isFalse();
    }


    @After
    public void tearDown() {
        comment = null;
    }
}
