package org.robotframework.ide.core.testData.model.common;

/**
 * Represents comment in Test Data files, because comment is always '#' it could
 * be used in any section.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public class Comment implements IOptional {

    private String comment;
    private boolean isExists = false;


    /**
     * Comment will be set to {@code null} and flag will be unavailable
     */
    public Comment() {
        comment = null;
        isExists = false;
    }


    /**
     * @param comment
     *            text to set
     */
    public Comment(String comment) {
        this.comment = comment;
        this.isExists = true;
    }


    /**
     * @return comment set by user
     */
    public String getText() {
        return comment;
    }


    /**
     * @param comment
     */
    public void setText(String comment) {
        this.comment = comment;
        isExists = true;
    }


    /**
     * clear comment
     */
    public void clearText() {
        comment = null;
        isExists = false;
    }


    /**
     * @return an information if comment is available
     */
    public boolean isPresent() {
        return isExists;
    }


    @Override
    public String getName() {
        return "Comment";
    }
}
