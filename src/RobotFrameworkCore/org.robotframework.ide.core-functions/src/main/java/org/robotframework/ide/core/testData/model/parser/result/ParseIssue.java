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


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result
                + ((position == null) ? 0 : position.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ParseIssue other = (ParseIssue) obj;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        if (position == null) {
            if (other.position != null)
                return false;
        } else if (!position.equals(other.position))
            return false;
        if (type != other.type)
            return false;
        return true;
    }
}
