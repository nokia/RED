package org.robotframework.ide.core.testData.model.table.setting;

import org.robotframework.ide.core.testData.model.Comment;


/**
 * This is common element for Library, Resources and Variables used in Setting
 * Table.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public abstract class AbstractImportable {

    private String pathOrName;
    private final Comment comment = new Comment();


    /**
     * @param pathOrName
     *            path to file or in case of library it could be also it name
     */
    public AbstractImportable(String pathOrName) {
        this.pathOrName = pathOrName;
    }


    /**
     * @return library name, path to library, resource or file with variables
     */
    public String getPathOrName() {
        return pathOrName;
    }


    /**
     * @param pathOrName
     *            library name, path to library, resource or file with variables
     */
    public void setPathOrName(String pathOrName) {
        this.pathOrName = pathOrName;
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
}
