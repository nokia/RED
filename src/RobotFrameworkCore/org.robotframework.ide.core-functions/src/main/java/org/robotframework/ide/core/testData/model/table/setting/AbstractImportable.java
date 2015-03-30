package org.robotframework.ide.core.testData.model.table.setting;

import java.io.File;

import org.robotframework.ide.core.testData.model.Comment;


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

    private String pathOrName;
    private File resolvedFile;
    private final Comment comment = new Comment();
    private long fileLastModified = 0;


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


    /**
     * @param fileLastModified
     *            timestamp of reference file last modification as EPOCH
     */
    public void setFileLastModified(long fileLastModified) {
        this.fileLastModified = fileLastModified;
    }


    /**
     * @return timestamp of reference file last modification as EPOCH
     */
    public long getFileLastModified() {
        return fileLastModified;
    }


    /**
     * @param resolvedFile
     *            path to reference file
     */
    public void setResolvedReferenceFile(File resolvedFile) {
        this.resolvedFile = resolvedFile;
    }


    /**
     * @return resolved reference file
     */
    public File getResolvedReferenceFile() {
        return this.resolvedFile;
    }
}
