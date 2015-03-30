package org.robotframework.ide.core.testData.model;

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


    public String getText() {
        return comment;
    }


    public void setText(String comment) {
        this.comment = comment;
        isExists = true;
    }


    public void clearText() {
        comment = null;
        isExists = false;
    }


    public boolean isPresent() {
        return isExists;
    }
}
