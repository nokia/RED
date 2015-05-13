package org.robotframework.ide.core.testData.text;



/**
 * @author wypych
 * 
 */
public class RobotToken {

    private final RobotTokenType type;
    private String text;
    private final LinearFilePosition startPos;
    private LinearFilePosition endPos;


    public RobotToken(final RobotTokenType type,
            final LinearFilePosition startPos) {
        this.type = type;
        this.startPos = startPos;
    }


    public RobotTokenType getType() {
        return type;
    }


    public String getText() {
        String result = null;

        if (text == null) {
            result = type.getSpecialAsText();
        } else {
            result = text;
        }

        return result;
    }


    public LinearFilePosition getStartPos() {
        return startPos;
    }


    public LinearFilePosition getEndPos() {
        return endPos;
    }


    public void setEndPos(LinearFilePosition endPos) {
        this.endPos = endPos;
    }


    public void setText(String text) {
        this.text = text;
    }


    @Override
    public String toString() {
        return String.format(
                "RobotToken [type=%s, text=%s, startPos=%s, endPos=%s]", type,
                text, startPos, endPos);
    }

}
