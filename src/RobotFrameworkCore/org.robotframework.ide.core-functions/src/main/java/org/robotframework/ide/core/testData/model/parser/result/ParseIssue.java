package org.robotframework.ide.core.testData.model.parser.result;

/**
 * Single an information about problem found during parsing test data to model
 * representation.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 */
public class ParseIssue {

    private final ParseIssueType type;
    private final String position;
    private final String message;


    /**
     * @param type
     * @param position
     *            you could use here line number or other clear for You
     *            localization information, therefore this element is string and
     *            not number
     * @param message
     */
    public ParseIssue(final ParseIssueType type, final String position,
            final String message) {
        this.type = type;
        this.position = position;
        this.message = message;
    }


    public String getMessage() {
        return message;
    }


    public String getPosition() {
        return position;
    }


    public ParseIssueType getType() {
        return type;
    }

    /**
     * Severity of problem
     * 
     * @author wypych
     * @serial 1.0
     */
    public static enum ParseIssueType {
        WARN, ERROR
    }
}
