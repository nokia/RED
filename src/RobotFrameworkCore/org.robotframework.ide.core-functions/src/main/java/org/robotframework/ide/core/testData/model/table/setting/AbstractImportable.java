package org.robotframework.ide.core.testData.model.table.setting;

import org.robotframework.ide.core.testData.model.common.Comment;


/**
 * This is common element for Library, Resources and Variables used in Setting
 * Table. The field {@link #fileLastModified} was added for tracking if file was
 * changed after last read.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public abstract class AbstractImportable {

    private final ExternalFile externalFile;
    private final Comment comment = new Comment();


    /**
     * @param pathOrName
     *            path to file or in case of library it could be also it name
     */
    public AbstractImportable(String pathOrName) {
        this.externalFile = new ExternalFile(pathOrName);
    }


    /**
     * @return optional user comment
     */
    protected Comment getComment() {
        return comment;
    }


    /**
     * @return say if comment is available
     */
    public boolean isCommentPresent() {
        return this.comment.isPresent();
    }


    /**
     * @return user comment text
     */
    public String getCommentText() {
        return this.comment.getText();
    }


    /**
     * @param comment
     */
    public void setCommentText(String comment) {
        this.comment.setText(comment);
    }


    /**
     * @return materialized file referenced
     */
    public ExternalFile getExternalFile() {
        return externalFile;
    }

}
