package org.robotframework.ide.core.testData.model.table.setting;

import org.robotframework.ide.core.testData.model.Comment;


public abstract class AbstractImportable {

    private String pathOrName;
    private final Comment comment = new Comment();


    public AbstractImportable(String pathOrName) {
        this.pathOrName = pathOrName;
    }


    public String getPathOrName() {
        return pathOrName;
    }


    public void setPathOrName(String pathOrName) {
        this.pathOrName = pathOrName;
    }


    protected Comment getComment() {
        return comment;
    }


    public boolean isCommentPresent() {
        return this.comment.isPresent();
    }


    public String getCommentText() {
        return this.comment.getText();
    }


    public void setCommentText(String comment) {
        this.comment.setText(comment);
    }
}
